// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/perpetualtask/ng_peprpetual_task_client.proto

package io.harness.perpetualtask;

@javax.annotation.Generated(value = "protoc", comments = "annotations:NgPeprpetualTaskClient.java.pb.meta")
public final class NgPeprpetualTaskClient {
  private NgPeprpetualTaskClient() {}
  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
      internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_TaskClientParamsEntry_descriptor;
  static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_TaskClientParamsEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
  static {
    java.lang.String[] descriptorData = {"\n8io/harness/perpetualtask/ng_peprpetual"
        + "_task_client.proto\022\030io.harness.perpetual"
        + "task\032\037google/protobuf/timestamp.proto\"\265\002"
        + "\n RemotePerpetualTaskClientContext\022~\n\022ta"
        + "sk_client_params\030\001 \003(\0132P.io.harness.perp"
        + "etualtask.RemotePerpetualTaskClientConte"
        + "xt.TaskClientParamsEntryR\020taskClientPara"
        + "ms\022L\n\024last_context_updated\030\002 \001(\0132\032.googl"
        + "e.protobuf.TimestampR\022lastContextUpdated"
        + "\032C\n\025TaskClientParamsEntry\022\020\n\003key\030\001 \001(\tR\003"
        + "key\022\024\n\005value\030\002 \001(\tR\005value:\0028\001B\002P\001b\006proto"
        + "3"};
    descriptor = com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
            com.google.protobuf.TimestampProto.getDescriptor(),
        });
    internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_descriptor =
        getDescriptor().getMessageTypes().get(0);
    internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_descriptor,
            new java.lang.String[] {
                "TaskClientParams",
                "LastContextUpdated",
            });
    internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_TaskClientParamsEntry_descriptor =
        internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_descriptor.getNestedTypes().get(0);
    internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_TaskClientParamsEntry_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_RemotePerpetualTaskClientContext_TaskClientParamsEntry_descriptor,
            new java.lang.String[] {
                "Key",
                "Value",
            });
    com.google.protobuf.TimestampProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
