package com.pixelmongenerations.rarecandy.animation;

import com.pixelmongenerations.pkl.assimp.AssimpUtils;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AINodeAnim;

import java.util.*;
import java.util.stream.Collectors;

public class AnimationStorage {

    public final String name;
    public final float ticksPerSecond;
    public final double animationDuration;
    public final Map<String, AnimationNode> animationNodes;
    private final Bones bones;

    public AnimationStorage(RawAnimation rawAnimation) {
        this.name = rawAnimation.name;
        this.ticksPerSecond = (float) (rawAnimation.aiAnim.mTicksPerSecond() != 0 ? rawAnimation.aiAnim.mTicksPerSecond() : 25.0f);
        this.animationDuration = rawAnimation.aiAnim.mDuration();
        this.animationNodes = new HashMap<>();
        this.bones = new Bones(rawAnimation.bones);

        fillAnimationNodes(rawAnimation);
    }

    public double getAnimationTime(double passedSecondsSinceStart) {
        var tps = ticksPerSecond != 0 ? ticksPerSecond : 25.0f;
        var tickTime = passedSecondsSinceStart * tps;
        return (tickTime % (float) animationDuration);
    }

    public Matrix4f[] getFrameTransform(double animTime, ModelNode rootModelNode) {
        var boneTransforms = new Matrix4f[this.bones.boneArray.length];
        readNodeHierarchy((float) animTime, rootModelNode, new Matrix4f().identity(), boneTransforms);
        return boneTransforms;
    }

    protected void readNodeHierarchy(float animTime, ModelNode node, Matrix4f parentTransform, Matrix4f[] boneTransforms) {
        var name = node.name;
        var transform = node.transform;
        var pNodeAnim = animationNodes.get(name);

        if (pNodeAnim != null) {
            var scale = AnimationMath.calcInterpolatedScaling(animTime, pNodeAnim);
            var scalingMat = new Matrix4f().identity().scale(scale.x(), scale.y(), scale.z());

            var rotation = AnimationMath.calcInterpolatedRotation(animTime, pNodeAnim);
            var rotationMat = rotation.get(new Matrix4f().identity());

            var translation = AnimationMath.calcInterpolatedPosition(animTime, pNodeAnim);
            var translationMat = new Matrix4f().identity().translate(translation.x(), translation.y(), translation.z());

            transform = new Matrix4f(translationMat).mul(new Matrix4f(rotationMat)).mul(new Matrix4f(scalingMat));
        }

        transform = new Matrix4f(parentTransform).mul(transform);
        var bone = bones.get(name);
        if(bone != null) boneTransforms[bones.getId(bone)] = new Matrix4f().identity().mul(new Matrix4f(transform)).mul(bone.offsetMatrix);

        for (var child : node.children) {
            readNodeHierarchy(animTime, child, transform, boneTransforms);
        }
    }

    private void fillAnimationNodes(RawAnimation rawAnimation) {
        for (var i = 0; i < rawAnimation.aiAnim.mNumChannels(); i++) {
            AINodeAnim nodeAnim = AINodeAnim.create(rawAnimation.aiAnim.mChannels().get(i));
            animationNodes.put(nodeAnim.mNodeName().dataString(), new AnimationNode(nodeAnim));
        }
    }

    public static class AnimationNode {
        public final TreeMap<Double, Vector3f> positionKeys = new TreeMap<>();
        public final TreeMap<Double, Quaternionf> rotationKeys = new TreeMap<>();
        public final TreeMap<Double, Vector3f> scaleKeys = new TreeMap<>();

        public AnimationNode(AINodeAnim animNode) {
            if (animNode.mNumPositionKeys() > 0) {
                for (var positionKey : Objects.requireNonNull(animNode.mPositionKeys(), "Position keys were null")) {
                    positionKeys.put(positionKey.mTime(), AssimpUtils.from(positionKey.mValue()));
                }
            }

            if (animNode.mNumRotationKeys() > 0) {
                for (var rotationKey : Objects.requireNonNull(animNode.mRotationKeys(), "Rotation keys were null")) {
                    rotationKeys.put(rotationKey.mTime(), AssimpUtils.from(rotationKey.mValue()));
                }
            }

            if (animNode.mNumScalingKeys() > 0) {
                for (var scaleKey : Objects.requireNonNull(animNode.mScalingKeys(), "Scaling keys were null")) {
                    scaleKeys.put(scaleKey.mTime(), AssimpUtils.from(scaleKey.mValue()));
                }
            }
        }

        public Matrix4f getTransformForTime(float animTime) {
            throw new RuntimeException("Not Implemented");
        }

        public List<Vector3f> getPositionKeys() {
            return positionKeys.values().stream().toList();
        }

        public List<Quaternionf> getRotationKeys() {
            return rotationKeys.values().stream().toList();
        }

        public List<Vector3f> getScaleKeys() {
            return scaleKeys.values().stream().toList();
        }

        public Vector3f getDefaultPosition() {
            return positionKeys.get(positionKeys.keySet().stream().toList().get(0));
        }

        public Quaternionf getDefaultRotation() {
            return rotationKeys.get(rotationKeys.keySet().stream().toList().get(0));
        }

        public Vector3f getDefaultScale() {
            return scaleKeys.get(scaleKeys.keySet().stream().toList().get(0));
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}