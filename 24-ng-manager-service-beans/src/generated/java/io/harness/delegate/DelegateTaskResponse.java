// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/delegate/delegate_task_response.proto

package io.harness.delegate;

@javax.annotation.Generated(value = "protoc", comments = "annotations:DelegateTaskResponse.java.pb.meta")
public final class DelegateTaskResponse {
  private DelegateTaskResponse() {}
  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_delegate_SendTaskResultRequest_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_delegate_SendTaskResultRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors
      .Descriptor internal_static_io_harness_delegate_SendTaskResultResponse_descriptor;
  static final com.google.protobuf.GeneratedMessageV3
      .FieldAccessorTable internal_static_io_harness_delegate_SendTaskResultResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
  static {
    java.lang.String[] descriptorData = {"\n0io/harness/delegate/delegate_task_resp"
        + "onse.proto\022\023io.harness.delegate\032\036io/harn"
        + "ess/delegate/task.proto\"r\n\025SendTaskResul"
        + "tRequest\0224\n\007task_id\030\001 \001(\0132\033.io.harness.d"
        + "elegate.TaskIdR\006taskId\022#\n\rresponse_data\030"
        + "\002 \001(\014R\014responseData\"B\n\026SendTaskResultRes"
        + "ponse\022(\n\017acknowledgement\030\001 \001(\010R\017acknowle"
        + "dgementB\002P\001b\006proto3"};
    descriptor = com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
            io.harness.delegate.Task.getDescriptor(),
        });
    internal_static_io_harness_delegate_SendTaskResultRequest_descriptor = getDescriptor().getMessageTypes().get(0);
    internal_static_io_harness_delegate_SendTaskResultRequest_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_delegate_SendTaskResultRequest_descriptor,
            new java.lang.String[] {
                "TaskId",
                "ResponseData",
            });
    internal_static_io_harness_delegate_SendTaskResultResponse_descriptor = getDescriptor().getMessageTypes().get(1);
    internal_static_io_harness_delegate_SendTaskResultResponse_fieldAccessorTable =
        new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
            internal_static_io_harness_delegate_SendTaskResultResponse_descriptor,
            new java.lang.String[] {
                "Acknowledgement",
            });
    io.harness.delegate.Task.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
