package gg.generations.rarecandy.pokeutils.tracs;// automatically generated by the FlatBuffers compiler, do not modify

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

public class ResourceLabelT {
  private String resourceName;
  private long resourceType;

  public String getResourceName() { return resourceName; }

  public void setResourceName(String resourceName) { this.resourceName = resourceName; }

  public long getResourceType() { return resourceType; }

  public void setResourceType(long resourceType) { this.resourceType = resourceType; }


  public ResourceLabelT() {
    this.resourceName = null;
    this.resourceType = 0L;
  }
}
