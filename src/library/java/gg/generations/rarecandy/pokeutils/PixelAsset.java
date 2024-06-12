package gg.generations.rarecandy.pokeutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(VariantParent.class, new VariantParent.Serializer())
            .registerTypeAdapter(MaterialReference.class, new MaterialReference.Serializer())
            .registerTypeAdapter(Vector2f.class, (JsonDeserializer<Vector2f>) (json, typeOfT, context) -> {
                var vec = new Vector2f();
                if (json.isJsonArray()) {
                    if (json.getAsJsonArray().size() == 2) {
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat());
                    }
                }

                return vec;
            })
            .create();

    public final Map<String, byte[]> files = new HashMap<>();
    public String modelName;
    public String name;
    private ModelConfig config;

    public PixelAsset(String modelName, byte[] glbFile) {
        this.name = modelName;
        this.modelName = modelName;
        files.put(modelName, glbFile);
    }

    public PixelAsset(Path path) {
        this(path, path.getFileName().toString() + ".pk");
    }

    public PixelAsset(Path path, String name) {
        this.name = name;
        load(path);
    }

    public void load(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {

                    if (entry.getFileName().toString().endsWith(".glb")) this.modelName = entry.getFileName().toString();

                    try {
                        files.put(entry.getFileName().toString(), Files.readAllBytes(entry));
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                }
            }

            if (files.containsKey("config.json")) {
                config = GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), ModelConfig.class);
            }

            updateSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PixelAsset of(Path path, @Nullable String debugName) {
        if(Files.isRegularFile(path)) {
            return new PixelAsset(getSevenZipFile(path), debugName);
        } else {
            return new PixelAsset(path, debugName);
        }
    }

    public PixelAsset(InputStream is, @Nullable String debugName) {
        this(getSevenZipFile(is), debugName);
    }

    public PixelAsset(SevenZFile is, @Nullable String debugName) {
        this.name = debugName;

        try {
            for (var entry : is.getEntries()) {
                if(entry.getName().endsWith("/")) continue;

                if (entry.getName().endsWith(".glb")) this.modelName = entry.getName();

                files.put(entry.getName(), is.getInputStream(entry).readAllBytes());
            }

            if (files.containsKey("config.json")) {
                config = GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), ModelConfig.class);
            }

            updateSettings();
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load " + debugName, e);
        }

    }

    public void updateSettings() {
    }

    public static TarFile getTarFile(InputStream inputStream) {
        try {
            var xzInputStream = new XZInputStream(inputStream);
            return new TarFile(xzInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    public static SevenZFile getSevenZipFile(InputStream stream) {
        try {
            return new SevenZFile(new SeekableInMemoryByteChannel(stream.readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    public static SevenZFile getSevenZipFile(Path path) {
        try {
            return new SevenZFile(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }


    public byte[] getModelFile() {
        return files.get(modelName);
    }

    public List<Map.Entry<String, byte[]>> getAnimationFiles() {
        return files.entrySet().stream().filter(a -> a.getKey().endsWith("smd")).toList();
    }

    public List<Map.Entry<String, byte[]>> getImageFiles() {
        return files.entrySet().stream().filter(a -> a.getKey().endsWith("png")).toList();
    }


    public ModelConfig getConfig() {
        return config;
    }

    public byte[] get(String key) {
        return this.files.get(key);
    }
}
