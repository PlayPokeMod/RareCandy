// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.GFLib.Anim;

import com.google.flatbuffers.FlatBufferBuilder;

public class FloatTrackUnion {
  private byte type;
  private TrackProcesser<Float> value;

  public byte getType() { return type; }

  public void setType(byte type) { this.type = type; }

  public TrackProcesser<Float> getValue() { return value; }

  public void setValue(TrackProcesser<Float> value) { this.value = value; }

  public FloatTrackUnion() {
    this.type = FloatTrack.NONE;
    this.value = null;
  }

  public FixedFloatTrackT asFixedFloatTrack() { return (FixedFloatTrackT) value; }
  public DynamicFloatTrackT asDynamicFloatTrack() { return (DynamicFloatTrackT) value; }
  public Framed16FloatTrackT asFramed16FloatTrack() { return (Framed16FloatTrackT) value; }
  public Framed8FloatTrackT asFramed8FloatTrack() { return (Framed8FloatTrackT) value; }

  public static int pack(FlatBufferBuilder builder, FloatTrackUnion _o) {
      return switch (_o.type) {
          case FloatTrack.FixedFloatTrack -> FixedFloatTrack.pack(builder, _o.asFixedFloatTrack());
          case FloatTrack.DynamicFloatTrack -> DynamicFloatTrack.pack(builder, _o.asDynamicFloatTrack());
          case FloatTrack.Framed16FloatTrack -> Framed16FloatTrack.pack(builder, _o.asFramed16FloatTrack());
          case FloatTrack.Framed8FloatTrack -> Framed8FloatTrack.pack(builder, _o.asFramed8FloatTrack());
          default -> 0;
      };
  }
}

