package gg.generations.rarecandy.pokeutils.tracp;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FloatVector;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Struct;
import com.google.flatbuffers.Table;
import com.google.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Switch extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static Switch getRootAsSwitch(ByteBuffer _bb) { return getRootAsSwitch(_bb, new Switch()); }
  public static Switch getRootAsSwitch(ByteBuffer _bb, Switch obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Switch __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String name() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public boolean val() { int o = __offset(6); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }

  public static int createSwitch(FlatBufferBuilder builder,
      int nameOffset,
      boolean val) {
    builder.startTable(2);
    Switch.addName(builder, nameOffset);
    Switch.addVal(builder, val);
    return Switch.endSwitch(builder);
  }

  public static void startSwitch(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(0, nameOffset, 0); }
  public static void addVal(FlatBufferBuilder builder, boolean val) { builder.addBoolean(1, val, false); }
  public static int endSwitch(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Switch get(int j) { return get(new Switch(), j); }
    public Switch get(Switch obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public SwitchT unpack() {
    SwitchT _o = new SwitchT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(SwitchT _o) {
    String _oName = name();
    _o.setName(_oName);
    boolean _oVal = val();
    _o.setVal(_oVal);
  }
  public static int pack(FlatBufferBuilder builder, SwitchT _o) {
    if (_o == null) return 0;
    int _name = _o.getName() == null ? 0 : builder.createString(_o.getName());
    return createSwitch(
      builder,
      _name,
      _o.getVal());
  }
}
