// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm.tracks._byte;

import gg.generations.rarecandy.renderer.animation.TransformStorage;
import org.joml.Vector3f;

public class DynamicByteTrackT implements ByteProcessor {
  private int[] byte_;

  public int[] getByte() { return byte_; }

  public void setByte(int[] byte_) { this.byte_ = byte_; }

  public DynamicByteTrackT() {
    this.byte_ = null;
  }

  @Override
  public void process(TransformStorage<Byte> keys) {
    for (int i = 0; i < getByte().length; i++) {
      var vec = getByte()[i];
      keys.add(i, (byte) vec);
    }
  }
}

