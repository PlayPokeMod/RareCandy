// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.pokeutils.tranm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class BoneInit extends Table {
    public static void ValidateVersion() {
        Constants.FLATBUFFERS_23_5_26();
    }

    public static BoneInit getRootAsBoneInit(ByteBuffer _bb) {
        return getRootAsBoneInit(_bb, new BoneInit());
    }

    public static BoneInit getRootAsBoneInit(ByteBuffer _bb, BoneInit obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb));
    }

    public static void startBoneInit(FlatBufferBuilder builder) {
        builder.startTable(2);
    }

    public static void addIsInit(FlatBufferBuilder builder, long isInit) {
        builder.addInt(0, (int) isInit, (int) 0L);
    }

    public static void addTransform(FlatBufferBuilder builder, int transformOffset) {
        builder.addStruct(1, transformOffset, 0);
    }

    public static int endBoneInit(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public BoneInit __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long isInit() {
        int o = __offset(4);
        return o != 0 ? (long) bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L;
    }

    public Transform transform() {
        return transform(new Transform());
    }

    public Transform transform(Transform obj) {
        int o = __offset(6);
        return o != 0 ? obj.__assign(o + bb_pos, bb) : null;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public BoneInit get(int j) {
            return get(new BoneInit(), j);
        }

        public BoneInit get(BoneInit obj, int j) {
            return obj.__assign(Table.__indirect(__element(j), bb), bb);
        }
    }
}

