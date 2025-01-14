package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MaterialReference {
//    public static Codec<MaterialReference> CODEC = RecordCodecBuilder.create(instance -> {
//        instance.group(
//                Codec.STRING.optionalFieldOf("inherits", null).forGetter(a -> a.parent),
//                Codec.STRING.optionalFieldOf("shader", "solid").forGetter(a -> a.shader),
//                CullType.CODEC.optionalFieldOf("cull", CullType.None).forGetter(a -> a.cull),
//                BlendType.CODEC.optionalFieldOf("blend", BlendType.None).forGetter(a -> a.blend)
//
//        )
//    })

    public String parent;
    public String shader;
    public String effect;

    public CullType cull;

    public BlendType blend;

    public Map<String, String> images;

    public Map<String, Object> values;

    public MaterialReference(String parent, String shader, CullType cull, BlendType blend, Map<String, String> images, Map<String, Object> values) {
        this.parent = parent;
        this.shader = shader;
        this.cull = cull;
        this.blend = blend;
        this.images = images;
        this.values = values;
    }

    public static Material process(String name, @NotNull Map<String, MaterialReference> materialreferences, @NotNull Map<String, String> imageMap) {
        var reference = materialreferences.get(name);

        var cull = reference.cull;
        var blend = reference.blend;
        var shader = reference.shader;
        var images = new HashMap<>(reference.images);
        var values = new HashMap<>(reference.values);
        var parent = reference.parent;

        while (parent != null) {
            reference = materialreferences.get(parent);

            if(reference == null) parent = null;
            else {

                if (!shader.equals(reference.shader)) {
                    shader = reference.shader;
                }

                if (!cull.equals(reference.cull)) {
                    cull = reference.cull;
                }

                if (!blend.equals(reference.blend)) {
                    blend = reference.blend;
                }

                reference.images.forEach((key, value) -> images.merge(key, value, (old, value1) -> old));
                reference.values.forEach((key, value) -> values.merge(key, value, (old, value1) -> old));

                parent = reference.parent;
            }
        }

        var map = new HashMap<String, String>();
        for (Map.Entry<String, String> a : images.entrySet()) {
            if (imageMap.containsKey(a.getValue())) {
                if (map.put(a.getKey(), imageMap.get(a.getValue())) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            } else {
                map.put(a.getKey(), a.getValue());
            }
        }
        return new Material(name, map, values, cull, blend, shader);
    }
    public static final class Serializer implements JsonDeserializer<MaterialReference> {
        @Override
        public MaterialReference deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            String shader = "solid";

            String effect = null;

            CullType cull = CullType.None;

            BlendType blend = BlendType.None;

            Map<String, String> images = new HashMap<>();

            Map<String, Object> values = new HashMap<>();

            var jsonObject = json.getAsJsonObject();

            String parent = jsonObject.has("inherits") ? jsonObject.get("inherits").getAsString() : null;

            if(jsonObject.has("type")) {

                var type = jsonObject.getAsJsonPrimitive("type").getAsString();

                if (jsonObject.has("texture")) {
                    var texture = jsonObject.getAsJsonPrimitive("texture").getAsString();

                    images.put("diffuse", texture);

                    if (type.equals("masked")) {
                        var color = jsonObject.has("color") ? color(jsonObject.get("color")) : new Vector3f(1.0f, 1.0f, 1.0f);
                        shader = "masked";
                        values.put("color", color);
                        images.put("mask", jsonObject.getAsJsonPrimitive("mask").getAsString());
//                        return new MaskReferenceMaterial(texture, jsonObject.getAsJsonPrimitive("mask").getAsString(), color);
                    } else {

                        switch (type) {
                            case "transparent" -> {
                                blend = BlendType.Regular;
                            }
                            case "cull" -> {
                                cull = CullType.Forward;
                            }
                            case "unlit_cull" -> {
                                cull = CullType.Forward;
                                values.put("useLight", false);
                            }
                            case "unlit" -> {
                                values.put("useLight", false);
                            }
                        }
                    }
                }
            } else {
                shader = jsonObject.has("shader") ? jsonObject.getAsJsonPrimitive("shader").getAsString() : "solid";
                effect = jsonObject.has("effect") ? jsonObject.getAsJsonPrimitive("effect").getAsString() : null;
                cull = jsonObject.has("cull") ? CullType.from(jsonObject.getAsJsonPrimitive("cull").getAsString()) : CullType.None;
                blend = jsonObject.has("blend") ? BlendType.from(jsonObject.getAsJsonPrimitive("blend").getAsString()) : BlendType.None;
                images = jsonObject.has("images") ? images(jsonObject.getAsJsonObject("images")) : new HashMap<>();
                values = jsonObject.has("values") ? values(jsonObject.getAsJsonObject("values")) : new HashMap<>();
            }

            return new MaterialReference(parent, shader + (effect != null ? "_" + effect : ""), cull, blend, images, values);

//            throw new JsonParseException("Material type %s invalid".formatted(type));
        }
    }

    private static Map<String, String> images(JsonObject images) {
        return images.asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> a.getValue().getAsString()));
    }

    private static Map<String, Object> values(JsonObject object) {
        var values = new HashMap<String, Object>();

        object.asMap().forEach((key, value) -> {
            if(value.isJsonObject()) {
                if(object.has("type")) {

                    var obj = value.getAsJsonObject();
                    var val = obj.get("value");

                    switch (obj.getAsJsonPrimitive("type").getAsString()) {
                        case "boolean" -> {
                            values.put(key, val.getAsBoolean());
                        }
                        case "color" -> {
                            values.put(key, MaterialReference.color(val));
                        }
                        case "float" -> {
                            values.put(key, val.getAsFloat());
                        }
                    }
                } else {
                    if(object.has("x") && object.has("y") && object.has("z")) {
                        values.put(key, new Vector3f(object.getAsJsonPrimitive("x").getAsFloat(), object.getAsJsonPrimitive("y").getAsFloat(), object.getAsJsonPrimitive("z").getAsFloat()));
                    }
                }
            } else if(value.isJsonPrimitive()) {
                if (value.getAsJsonPrimitive().isBoolean()) values.put(key, value.getAsBoolean());
                else if (value.getAsJsonPrimitive().isNumber()) values.put(key, value.getAsFloat());
                else if (value.getAsJsonPrimitive().isString()) values.put(key, color(value));
            } else if(value.isJsonArray()) values.put(key, color(value));
        });

        return values;
    }


    public static Vector3f color(JsonElement element) {
        if(element.isJsonArray()) {
            var array = element.getAsJsonArray();
            return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        } else if(element.isJsonPrimitive()){
            int colorValue = Integer.parseInt(element.getAsString().replace("#", ""), 16);

            // Extract individual R, G, and B components
            int red = (colorValue >> 16) & 0xFF;
            int green = (colorValue >> 8) & 0xFF;
            int blue = colorValue & 0xFF;

            // Normalize the RGB components to a range of 0.0 to 1.0 for Vector3f
            float r = red / 255.0f;
            float g = green / 255.0f;
            float b = blue / 255.0f;

            // Create and return the Vector3f object
            return new Vector3f(r, g, b);
        } else {
            return new Vector3f(1.0f, 1.0f, 1.0f);
        }
    }

    // Tolerance for floating-point comparison (epsilon)
    private static final float EPSILON = 1e-6f;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialReference that = (MaterialReference) o;

        // Compare simple fields
        if (!Objects.equals(parent, that.parent)) return false;
        if (!Objects.equals(shader, that.shader)) return false;
        if (!Objects.equals(cull, that.cull)) return false;
        if (!Objects.equals(blend, that.blend)) return false;

        // Compare the images map based on byte content
        if (!compareImages(images, that.images)) return false;

        // Compare the values map
        if (!compareValues(values, that.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(parent, shader, cull, blend);
        result = 31 * result + hashImages(images);
        result = 31 * result + hashValues(values);
        return result;
    }

    // Helper method to compare images based on exact byte content of key-value pairs
    private boolean compareImages(Map<String, String> images1, Map<String, String> images2) {
        if (images1 == images2) return true;
        if (images1 == null || images2 == null || images1.size() != images2.size()) return false;

        for (Map.Entry<String, String> entry : images1.entrySet()) {
            String key1 = entry.getKey();
            String value1 = entry.getValue();
            String value2 = images2.get(key1);

            // Compare key and value byte arrays
            if (value2 == null ||
                    !Arrays.equals(key1.getBytes(StandardCharsets.UTF_8), key1.getBytes(StandardCharsets.UTF_8)) ||
                    !Arrays.equals(value1.getBytes(StandardCharsets.UTF_8), value2.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
        }
        return true;
    }

    // Helper method to hash images based on byte content
    private int hashImages(Map<String, String> images) {
        if (images == null) return 0;
        return images.entrySet().stream()
                .mapToInt(e -> Arrays.hashCode(e.getKey().getBytes(StandardCharsets.UTF_8)) ^
                        Arrays.hashCode(e.getValue().getBytes(StandardCharsets.UTF_8)))
                .sum();
    }

    // Helper method to compare values map
    private boolean compareValues(Map<String, Object> values1, Map<String, Object> values2) {
        if (values1 == values2) return true;
        if (values1 == null || values2 == null || values1.size() != values2.size()) return false;

        for (Map.Entry<String, Object> entry : values1.entrySet()) {
            String key1 = entry.getKey();
            Object value1 = entry.getValue();
            Object value2 = values2.get(key1);

            if (value2 == null || !deepEquals(value1, value2)) {
                return false;
            }
        }
        return true;
    }

    // Helper method to deeply compare objects in values map
    private boolean deepEquals(Object o1, Object o2) {
        if (o1 == o2) return true;
        if (o1 == null || o2 == null) return false;

        // Handle specific types
        if (o1 instanceof Vector3f && o2 instanceof Vector3f) {
            return compareVector3f((Vector3f) o1, (Vector3f) o2);
        }
        if (o1 instanceof Vector4f && o2 instanceof Vector4f) {
            return compareVector4f((Vector4f) o1, (Vector4f) o2);
        }
        if (o1 instanceof Boolean && o2 instanceof Boolean) {
            return o1.equals(o2); // Booleans can be compared directly
        }
        if (o1 instanceof Number && o2 instanceof Number) {
            return ((Number) o1).doubleValue() == ((Number) o2).doubleValue(); // Compare numbers as doubles
        }
        if (o1 instanceof String && o2 instanceof String) {
            // Compare strings by byte content
            return Arrays.equals(((String) o1).getBytes(StandardCharsets.UTF_8),
                    ((String) o2).getBytes(StandardCharsets.UTF_8));
        }

        // Default to regular equals for other types
        return Objects.equals(o1, o2);
    }

    // Helper method to hash the values map
    private int hashValues(Map<String, Object> values) {
        if (values == null) return 0;
        return values.entrySet().stream()
                .mapToInt(e -> Objects.hash(e.getKey(), deepHashCode(e.getValue())))
                .sum();
    }

    // Helper method to deeply hash objects in values map
    private int deepHashCode(Object o) {
        if (o == null) return 0;

        // Handle specific types
        if (o instanceof Vector3f) {
            return Arrays.hashCode(new float[] { ((Vector3f) o).x, ((Vector3f) o).y, ((Vector3f) o).z });
        }
        if (o instanceof Vector4f) {
            return Arrays.hashCode(new float[] { ((Vector4f) o).x, ((Vector4f) o).y, ((Vector4f) o).z, ((Vector4f) o).w });
        }
        if (o instanceof Boolean) {
            return Boolean.hashCode((Boolean) o);
        }
        if (o instanceof Number) {
            return Double.hashCode(((Number) o).doubleValue());
        }
        if (o instanceof String) {
            // Hash the byte content of the string
            return Arrays.hashCode(((String) o).getBytes(StandardCharsets.UTF_8));
        }

        // Default to regular hashCode for other types
        return o.hashCode();
    }

    // Helper method to compare Vector3f components with an epsilon tolerance
    private boolean compareVector3f(Vector3f v1, Vector3f v2) {
        return Math.abs(v1.x - v2.x) < EPSILON &&
                Math.abs(v1.y - v2.y) < EPSILON &&
                Math.abs(v1.z - v2.z) < EPSILON;
    }

    // Helper method to compare Vector4f components with an epsilon tolerance
    private boolean compareVector4f(Vector4f v1, Vector4f v2) {
        return Math.abs(v1.x - v2.x) < EPSILON &&
                Math.abs(v1.y - v2.y) < EPSILON &&
                Math.abs(v1.z - v2.z) < EPSILON &&
                Math.abs(v1.w - v2.w) < EPSILON;
    }
}
