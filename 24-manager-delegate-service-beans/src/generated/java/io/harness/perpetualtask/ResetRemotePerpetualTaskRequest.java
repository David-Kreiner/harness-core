// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/perpetualtask/ng_perpetual_task.proto

package io.harness.perpetualtask;

/**
 * Protobuf type {@code io.harness.perpetualtask.ResetRemotePerpetualTaskRequest}
 */
@javax.annotation.Generated(value = "protoc", comments = "annotations:ResetRemotePerpetualTaskRequest.java.pb.meta")
public final class ResetRemotePerpetualTaskRequest extends com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:io.harness.perpetualtask.ResetRemotePerpetualTaskRequest)
    ResetRemotePerpetualTaskRequestOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use ResetRemotePerpetualTaskRequest.newBuilder() to construct.
  private ResetRemotePerpetualTaskRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ResetRemotePerpetualTaskRequest() {
    accountId_ = "";
    perpetualTaskId_ = "";
  }

  @java.
  lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
    return new ResetRemotePerpetualTaskRequest();
  }

  @java.
  lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }
  private ResetRemotePerpetualTaskRequest(
      com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            accountId_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            perpetualTaskId_ = s;
            break;
          }
          default: {
            if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
    return io.harness.perpetualtask.NgPerpetualTask
        .internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_descriptor;
  }

  @java.
  lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
    return io.harness.perpetualtask.NgPerpetualTask
        .internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(io.harness.perpetualtask.ResetRemotePerpetualTaskRequest.class,
            io.harness.perpetualtask.ResetRemotePerpetualTaskRequest.Builder.class);
  }

  public static final int ACCOUNT_ID_FIELD_NUMBER = 1;
  private volatile java.lang.Object accountId_;
  /**
   * <code>string account_id = 1[json_name = "accountId"];</code>
   * @return The accountId.
   */
  public java.lang.String getAccountId() {
    java.lang.Object ref = accountId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      accountId_ = s;
      return s;
    }
  }
  /**
   * <code>string account_id = 1[json_name = "accountId"];</code>
   * @return The bytes for accountId.
   */
  public com.google.protobuf.ByteString getAccountIdBytes() {
    java.lang.Object ref = accountId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      accountId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PERPETUAL_TASK_ID_FIELD_NUMBER = 2;
  private volatile java.lang.Object perpetualTaskId_;
  /**
   * <code>string perpetual_task_id = 2[json_name = "perpetualTaskId"];</code>
   * @return The perpetualTaskId.
   */
  public java.lang.String getPerpetualTaskId() {
    java.lang.Object ref = perpetualTaskId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      perpetualTaskId_ = s;
      return s;
    }
  }
  /**
   * <code>string perpetual_task_id = 2[json_name = "perpetualTaskId"];</code>
   * @return The bytes for perpetualTaskId.
   */
  public com.google.protobuf.ByteString getPerpetualTaskIdBytes() {
    java.lang.Object ref = perpetualTaskId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
      perpetualTaskId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1)
      return true;
    if (isInitialized == 0)
      return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
    if (!getAccountIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, accountId_);
    }
    if (!getPerpetualTaskIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, perpetualTaskId_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1)
      return size;

    size = 0;
    if (!getAccountIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, accountId_);
    }
    if (!getPerpetualTaskIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, perpetualTaskId_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof io.harness.perpetualtask.ResetRemotePerpetualTaskRequest)) {
      return super.equals(obj);
    }
    io.harness.perpetualtask.ResetRemotePerpetualTaskRequest other =
        (io.harness.perpetualtask.ResetRemotePerpetualTaskRequest) obj;

    if (!getAccountId().equals(other.getAccountId()))
      return false;
    if (!getPerpetualTaskId().equals(other.getPerpetualTaskId()))
      return false;
    if (!unknownFields.equals(other.unknownFields))
      return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + ACCOUNT_ID_FIELD_NUMBER;
    hash = (53 * hash) + getAccountId().hashCode();
    hash = (37 * hash) + PERPETUAL_TASK_ID_FIELD_NUMBER;
    hash = (53 * hash) + getPerpetualTaskId().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(
      com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseDelimitedFrom(java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }
  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parseFrom(
      com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() {
    return newBuilder();
  }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(io.harness.perpetualtask.ResetRemotePerpetualTaskRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code io.harness.perpetualtask.ResetRemotePerpetualTaskRequest}
   */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:io.harness.perpetualtask.ResetRemotePerpetualTaskRequest)
      io.harness.perpetualtask.ResetRemotePerpetualTaskRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return io.harness.perpetualtask.NgPerpetualTask
          .internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_descriptor;
    }

    @java.
    lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return io.harness.perpetualtask.NgPerpetualTask
          .internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(io.harness.perpetualtask.ResetRemotePerpetualTaskRequest.class,
              io.harness.perpetualtask.ResetRemotePerpetualTaskRequest.Builder.class);
    }

    // Construct using io.harness.perpetualtask.ResetRemotePerpetualTaskRequest.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      accountId_ = "";

      perpetualTaskId_ = "";

      return this;
    }

    @java.
    lang.Override
    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return io.harness.perpetualtask.NgPerpetualTask
          .internal_static_io_harness_perpetualtask_ResetRemotePerpetualTaskRequest_descriptor;
    }

    @java.
    lang.Override
    public io.harness.perpetualtask.ResetRemotePerpetualTaskRequest getDefaultInstanceForType() {
      return io.harness.perpetualtask.ResetRemotePerpetualTaskRequest.getDefaultInstance();
    }

    @java.
    lang.Override
    public io.harness.perpetualtask.ResetRemotePerpetualTaskRequest build() {
      io.harness.perpetualtask.ResetRemotePerpetualTaskRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.
    lang.Override
    public io.harness.perpetualtask.ResetRemotePerpetualTaskRequest buildPartial() {
      io.harness.perpetualtask.ResetRemotePerpetualTaskRequest result =
          new io.harness.perpetualtask.ResetRemotePerpetualTaskRequest(this);
      result.accountId_ = accountId_;
      result.perpetualTaskId_ = perpetualTaskId_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field, int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof io.harness.perpetualtask.ResetRemotePerpetualTaskRequest) {
        return mergeFrom((io.harness.perpetualtask.ResetRemotePerpetualTaskRequest) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.harness.perpetualtask.ResetRemotePerpetualTaskRequest other) {
      if (other == io.harness.perpetualtask.ResetRemotePerpetualTaskRequest.getDefaultInstance())
        return this;
      if (!other.getAccountId().isEmpty()) {
        accountId_ = other.accountId_;
        onChanged();
      }
      if (!other.getPerpetualTaskId().isEmpty()) {
        perpetualTaskId_ = other.perpetualTaskId_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
      io.harness.perpetualtask.ResetRemotePerpetualTaskRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (io.harness.perpetualtask.ResetRemotePerpetualTaskRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object accountId_ = "";
    /**
     * <code>string account_id = 1[json_name = "accountId"];</code>
     * @return The accountId.
     */
    public java.lang.String getAccountId() {
      java.lang.Object ref = accountId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        accountId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string account_id = 1[json_name = "accountId"];</code>
     * @return The bytes for accountId.
     */
    public com.google.protobuf.ByteString getAccountIdBytes() {
      java.lang.Object ref = accountId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        accountId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string account_id = 1[json_name = "accountId"];</code>
     * @param value The accountId to set.
     * @return This builder for chaining.
     */
    public Builder setAccountId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      accountId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string account_id = 1[json_name = "accountId"];</code>
     * @return This builder for chaining.
     */
    public Builder clearAccountId() {
      accountId_ = getDefaultInstance().getAccountId();
      onChanged();
      return this;
    }
    /**
     * <code>string account_id = 1[json_name = "accountId"];</code>
     * @param value The bytes for accountId to set.
     * @return This builder for chaining.
     */
    public Builder setAccountIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      accountId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object perpetualTaskId_ = "";
    /**
     * <code>string perpetual_task_id = 2[json_name = "perpetualTaskId"];</code>
     * @return The perpetualTaskId.
     */
    public java.lang.String getPerpetualTaskId() {
      java.lang.Object ref = perpetualTaskId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        perpetualTaskId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string perpetual_task_id = 2[json_name = "perpetualTaskId"];</code>
     * @return The bytes for perpetualTaskId.
     */
    public com.google.protobuf.ByteString getPerpetualTaskIdBytes() {
      java.lang.Object ref = perpetualTaskId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
        perpetualTaskId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string perpetual_task_id = 2[json_name = "perpetualTaskId"];</code>
     * @param value The perpetualTaskId to set.
     * @return This builder for chaining.
     */
    public Builder setPerpetualTaskId(java.lang.String value) {
      if (value == null) {
        throw new NullPointerException();
      }

      perpetualTaskId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string perpetual_task_id = 2[json_name = "perpetualTaskId"];</code>
     * @return This builder for chaining.
     */
    public Builder clearPerpetualTaskId() {
      perpetualTaskId_ = getDefaultInstance().getPerpetualTaskId();
      onChanged();
      return this;
    }
    /**
     * <code>string perpetual_task_id = 2[json_name = "perpetualTaskId"];</code>
     * @param value The bytes for perpetualTaskId to set.
     * @return This builder for chaining.
     */
    public Builder setPerpetualTaskIdBytes(com.google.protobuf.ByteString value) {
      if (value == null) {
        throw new NullPointerException();
      }
      checkByteStringIsUtf8(value);

      perpetualTaskId_ = value;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:io.harness.perpetualtask.ResetRemotePerpetualTaskRequest)
  }

  // @@protoc_insertion_point(class_scope:io.harness.perpetualtask.ResetRemotePerpetualTaskRequest)
  private static final io.harness.perpetualtask.ResetRemotePerpetualTaskRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.harness.perpetualtask.ResetRemotePerpetualTaskRequest();
  }

  public static io.harness.perpetualtask.ResetRemotePerpetualTaskRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ResetRemotePerpetualTaskRequest> PARSER =
      new com.google.protobuf.AbstractParser<ResetRemotePerpetualTaskRequest>() {
        @java.lang.Override
        public ResetRemotePerpetualTaskRequest parsePartialFrom(
            com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new ResetRemotePerpetualTaskRequest(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<ResetRemotePerpetualTaskRequest> parser() {
    return PARSER;
  }

  @java.
  lang.Override
  public com.google.protobuf.Parser<ResetRemotePerpetualTaskRequest> getParserForType() {
    return PARSER;
  }

  @java.
  lang.Override
  public io.harness.perpetualtask.ResetRemotePerpetualTaskRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
