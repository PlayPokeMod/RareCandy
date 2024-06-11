// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm.tracks._boolean;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Framed16BooleanTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static Framed16BooleanTrack getRootAsFramed16BooleanTrack(ByteBuffer _bb) { return getRootAsFramed16BooleanTrack(_bb, new Framed16BooleanTrack()); }
  public static Framed16BooleanTrack getRootAsFramed16BooleanTrack(ByteBuffer _bb, Framed16BooleanTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Framed16BooleanTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int frames(int j) { int o = __offset(4); return o != 0 ? bb.getShort(__vector(o) + j * 2) & 0xFFFF : 0; }
  public int framesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ShortVector framesVector() { return framesVector(new ShortVector()); }
  public ShortVector framesVector(ShortVector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer framesAsByteBuffer() { return __vector_as_bytebuffer(4, 2); }
  public ByteBuffer framesInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 2); }
  public boolean bool(int j) { int o = __offset(6); return o != 0 ? 0!=bb.get(__vector(o) + j * 1) : false; }
  public int boolLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public BooleanVector boolVector() { return boolVector(new BooleanVector()); }
  public BooleanVector boolVector(BooleanVector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer boolAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer boolInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }

  public static int createFramed16BooleanTrack(FlatBufferBuilder builder,
      int framesOffset,
      int boolOffset) {
    builder.startTable(2);
    Framed16BooleanTrack.addBool(builder, boolOffset);
    Framed16BooleanTrack.addFrames(builder, framesOffset);
    return Framed16BooleanTrack.endFramed16BooleanTrack(builder);
  }

  public static void startFramed16BooleanTrack(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addFrames(FlatBufferBuilder builder, int framesOffset) { builder.addOffset(0, framesOffset, 0); }
  public static int createFramesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(2, data.length, 2); for (int i = data.length - 1; i >= 0; i--) builder.addShort((short) data[i]); return builder.endVector(); }
  public static void startFramesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(2, numElems, 2); }
  public static void addBool(FlatBufferBuilder builder, int boolOffset) { builder.addOffset(1, boolOffset, 0); }
  public static int createBoolVector(FlatBufferBuilder builder, boolean[] data) { builder.startVector(1, data.length, 1); for (int i = data.length - 1; i >= 0; i--) builder.addBoolean(data[i]); return builder.endVector(); }
  public static void startBoolVector(FlatBufferBuilder builder, int numElems) { builder.startVector(1, numElems, 1); }
  public static int endFramed16BooleanTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Framed16BooleanTrack get(int j) { return get(new Framed16BooleanTrack(), j); }
    public Framed16BooleanTrack get(Framed16BooleanTrack obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public Framed16BooleanTrackT unpack() {
    Framed16BooleanTrackT _o = new Framed16BooleanTrackT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(Framed16BooleanTrackT _o) {
    int[] _oFrames = new int[framesLength()];
    for (int _j = 0; _j < framesLength(); ++_j) {_oFrames[_j] = frames(_j);}
    _o.setFrames(_oFrames);
    boolean[] _oBool = new boolean[boolLength()];
    for (int _j = 0; _j < boolLength(); ++_j) {_oBool[_j] = bool(_j);}
    _o.setBool(_oBool);
  }
  public static int pack(FlatBufferBuilder builder, Framed16BooleanTrackT _o) {
    if (_o == null) return 0;
    int _frames = 0;
    if (_o.getFrames() != null) {
      _frames = createFramesVector(builder, _o.getFrames());
    }
    int _bool = 0;
    if (_o.getBool() != null) {
      _bool = createBoolVector(builder, _o.getBool());
    }
    return createFramed16BooleanTrack(
      builder,
      _frames,
      _bool);
  }
}
