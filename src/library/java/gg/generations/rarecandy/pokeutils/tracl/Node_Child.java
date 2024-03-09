package gg.generations.rarecandy.pokeutils.tracl;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Node_Child extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static Node_Child getRootAsNode_Child(ByteBuffer _bb) { return getRootAsNode_Child(_bb, new Node_Child()); }
  public static Node_Child getRootAsNode_Child(ByteBuffer _bb, Node_Child obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Node_Child __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public long index() { int o = __offset(4); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
  public String name() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public ByteBuffer nameInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }
  public Channels channels() { return channels(new Channels()); }
  public Channels channels(Channels obj) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public long unk() { int o = __offset(10); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }

  public static int createNode_Child(FlatBufferBuilder builder,
      long index,
      int nameOffset,
      int channelsOffset,
      long unk) {
    builder.startTable(4);
    Node_Child.addUnk(builder, unk);
    Node_Child.addChannels(builder, channelsOffset);
    Node_Child.addName(builder, nameOffset);
    Node_Child.addIndex(builder, index);
    return Node_Child.endNode_Child(builder);
  }

  public static void startNode_Child(FlatBufferBuilder builder) { builder.startTable(4); }
  public static void addIndex(FlatBufferBuilder builder, long index) { builder.addInt(0, (int) index, (int) 0L); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(1, nameOffset, 0); }
  public static void addChannels(FlatBufferBuilder builder, int channelsOffset) { builder.addOffset(2, channelsOffset, 0); }
  public static void addUnk(FlatBufferBuilder builder, long unk) { builder.addInt(3, (int) unk, (int) 0L); }
  public static int endNode_Child(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Node_Child get(int j) { return get(new Node_Child(), j); }
    public Node_Child get(Node_Child obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
  public Node_ChildT unpack() {
    Node_ChildT _o = new Node_ChildT();
    unpackTo(_o);
    return _o;
  }
  public void unpackTo(Node_ChildT _o) {
    long _oIndex = index();
    _o.setIndex(_oIndex);
    String _oName = name();
    _o.setName(_oName);
    if (channels() != null) _o.setChannels(channels().unpack());
    else _o.setChannels(null);
    long _oUnk = unk();
    _o.setUnk(_oUnk);
  }
  public static int pack(FlatBufferBuilder builder, Node_ChildT _o) {
    if (_o == null) return 0;
    int _name = _o.getName() == null ? 0 : builder.createString(_o.getName());
    int _channels = _o.getChannels() == null ? 0 : Channels.pack(builder, _o.getChannels());
    return createNode_Child(
      builder,
      _o.getIndex(),
      _name,
      _channels,
      _o.getUnk());
  }
}

