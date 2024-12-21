package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.model.material.Material;

import java.util.*;

public class ModelConfig {
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantDetails> defaultVariant;
    public Map<String, VariantParent> variants;
    public Map<String, HideDuringAnimation> hideDuringAnimation = Collections.emptyMap();

    public Map<String, Integer> animationFpsOverride;

    public Map<String, SkeletalTransform> offsets = new HashMap<>();

    public Map<String, List<String>> materialsWithSameMaterialAnimation;

    public List<String> ignoreScaleInAnimation;

    public Map<String, MeshOptions> modelOptions;

    public List<String> meshesToRenderFirst;

    public Map<String, List<String>> aliases;

    public boolean excludeMeshNamesFromSkeleton = false;

    public List<String> getMaterialsForAnimation(String trackName) {
        var list = new ArrayList<String>();
        list.add(trackName);

        if(materialsWithSameMaterialAnimation != null) {
            if(materialsWithSameMaterialAnimation.containsKey(trackName)) {
                list.addAll(materialsWithSameMaterialAnimation.get(trackName));
            }
        }

        return list;
    }

    public Map<String, Material> prepMaterials(Map<String, String> images) {
        var map = new HashMap<String, Material>();

        for (Map.Entry<String, MaterialReference> entry : materials.entrySet()) {
            String k = entry.getKey();
            MaterialReference v = entry.getValue();
            var material = MaterialReference.process(k, materials, images);

            map.put(k, material);
        }

        return map;
    }

    public record HideDuringAnimation(boolean blackList, List<String> animations) {
        public static final HideDuringAnimation NONE = new HideDuringAnimation();

        public HideDuringAnimation() {
            this(false, null);
        }

        public boolean check(Animation animation) {
            return check(animation != null ? animation.name : null);
        }

        public boolean check(String animation) {
            if (animations != null)
                return animations.contains(animation) == blackList;
            return false;
        }
    }
}
