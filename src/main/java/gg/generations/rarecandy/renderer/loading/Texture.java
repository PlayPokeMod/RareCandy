package gg.generations.rarecandy.renderer.loading;

import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLImage;
import com.traneptora.jxlatte.JXLatte;
import com.traneptora.jxlatte.color.ColorFlags;
import com.traneptora.jxlatte.util.ImageBuffer;
import io.github.mudbill.dds.DDSFile;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class Texture implements ITexture {
    private TextureDetails details;
    public int id;
    private int width;
    private int height;

    public Texture(TextureDetails textureDetails) {
        this.details = textureDetails;
        this.width = textureDetails.width();
        this.height = textureDetails.height();
    }

    public void bind(int slot) {
        if(details != null) {
            this.id = details.init();
//            try {
//                details.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
                details = null;
//            }
        }

        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void close() throws IOException {
        GL11.glDeleteTextures(id);
    }

    public static Texture read(byte[] imageBytes, String name) throws IOException {
        if(name.endsWith(".dds")) {
            var dds = new DDSFile(new ByteArrayInputStream(imageBytes));

            return new Texture(new DDSTextureDetails(dds));
        } else if(name.endsWith(".jxl")) {
            return new Texture(readJXL(imageBytes));
        } else return new Texture(read(imageBytes));
    }

    public static TextureDetails read(byte[] bytes) {
        ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();

        IntBuffer wBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer hBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer compBuffer = MemoryUtil.memAllocInt(1);

        // Use info to read image metadata without decoding the entire image.
        // We don't need this for this demo, just testing the API.
        if (!stbi_info_from_memory(imageBuffer, wBuffer, hBuffer, compBuffer)) {
            return null;
        }

        // Decode the image
        var image = stbi_load_from_memory(imageBuffer, wBuffer, hBuffer, compBuffer, 0);
        if (image == null) {
            return null;
        }

        var w = wBuffer.get(0);
        var h = hBuffer.get(0);
        var comp = compBuffer.get(0);

        MemoryUtil.memFree(wBuffer);
        MemoryUtil.memFree(hBuffer);
        MemoryUtil.memFree(compBuffer);
        MemoryUtil.memFree(imageBuffer);

        if (comp != 3 && comp != 4) throw new RuntimeException("Inccorect amount of color channels");


        return new TextureDetailsSTB(image, comp == 3 ? Type.RGB_BYTE : Type.RGBA_BYTE, w, h);
    }

    private static TextureDetails readJXL(byte[] bytes) throws IOException {
        // Decode the JPEG XL image
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        JXLImage jxlImage = new JXLDecoder(inputStream).decode();

        if (jxlImage == null) {
            throw new IOException("Failed to decode JPEG XL image.");
        }

        // Validate 8-bit support (common for typical GL texture usage)
        int bitDepth = jxlImage.getHeader().getBitDepthHeader().bitsPerSample;
        if (bitDepth != 8) {
            throw new IOException("Unsupported bit depth: " + bitDepth + ". Only 8-bit is supported.");
        }

        // Retrieve relevant metadata
        int width      = jxlImage.getWidth();
        int height     = jxlImage.getHeight();
        boolean hasAlpha = jxlImage.hasAlpha();

        // Decide how many channels to pull from the image buffers
        // For colorEncoding == CE_GRAY -> 1 channel (plus alpha if present)
        // For colorEncoding == CE_RGB  -> 3 channels (plus alpha if present)
        int colorChannels = jxlImage.getColorChannelCount();  // 1 (gray) or 3 (rgb)
        if (hasAlpha) {
            colorChannels++; // add one for alpha
        }

        // Prepare a ByteBuffer for the raw pixel data
        ByteBuffer pixelData = MemoryUtil.memAlloc(width * height * colorChannels);

        // We'll gather channel indices to read in the correct order (R, G, B, then alpha)
        // If it's grayscale, that means channel=0 for Gray, and if alpha exists, alphaIndex
        // If it's RGB, channels=0,1,2, then alphaIndex if present
        ImageBuffer[] buffers = jxlImage.getBuffer(false);
        int[] channelIndices = getInts(jxlImage, hasAlpha);

        // Write raw pixel data (8-bit) into the ByteBuffer
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int index : channelIndices) {
                    // Get the integer sample; JXL is guaranteed 8-bit in this context
                    int sample = buffers[index].getIntBuffer()[y][x];
                    pixelData.put((byte) sample);
                }
            }
        }
        pixelData.flip();

        // Determine texture format
        Texture.Type type = hasAlpha ? Texture.Type.RGBA_BYTE : Texture.Type.RGB_BYTE;

        // Wrap and return
        return new TextureDetailsSTB(pixelData, type, width, height);
    }

    private static int @NotNull [] getInts(JXLImage jxlImage, boolean hasAlpha) {
        int alphaIndex        = jxlImage.getAlphaIndex(); // -1 if none

        // Build the ordered channel list
        // e.g., if CE_GRAY and alphaIndex=1 -> channels = [0, 1]
        //       if CE_RGB and alphaIndex=3  -> channels = [0, 1, 2, 3]
        //       if CE_RGB and no alpha      -> channels = [0, 1, 2]
        //       etc.
        int[] channelIndices;
        if (jxlImage.getColorEncoding() == ColorFlags.CE_GRAY) {
            channelIndices = hasAlpha ? new int[]{0, alphaIndex} : new int[]{0};
        } else {
            // CE_RGB
            if (hasAlpha) {
                channelIndices = new int[]{0, 1, 2, alphaIndex};
            } else {
                channelIndices = new int[]{0, 1, 2};
            }
        }
        return channelIndices;
    }

    public enum Type {
        RGBA_BYTE(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE),
        RGB_BYTE(GL30.GL_RGB8, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE);

        public final int internalFormat;
        public final int format;
        public final int type;

        Type(int internalFormat, int format, int type) {
            this.internalFormat = internalFormat;
            this.format = format;
            this.type = type;
        }
    }
}