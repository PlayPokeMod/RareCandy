package gg.generations.rarecandy.pokeutils.tracp;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Trigger extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static Trigger getRootAsTrigger(ByteBuffer _bb) { return getRootAsTrigger(_bb, new Trigger()); }
  public static Trigger getRootAsTrigger(ByteBuffer _bb, Trigger obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Trigger __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }

  public static int createTrigger(FlatBufferBuilder builder,
      int nameOffset) {
    builder.startTable(1);
    Trigger.addName(builder, nameOffset);
    return Trigger.endTrigger(builder);
  }

  public static void startTrigger(FlatBufferBuilder builder) { builder.startTable(1); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static int endTrigger(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Trigger get(int j) { return get(new Trigger(), j); }
    public Trigger get(Trigger obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public TriggerT unpack() {
    TriggerT _o = new TriggerT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(TriggerT _o) {
    String _oName = name();
    _o.setName(_oName);
  }
  public static int pack(FlatBufferBuilder builder, TriggerT _o) {
    if (_o == null) return 0;
    int _name = _o.getName() == null ? 0 : builder.createString(_o.getName());
    return createTrigger(
      builder,
      _name);
  }
}

