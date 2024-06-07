// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.rarecandy.pokeutils.gfbanm;

import com.google.flatbuffers.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class DynamicFloatTrack extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static DynamicFloatTrack getRootAsDynamicFloatTrack(ByteBuffer _bb) {
        return getRootAsDynamicFloatTrack(_bb, new DynamicFloatTrack());
    }

    public static DynamicFloatTrack getRootAsDynamicFloatTrack(ByteBuffer _bb, DynamicFloatTrack obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public DynamicFloatTrack __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public float float_(int j) {
        int o = __offset(4);
        return o != 0 ? bb.getFloat(__vector(o) + j * 4) : 0;
    }

    public int float_Length() {
        int o = __offset(4);
        return o != 0 ? __vector_len(o) : 0;
    }

    public FloatVector floatVector() {
        return floatVector(new FloatVector());
    }

    public FloatVector floatVector(FloatVector obj) {
        int o = __offset(4);
        return o != 0 ? obj.__assign(__vector(o), bb) : null;
    }

    public ByteBuffer float_AsByteBuffer() {
        return __vector_as_bytebuffer(4, 4);
    }

    public ByteBuffer float_InByteBuffer(ByteBuffer _bb) {
        return __vector_in_bytebuffer(_bb, 4, 4);
    }

    public static int createDynamicFloatTrack(FlatBufferBuilder builder,
                                              int float_Offset) {
        builder.startTable(1);
        DynamicFloatTrack.addFloat(builder, float_Offset);
        return DynamicFloatTrack.endDynamicFloatTrack(builder);
    }

    public static void startDynamicFloatTrack(FlatBufferBuilder builder) {
        builder.startTable(1);
    }

    public static void addFloat(FlatBufferBuilder builder, int float_Offset) {
        builder.addOffset(0, float_Offset, 0);
    }

    public static int createFloatVector(FlatBufferBuilder builder, float[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) builder.addFloat(data[i]);
        return builder.endVector();
    }

    public static void startFloatVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static int endDynamicFloatTrack(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public DynamicFloatTrack get(int j) {
            return get(new DynamicFloatTrack(), j);
        }

        public DynamicFloatTrack get(DynamicFloatTrack obj, int j) {
            return obj.__assign(__indirect(__element(j), bb), bb);
        }
    }

    public DynamicFloatTrackT unpack() {
        DynamicFloatTrackT _o = new DynamicFloatTrackT();
        unpackTo(_o);
        return _o;
    }

    public void unpackTo(DynamicFloatTrackT _o) {
        float[] _oFloat = new float[float_Length()];
        for (int _j = 0; _j < float_Length(); ++_j) {
            _oFloat[_j] = float_(_j);
        }
        _o.setFloat(_oFloat);
    }

    public static int pack(FlatBufferBuilder builder, DynamicFloatTrackT _o) {
        if (_o == null) return 0;
        int _float_ = 0;
        if (_o.getFloat() != null) {
            _float_ = createFloatVector(builder, _o.getFloat());
        }
        return createDynamicFloatTrack(
                builder,
                _float_);
    }
}
