// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/delegate/ng_delegate_task_service.proto

package io.harness.delegate;

@javax.annotation.Generated(value = "protoc", comments = "annotations:NgDelegateTaskServiceOuterClass.java.pb.meta")
public final class NgDelegateTaskServiceOuterClass {
  private NgDelegateTaskServiceOuterClass() {}
  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {}

  public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
  static {
    java.lang.String[] descriptorData = {"\n2io/harness/delegate/ng_delegate_task_s"
        + "ervice.proto\022\023io.harness.delegate\032*io/ha"
        + "rness/delegate/ng_delegate_task.proto\0320i"
        + "o/harness/perpetualtask/ng_perpetual_tas"
        + "k.proto2\366\005\n\025NgDelegateTaskService\022W\n\010Sen"
        + "dTask\022$.io.harness.delegate.SendTaskRequ"
        + "est\032%.io.harness.delegate.SendTaskRespon"
        + "se\022f\n\rSendTaskAsync\022).io.harness.delegat"
        + "e.SendTaskAsyncRequest\032*.io.harness.dele"
        + "gate.SendTaskAsyncResponse\022Z\n\tAbortTask\022"
        + "%.io.harness.delegate.AbortTaskRequest\032&"
        + ".io.harness.delegate.AbortTaskResponse\022\224"
        + "\001\n\031CreateRemotePerpetualTask\022:.io.harnes"
        + "s.perpetualtask.CreateRemotePerpetualTas"
        + "kRequest\032;.io.harness.perpetualtask.Crea"
        + "teRemotePerpetualTaskResponse\022\224\001\n\031Delete"
        + "RemotePerpetualTask\022:.io.harness.perpetu"
        + "altask.DeleteRemotePerpetualTaskRequest\032"
        + ";.io.harness.perpetualtask.DeleteRemoteP"
        + "erpetualTaskResponse\022\221\001\n\030ResetRemotePerp"
        + "etualTask\0229.io.harness.perpetualtask.Res"
        + "etRemotePerpetualTaskRequest\032:.io.harnes"
        + "s.perpetualtask.ResetRemotePerpetualTask"
        + "ResponseB\002P\001b\006proto3"};
    descriptor = com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
            io.harness.delegate.NgDelegateTask.getDescriptor(),
            io.harness.perpetualtask.NgPerpetualTask.getDescriptor(),
        });
    io.harness.delegate.NgDelegateTask.getDescriptor();
    io.harness.perpetualtask.NgPerpetualTask.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
