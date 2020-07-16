// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: io/harness/perpetualtask/ng_perpetualtask_service_client.proto

package io.harness.perpetualtask;

/**
 * Protobuf type {@code io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse}
 */
@javax.annotation.
Generated(value = "protoc", comments = "annotations:ObtainPerpetualTaskValidationDetailsResponse.java.pb.meta")
public final class ObtainPerpetualTaskValidationDetailsResponse extends com.google.protobuf
                                                                            .GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse)
    ObtainPerpetualTaskValidationDetailsResponseOrBuilder {
  private static final long serialVersionUID = 0L;
  // Use ObtainPerpetualTaskValidationDetailsResponse.newBuilder() to construct.
  private ObtainPerpetualTaskValidationDetailsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ObtainPerpetualTaskValidationDetailsResponse() {}

  @java.
  lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
    return new ObtainPerpetualTaskValidationDetailsResponse();
  }

  @java.
  lang.Override
  public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
    return this.unknownFields;
  }
  private ObtainPerpetualTaskValidationDetailsResponse(
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
            io.harness.delegate.TaskSetupAbstractions.Builder subBuilder = null;
            if (setupAbstractions_ != null) {
              subBuilder = setupAbstractions_.toBuilder();
            }
            setupAbstractions_ =
                input.readMessage(io.harness.delegate.TaskSetupAbstractions.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(setupAbstractions_);
              setupAbstractions_ = subBuilder.buildPartial();
            }

            break;
          }
          case 18: {
            io.harness.delegate.TaskDetails.Builder subBuilder = null;
            if (details_ != null) {
              subBuilder = details_.toBuilder();
            }
            details_ = input.readMessage(io.harness.delegate.TaskDetails.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(details_);
              details_ = subBuilder.buildPartial();
            }

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
    return io.harness.perpetualtask.NgPerpetualtaskServiceClient
        .internal_static_io_harness_perpetualtask_ObtainPerpetualTaskValidationDetailsResponse_descriptor;
  }

  @java.
  lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
    return io.harness.perpetualtask.NgPerpetualtaskServiceClient
        .internal_static_io_harness_perpetualtask_ObtainPerpetualTaskValidationDetailsResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse.class,
            io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse.Builder.class);
  }

  public static final int SETUP_ABSTRACTIONS_FIELD_NUMBER = 1;
  private io.harness.delegate.TaskSetupAbstractions setupAbstractions_;
  /**
   * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
   * @return Whether the setupAbstractions field is set.
   */
  public boolean hasSetupAbstractions() {
    return setupAbstractions_ != null;
  }
  /**
   * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
   * @return The setupAbstractions.
   */
  public io.harness.delegate.TaskSetupAbstractions getSetupAbstractions() {
    return setupAbstractions_ == null ? io.harness.delegate.TaskSetupAbstractions.getDefaultInstance()
                                      : setupAbstractions_;
  }
  /**
   * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
   */
  public io.harness.delegate.TaskSetupAbstractionsOrBuilder getSetupAbstractionsOrBuilder() {
    return getSetupAbstractions();
  }

  public static final int DETAILS_FIELD_NUMBER = 2;
  private io.harness.delegate.TaskDetails details_;
  /**
   * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
   * @return Whether the details field is set.
   */
  public boolean hasDetails() {
    return details_ != null;
  }
  /**
   * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
   * @return The details.
   */
  public io.harness.delegate.TaskDetails getDetails() {
    return details_ == null ? io.harness.delegate.TaskDetails.getDefaultInstance() : details_;
  }
  /**
   * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
   */
  public io.harness.delegate.TaskDetailsOrBuilder getDetailsOrBuilder() {
    return getDetails();
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
    if (setupAbstractions_ != null) {
      output.writeMessage(1, getSetupAbstractions());
    }
    if (details_ != null) {
      output.writeMessage(2, getDetails());
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1)
      return size;

    size = 0;
    if (setupAbstractions_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, getSetupAbstractions());
    }
    if (details_ != null) {
      size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getDetails());
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
    if (!(obj instanceof io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse)) {
      return super.equals(obj);
    }
    io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse other =
        (io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse) obj;

    if (hasSetupAbstractions() != other.hasSetupAbstractions())
      return false;
    if (hasSetupAbstractions()) {
      if (!getSetupAbstractions().equals(other.getSetupAbstractions()))
        return false;
    }
    if (hasDetails() != other.hasDetails())
      return false;
    if (hasDetails()) {
      if (!getDetails().equals(other.getDetails()))
        return false;
    }
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
    if (hasSetupAbstractions()) {
      hash = (37 * hash) + SETUP_ABSTRACTIONS_FIELD_NUMBER;
      hash = (53 * hash) + getSetupAbstractions().hashCode();
    }
    if (hasDetails()) {
      hash = (37 * hash) + DETAILS_FIELD_NUMBER;
      hash = (53 * hash) + getDetails().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseDelimitedFrom(
      java.io.InputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseDelimitedFrom(
      java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
      com.google.protobuf.CodedInputStream input) throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
  }
  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parseFrom(
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
  public static Builder newBuilder(io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse prototype) {
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
   * Protobuf type {@code io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse}
   */
  public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse)
      io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
      return io.harness.perpetualtask.NgPerpetualtaskServiceClient
          .internal_static_io_harness_perpetualtask_ObtainPerpetualTaskValidationDetailsResponse_descriptor;
    }

    @java.
    lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return io.harness.perpetualtask.NgPerpetualtaskServiceClient
          .internal_static_io_harness_perpetualtask_ObtainPerpetualTaskValidationDetailsResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse.class,
              io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse.Builder.class);
    }

    // Construct using io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse.newBuilder()
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
      if (setupAbstractionsBuilder_ == null) {
        setupAbstractions_ = null;
      } else {
        setupAbstractions_ = null;
        setupAbstractionsBuilder_ = null;
      }
      if (detailsBuilder_ == null) {
        details_ = null;
      } else {
        details_ = null;
        detailsBuilder_ = null;
      }
      return this;
    }

    @java.
    lang.Override
    public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
      return io.harness.perpetualtask.NgPerpetualtaskServiceClient
          .internal_static_io_harness_perpetualtask_ObtainPerpetualTaskValidationDetailsResponse_descriptor;
    }

    @java.
    lang.Override
    public io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse getDefaultInstanceForType() {
      return io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse.getDefaultInstance();
    }

    @java.
    lang.Override
    public io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse build() {
      io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.
    lang.Override
    public io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse buildPartial() {
      io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse result =
          new io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse(this);
      if (setupAbstractionsBuilder_ == null) {
        result.setupAbstractions_ = setupAbstractions_;
      } else {
        result.setupAbstractions_ = setupAbstractionsBuilder_.build();
      }
      if (detailsBuilder_ == null) {
        result.details_ = details_;
      } else {
        result.details_ = detailsBuilder_.build();
      }
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
      if (other instanceof io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse) {
        return mergeFrom((io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse) other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse other) {
      if (other == io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse.getDefaultInstance())
        return this;
      if (other.hasSetupAbstractions()) {
        mergeSetupAbstractions(other.getSetupAbstractions());
      }
      if (other.hasDetails()) {
        mergeDetails(other.getDetails());
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
      io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage =
            (io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private io.harness.delegate.TaskSetupAbstractions setupAbstractions_;
    private com.google.protobuf.SingleFieldBuilderV3<io.harness.delegate.TaskSetupAbstractions,
        io.harness.delegate.TaskSetupAbstractions.Builder, io.harness.delegate.TaskSetupAbstractionsOrBuilder>
        setupAbstractionsBuilder_;
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     * @return Whether the setupAbstractions field is set.
     */
    public boolean hasSetupAbstractions() {
      return setupAbstractionsBuilder_ != null || setupAbstractions_ != null;
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     * @return The setupAbstractions.
     */
    public io.harness.delegate.TaskSetupAbstractions getSetupAbstractions() {
      if (setupAbstractionsBuilder_ == null) {
        return setupAbstractions_ == null ? io.harness.delegate.TaskSetupAbstractions.getDefaultInstance()
                                          : setupAbstractions_;
      } else {
        return setupAbstractionsBuilder_.getMessage();
      }
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     */
    public Builder setSetupAbstractions(io.harness.delegate.TaskSetupAbstractions value) {
      if (setupAbstractionsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        setupAbstractions_ = value;
        onChanged();
      } else {
        setupAbstractionsBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     */
    public Builder setSetupAbstractions(io.harness.delegate.TaskSetupAbstractions.Builder builderForValue) {
      if (setupAbstractionsBuilder_ == null) {
        setupAbstractions_ = builderForValue.build();
        onChanged();
      } else {
        setupAbstractionsBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     */
    public Builder mergeSetupAbstractions(io.harness.delegate.TaskSetupAbstractions value) {
      if (setupAbstractionsBuilder_ == null) {
        if (setupAbstractions_ != null) {
          setupAbstractions_ =
              io.harness.delegate.TaskSetupAbstractions.newBuilder(setupAbstractions_).mergeFrom(value).buildPartial();
        } else {
          setupAbstractions_ = value;
        }
        onChanged();
      } else {
        setupAbstractionsBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     */
    public Builder clearSetupAbstractions() {
      if (setupAbstractionsBuilder_ == null) {
        setupAbstractions_ = null;
        onChanged();
      } else {
        setupAbstractions_ = null;
        setupAbstractionsBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     */
    public io.harness.delegate.TaskSetupAbstractions.Builder getSetupAbstractionsBuilder() {
      onChanged();
      return getSetupAbstractionsFieldBuilder().getBuilder();
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     */
    public io.harness.delegate.TaskSetupAbstractionsOrBuilder getSetupAbstractionsOrBuilder() {
      if (setupAbstractionsBuilder_ != null) {
        return setupAbstractionsBuilder_.getMessageOrBuilder();
      } else {
        return setupAbstractions_ == null ? io.harness.delegate.TaskSetupAbstractions.getDefaultInstance()
                                          : setupAbstractions_;
      }
    }
    /**
     * <code>.io.harness.delegate.TaskSetupAbstractions setup_abstractions = 1[json_name = "setupAbstractions"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<io.harness.delegate.TaskSetupAbstractions,
        io.harness.delegate.TaskSetupAbstractions.Builder, io.harness.delegate.TaskSetupAbstractionsOrBuilder>
    getSetupAbstractionsFieldBuilder() {
      if (setupAbstractionsBuilder_ == null) {
        setupAbstractionsBuilder_ =
            new com.google.protobuf.SingleFieldBuilderV3<io.harness.delegate.TaskSetupAbstractions,
                io.harness.delegate.TaskSetupAbstractions.Builder, io.harness.delegate.TaskSetupAbstractionsOrBuilder>(
                getSetupAbstractions(), getParentForChildren(), isClean());
        setupAbstractions_ = null;
      }
      return setupAbstractionsBuilder_;
    }

    private io.harness.delegate.TaskDetails details_;
    private com.google.protobuf.SingleFieldBuilderV3<io.harness.delegate.TaskDetails,
        io.harness.delegate.TaskDetails.Builder, io.harness.delegate.TaskDetailsOrBuilder> detailsBuilder_;
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     * @return Whether the details field is set.
     */
    public boolean hasDetails() {
      return detailsBuilder_ != null || details_ != null;
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     * @return The details.
     */
    public io.harness.delegate.TaskDetails getDetails() {
      if (detailsBuilder_ == null) {
        return details_ == null ? io.harness.delegate.TaskDetails.getDefaultInstance() : details_;
      } else {
        return detailsBuilder_.getMessage();
      }
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     */
    public Builder setDetails(io.harness.delegate.TaskDetails value) {
      if (detailsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        details_ = value;
        onChanged();
      } else {
        detailsBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     */
    public Builder setDetails(io.harness.delegate.TaskDetails.Builder builderForValue) {
      if (detailsBuilder_ == null) {
        details_ = builderForValue.build();
        onChanged();
      } else {
        detailsBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     */
    public Builder mergeDetails(io.harness.delegate.TaskDetails value) {
      if (detailsBuilder_ == null) {
        if (details_ != null) {
          details_ = io.harness.delegate.TaskDetails.newBuilder(details_).mergeFrom(value).buildPartial();
        } else {
          details_ = value;
        }
        onChanged();
      } else {
        detailsBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     */
    public Builder clearDetails() {
      if (detailsBuilder_ == null) {
        details_ = null;
        onChanged();
      } else {
        details_ = null;
        detailsBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     */
    public io.harness.delegate.TaskDetails.Builder getDetailsBuilder() {
      onChanged();
      return getDetailsFieldBuilder().getBuilder();
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     */
    public io.harness.delegate.TaskDetailsOrBuilder getDetailsOrBuilder() {
      if (detailsBuilder_ != null) {
        return detailsBuilder_.getMessageOrBuilder();
      } else {
        return details_ == null ? io.harness.delegate.TaskDetails.getDefaultInstance() : details_;
      }
    }
    /**
     * <code>.io.harness.delegate.TaskDetails details = 2[json_name = "details"];</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<io.harness.delegate.TaskDetails,
        io.harness.delegate.TaskDetails.Builder, io.harness.delegate.TaskDetailsOrBuilder>
    getDetailsFieldBuilder() {
      if (detailsBuilder_ == null) {
        detailsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<io.harness.delegate.TaskDetails,
            io.harness.delegate.TaskDetails.Builder, io.harness.delegate.TaskDetailsOrBuilder>(
            getDetails(), getParentForChildren(), isClean());
        details_ = null;
      }
      return detailsBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }

    // @@protoc_insertion_point(builder_scope:io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse)
  }

  // @@protoc_insertion_point(class_scope:io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse)
  private static final io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse();
  }

  public static io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ObtainPerpetualTaskValidationDetailsResponse> PARSER =
      new com.google.protobuf.AbstractParser<ObtainPerpetualTaskValidationDetailsResponse>() {
        @java.lang.Override
        public ObtainPerpetualTaskValidationDetailsResponse parsePartialFrom(
            com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          return new ObtainPerpetualTaskValidationDetailsResponse(input, extensionRegistry);
        }
      };

  public static com.google.protobuf.Parser<ObtainPerpetualTaskValidationDetailsResponse> parser() {
    return PARSER;
  }

  @java.
  lang.Override
  public com.google.protobuf.Parser<ObtainPerpetualTaskValidationDetailsResponse> getParserForType() {
    return PARSER;
  }

  @java.
  lang.Override
  public io.harness.perpetualtask.ObtainPerpetualTaskValidationDetailsResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }
}
