// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.tracn;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class AnimationEntry extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static AnimationEntry getRootAsAnimationEntry(ByteBuffer _bb) { return getRootAsAnimationEntry(_bb, new AnimationEntry()); }
  public static AnimationEntry getRootAsAnimationEntry(ByteBuffer _bb, AnimationEntry obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public AnimationEntry __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String animationName() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer animationNameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer animationNameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public String filename() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer filenameAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer filenameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }

  public static int createAnimationEntry(FlatBufferBuilder builder,
      int animationNameOffset,
      int filenameOffset) {
    builder.startTable(2);
    AnimationEntry.addFilename(builder, filenameOffset);
    AnimationEntry.addAnimationName(builder, animationNameOffset);
    return AnimationEntry.endAnimationEntry(builder);
  }

  public static void startAnimationEntry(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addAnimationName(FlatBufferBuilder builder, int animationNameOffset) { builder.addOffset(0, animationNameOffset, 0); }
  public static void addFilename(FlatBufferBuilder builder, int filenameOffset) { builder.addOffset(1, filenameOffset, 0); }
  public static int endAnimationEntry(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public AnimationEntry get(int j) { return get(new AnimationEntry(), j); }
    public AnimationEntry get(AnimationEntry obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public AnimationEntryT unpack() {
    AnimationEntryT _o = new AnimationEntryT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(AnimationEntryT _o) {
    String _oAnimationName = animationName();
    _o.setAnimationName(_oAnimationName);
    String _oFilename = filename();
    _o.setFilename(_oFilename);
  }
  public static int pack(FlatBufferBuilder builder, AnimationEntryT _o) {
    if (_o == null) return 0;
    int _animationName = _o.getAnimationName() == null ? 0 : builder.createString(_o.getAnimationName());
    int _filename = _o.getFilename() == null ? 0 : builder.createString(_o.getFilename());
    return createAnimationEntry(
      builder,
      _animationName,
      _filename);
  }
}

