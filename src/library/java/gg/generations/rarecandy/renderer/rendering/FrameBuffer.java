package gg.generations.rarecandy.renderer.rendering;

import com.spinyowl.legui.image.Image;
import gg.generations.rarecandy.renderer.loading.ITexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FrameBuffer extends Image implements ITexture {
    private final int framebufferId;
    private final int textureId;
    private final int rbo;
    private final int width;
    private final int height;

    public FrameBuffer(int width, int height) {
        super();

        this.width = width;
        this.height = height;
        framebufferId = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);

        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureId, 0);

        rbo = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, rbo);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete!");
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void bindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
    }

    public void unbindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void bind(int slot) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    @Override
    public int width() {
        return width;
    }

    public int getWidth() {
        return width();
    }

    public int getHeight() {
        return height();
    }

    public int height() {
        return height;
    }

    @Override
    public void close() {
        GL30.glDeleteFramebuffers(framebufferId);
        GL11.glDeleteTextures(textureId);
        GL30.glDeleteRenderbuffers(rbo);
    }

//    public boolean captureScreenshot(Path filePath, boolean isPortrait) throws IOException {
//        if (Files.notExists(filePath)) Files.createDirectories(filePath);
//
//        var scale = isPortrait ? 2 : 4;
//        var newWidth = (width / scale);
//        var newHeight = (height / scale);
//
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
//        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
//        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
//        ByteBuffer resizedBuffer = BufferUtils.createByteBuffer(newWidth * newHeight * 4);
//        STBImageResize.stbir_resize_uint8(buffer, width, height, 0, resizedBuffer, newWidth, newHeight, 0, 4);
//        STBImageWrite.stbi_flip_vertically_on_write(true);
//        var result = STBImageWrite.stbi_write_png(filePath.toString(), newWidth, newHeight, 4, resizedBuffer, newWidth * 4);
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//        return result;
//    }

    public boolean regularScreenshot(Path filePath, int res) throws IOException {
        if (Files.notExists(filePath)) Files.createDirectories(filePath);
        int newWidth = res;
        int newHeight = res;

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;
                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, height - 1 - y, argb); // flip vertically
            }
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        try {
            return ImageIO.write(resizedImage, "png", filePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean regularScreenshot(Path filePath) throws IOException {
        if (Files.notExists(filePath)) Files.createDirectories(filePath);
        int newWidth = width;
        int newHeight = height;

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;
                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, height - 1 - y, argb); // flip vertically
            }
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        try {
            return ImageIO.write(resizedImage, "png", filePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean captureScreenshot(Path filePath, boolean isPortrait) throws IOException {
        if (Files.notExists(filePath)) Files.createDirectories(filePath);
        int scale = isPortrait ? 4 : 2;
        int newWidth = width / scale;
        int newHeight = height / scale;

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * y)) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;
                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, height - 1 - y, argb); // flip vertically
            }
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        try {
            return ImageIO.write(resizedImage, "png", filePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTextureId() {
        return textureId;
    }
}
