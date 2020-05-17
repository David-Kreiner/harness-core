package software.wings.delegatetasks.aws;

import static io.harness.beans.ExecutionStatus.SUCCESS;
import static software.wings.service.impl.aws.model.AwsS3Request.AwsS3RequestType.LIST_BUCKET_NAMES;

import com.google.inject.Inject;

import io.harness.delegate.beans.DelegateTaskResponse;
import io.harness.delegate.task.TaskParameters;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.WingsException;
import org.apache.commons.lang3.NotImplementedException;
import software.wings.beans.DelegateTaskPackage;
import software.wings.delegatetasks.AbstractDelegateRunnableTask;
import software.wings.service.impl.aws.model.AwsResponse;
import software.wings.service.impl.aws.model.AwsS3ListBucketNamesResponse;
import software.wings.service.impl.aws.model.AwsS3Request;
import software.wings.service.impl.aws.model.AwsS3Request.AwsS3RequestType;
import software.wings.service.intfc.aws.delegate.AwsS3HelperServiceDelegate;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AwsS3Task extends AbstractDelegateRunnableTask {
  @Inject private AwsS3HelperServiceDelegate s3HelperServiceDelegate;

  public AwsS3Task(
      DelegateTaskPackage delegateTaskPackage, Consumer<DelegateTaskResponse> consumer, Supplier<Boolean> preExecute) {
    super(delegateTaskPackage, consumer, preExecute);
  }

  @Override
  public AwsResponse run(TaskParameters parameters) {
    AwsS3Request request = (AwsS3Request) parameters;
    try {
      AwsS3RequestType requestType = request.getRequestType();
      if (LIST_BUCKET_NAMES == requestType) {
        List<String> bucketNames =
            s3HelperServiceDelegate.listBucketNames(request.getAwsConfig(), request.getEncryptionDetails());
        return AwsS3ListBucketNamesResponse.builder().bucketNames(bucketNames).executionStatus(SUCCESS).build();
      } else {
        throw new InvalidRequestException("Invalid request type [" + requestType + "]", WingsException.USER);
      }
    } catch (WingsException exception) {
      throw exception;
    } catch (Exception ex) {
      throw new InvalidRequestException(ex.getMessage(), WingsException.USER);
    }
  }

  @Override
  public AwsResponse run(Object[] parameters) {
    throw new NotImplementedException("not implemented");
  }
}