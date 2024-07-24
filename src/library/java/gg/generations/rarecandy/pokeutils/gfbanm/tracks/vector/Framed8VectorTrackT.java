// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm.tracks.vector;

import gg.generations.rarecandy.pokeutils.gfbanm.Vec3T;
import gg.generations.rarecandy.renderer.animation.TransformStorage;
import org.joml.Vector3f;

public class Framed8VectorTrackT implements VectorProcessor {
  private int[] frames;
  private Vec3T[] co;

  public int[] getFrames() { return frames; }

  public void setFrames(int[] frames) { this.frames = frames; }

  public Vec3T[] getCo() { return co; }

  public void setCo(Vec3T[] co) { this.co = co; }


  public Framed8VectorTrackT() {
    this.frames = null;
    this.co = null;
  }

  @Override
  public void process(TransformStorage<Vector3f> keys, Vector3f offset) {
    for (int i = 0; i < getCo().length; i++) {
      var vec = getCo()[i];
      keys.add(getFrames()[i], new Vector3f(vec.getX(), vec.getY(), vec.getZ()).add(offset));
    }
  }
}

