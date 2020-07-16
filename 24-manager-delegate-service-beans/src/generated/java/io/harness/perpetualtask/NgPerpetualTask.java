// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/perpetualtask/ng_perpetual_task.proto

package io.harness.perpetualtask;

@javax.annotation.Generated(value = "protoc", comments = "annotations:NgPerpetualTask.java.pb.meta")
public final class NgPerpetualTask {
  private NgPerpetualTask() {}
  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_RemotePerpetualTaskSchedule_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_RemotePerpetualTaskSchedule_fieldAccessorTable;
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskRequest_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskResponse_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskRequest_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskResponse_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskResponse_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
  static {
    java.lang.String[] descriptorData = {"\n0io/harness/perpetualtask/ng_perpetual_"
        + "task.proto\022\030io.harness.perpetualtask\0328io"
        + "/harness/perpetualtask/ng_peprpetual_tas"
        + "k_client.proto\032\036google/protobuf/duration"
        + ".proto\"\211\001\n\033RemotePerpetualTaskSchedule\0225"
        + "\n\010interval\030\001 \001(\0132\031.google.protobuf.Durat"
        + "ionR\010interval\0223\n\007timeout\030\002 \001(\0132\031.google."
        + "protobuf.DurationR\007timeout\"\260\002\n CreateRem"
        + "otePerpetualTaskRequest\022\035\n\naccount_id\030\001 "
        + "\001(\tR\taccountId\022\033\n\ttask_type\030\002 \001(\tR\010taskT"
        + "ype\022Q\n\010schedule\030\003 \001(\01325.io.harness.perpe"
        + "tualtask.RemotePerpetualTaskScheduleR\010sc"
        + "hedule\022T\n\007context\030\004 \001(\0132:.io.harness.per"
        + "petualtask.RemotePerpetualTaskClientCont"
        + "extR\007context\022\'\n\017allow_duplicate\030\005 \001(\010R\016a"
        + "llowDuplicate\"O\n!CreateRemotePerpetualTa"
        + "skResponse\022*\n\021perpetual_task_id\030\001 \001(\tR\017p"
        + "erpetualTaskId\"m\n DeleteRemotePerpetualT"
        + "askRequest\022\035\n\naccount_id\030\001 \001(\tR\taccountI"
        + "d\022*\n\021perpetual_task_id\030\002 \001(\tR\017perpetualT"
        + "askId\"=\n!DeleteRemotePerpetualTaskRespon"
        + "se\022\030\n\007success\030\001 \001(\010R\007success\"l\n\037ResetRem"
        + "otePerpetualTaskRequest\022\035\n\naccount_id\030\001 "
        + "\001(\tR\taccountId\022*\n\021perpetual_task_id\030\002 \001("
        + "\tR\017perpetualTaskId\"<\n ResetRemotePerpetu"
        + "alTaskResponse\022\030\n\007success\030\001 \001(\010R\007success"
        + "B\002P\001b\006proto3"};
    descriptor = com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
            io.harness.perpetualtask.NgPeprpetualTaskClient.getDescriptor(),
            com.google.protobuf.DurationProto.getDescriptor(),
        });
    internal_static_io_harness_perpetualtask_RemotePerpetualTaskSchedule_descriptor =
        getDescriptor().getMessageTypes().get(0);
    internal_static_io_harness_perpetualtask_RemotePerpetualTaskSchedule_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_RemotePerpetualTaskSchedule_descriptor,
            new java.lang.String[] {
                "Interval",
                "Timeout",
            });
    internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskRequest_descriptor =
        getDescriptor().getMessageTypes().get(1);
    internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskRequest_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskRequest_descriptor,
            new java.lang.String[] {
                "AccountId",
                "TaskType",
                "Schedule",
                "Context",
                "AllowDuplicate",
            });
    internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskResponse_descriptor =
        getDescriptor().getMessageTypes().get(2);
    internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskResponse_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_CreateRemotePerpetualTaskResponse_descriptor,
            new java.lang.String[] {
                "PerpetualTaskId",
            });
    internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskRequest_descriptor =
        getDescriptor().getMessageTypes().get(3);
    internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskRequest_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskRequest_descriptor,
            new java.lang.String[] {
                "AccountId",
                "PerpetualTaskId",
            });
    internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskResponse_descriptor =
        getDescriptor().getMessageTypes().get(4);
    internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskResponse_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_DeleteRemotePerpetualTaskResponse_descriptor,
            new java.lang.String[] {
                "Success",
            });
    internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_descriptor =
        getDescriptor().getMessageTypes().get(5);
    internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_descriptor,
            new java.lang.String[] {
                "AccountId",
                "PerpetualTaskId",
            });
    internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskResponse_descriptor =
        getDescriptor().getMessageTypes().get(6);
    internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskResponse_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskResponse_descriptor,
            new java.lang.String[] {
                "Success",
            });
    io.harness.perpetualtask.NgPeprpetualTaskClient.getDescriptor();
    com.google.protobuf.DurationProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
