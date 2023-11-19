package gg.generations.rarecandy.renderer.animation;

import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SkinModel;
import gg.generations.rarecandy.pokeutils.ModelNode;
import gg.generations.rarecandy.renderer.rendering.Bone;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class Skeleton {
    public final Bone[] boneArray;
    public final Map<String, Bone> boneMap;
    public final ModelNode rootNode;
    private final SkinModel raw;

    public Skeleton(SkinModel skeleton) {
        var boneCount = skeleton.getJoints().size();
        this.raw = skeleton;
        this.boneArray = new Bone[boneCount];
        this.boneMap = new HashMap<>(boneCount);
        var root = findRoot(skeleton);
        this.rootNode = new ModelNode(root, null);
        var array = new float[16];

        for (var i = 0; i < skeleton.getJoints().size(); i++) {
            var jointNode = skeleton.getJoints().get(i);
            var bone = new Bone(jointNode, new Matrix4f().set(skeleton.getInverseBindMatrix(i, array)));
            this.boneArray[i] = bone;
            this.boneMap.put(jointNode.getName(), bone);
        }
    }

    private NodeModel findRoot(SkinModel skeleton) {
        var root = skeleton.getJoints().get(0);

        while(root.getParent() != null && skeleton.getJoints().contains(root.getParent())) {
            root = root.getParent();
        }

        return root;
    }

    public Skeleton(Skeleton skeleton) {
        this(skeleton.raw);
    }

    public Bone get(String name) {
        return boneMap.get(name);
    }

    public Bone get(int id) {
        if (id > boneArray.length)
            throw new RuntimeException("Animation is referencing bones which are out of bounds. Model is missing bone " + id);
        return boneArray[id];
    }

    public String getName(int id) {
        var bone = get(id);

        for (var entry : boneMap.entrySet()) {
            if (entry.getValue().equals(bone)) return entry.getKey();
        }

        return "";
    }

    public int getId(Bone bone) {
        for (int i = 0; i < boneArray.length; i++) {
            if (bone.equals(boneArray[i])) return i;
        }

        return 0;
    }
}