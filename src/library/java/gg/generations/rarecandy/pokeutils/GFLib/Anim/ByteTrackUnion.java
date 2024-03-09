// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.GFLib.Anim;

import com.google.flatbuffers.FlatBufferBuilder;

public class ByteTrackUnion {
  private byte type;
  private Object value;

  public byte getType() { return type; }

  public void setType(byte type) { this.type = type; }

  public Object getValue() { return value; }

  public void setValue(Object value) { this.value = value; }

  public ByteTrackUnion() {
    this.type = ByteTrack.NONE;
    this.value = null;
  }

  public FixedByteTrackT asFixedByteTrack() { return (FixedByteTrackT) value; }
  public DynamicByteTrackT asDynamicByteTrack() { return (DynamicByteTrackT) value; }
  public Framed16ByteTrackT asFramed16ByteTrack() { return (Framed16ByteTrackT) value; }
  public Framed8ByteTrackT asFramed8ByteTrack() { return (Framed8ByteTrackT) value; }

  public static int pack(FlatBufferBuilder builder, ByteTrackUnion _o) {
      return switch (_o.type) {
          case ByteTrack.FixedByteTrack -> FixedByteTrack.pack(builder, _o.asFixedByteTrack());
          case ByteTrack.DynamicByteTrack -> DynamicByteTrack.pack(builder, _o.asDynamicByteTrack());
          case ByteTrack.Framed16ByteTrack -> Framed16ByteTrack.pack(builder, _o.asFramed16ByteTrack());
          case ByteTrack.Framed8ByteTrack -> Framed8ByteTrack.pack(builder, _o.asFramed8ByteTrack());
          default -> 0;
      };
  }
}

