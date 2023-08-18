// automatically generated by the FlatBuffers compiler, do not modify

package gg.generations.pokeutils.tranm;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public final class Vec3s extends Struct {
    public static int createVec3s(FlatBufferBuilder builder, int x, int y, int z) {
        builder.prep(2, 6);
        builder.putShort((short) z);
        builder.putShort((short) y);
        builder.putShort((short) x);
        return builder.offset();
    }

    public void __init(int _i, ByteBuffer _bb) {
        __reset(_i, _bb);
    }

    public Vec3s __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public int x() {
        return bb.getShort(bb_pos) & 0xFFFF;
    }

    public int y() {
        return bb.getShort(bb_pos + 2) & 0xFFFF;
    }

    public int z() {
        return bb.getShort(bb_pos + 4) & 0xFFFF;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) {
            __reset(_vector, _element_size, _bb);
            return this;
        }

        public Vec3s get(int j) {
            return get(new Vec3s(), j);
        }

        public Vec3s get(Vec3s obj, int j) {
            return obj.__assign(__element(j), bb);
        }
    }
}

