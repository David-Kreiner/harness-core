package io.harness.perpetualtask;

import static io.harness.rule.OwnerRule.ROHIT_KUMAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Any;
import com.google.protobuf.Message;

import io.harness.CategoryTest;
import io.harness.NgManagerServiceDriver;
import io.harness.beans.DelegateTask;
import io.harness.category.element.UnitTests;
import io.harness.delegate.TaskDetails;
import io.harness.delegate.TaskMode;
import io.harness.delegate.TaskSetupAbstractions;
import io.harness.delegate.TaskType;
import io.harness.perpetualtask.example.SamplePerpetualTaskParams;
import io.harness.rule.Owner;
import io.harness.serializer.KryoSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ForwardingPerpetualTaskServiceClientTest extends CategoryTest {
  public static final String ACCOUNT_ID = "accountId";

  @Mock private NgManagerServiceDriver ngManagerServiceDriver;
  @Mock private KryoSerializer kryoSerializer;
  @InjectMocks
  ForwardingPerpetualTaskServiceClient forwardingPerpetualTaskServiceClient =
      new ForwardingPerpetualTaskServiceClient("task_type", "ng-manager");

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  @Owner(developers = ROHIT_KUMAR)
  @Category(UnitTests.class)
  public void getTaskParams() {
    doReturn(ObtainPerpetualTaskExecutionParamsResponse.newBuilder()
                 .setCustomizedParams(Any.pack(SamplePerpetualTaskParams.newBuilder().setCountry("country").build()))
                 .build())
        .when(ngManagerServiceDriver)
        .obtainPerpetualTaskExecutionParams(any());

    final Message taskParams = forwardingPerpetualTaskServiceClient.getTaskParams(
        PerpetualTaskClientContext.builder().clientParams(ImmutableMap.of("key", "value")).build());

    assertThat(taskParams).isInstanceOf(SamplePerpetualTaskParams.class);
    assertThat(((SamplePerpetualTaskParams) taskParams).getCountry()).isEqualTo("country");
  }

  @Test
  @Owner(developers = ROHIT_KUMAR)
  @Category(UnitTests.class)
  public void onTaskStateChange() {
    final PerpetualTaskResponse newPerpetualTaskResponse =
        PerpetualTaskResponse.builder()
            .perpetualTaskState(PerpetualTaskState.TASK_RUN_SUCCEEDED)
            .responseCode(100)
            .build();
    final PerpetualTaskResponse oldTaskResponse = PerpetualTaskResponse.builder()
                                                      .perpetualTaskState(PerpetualTaskState.TASK_ASSIGNED)
                                                      .responseCode(200)
                                                      .responseMessage("old task")
                                                      .build();

    doReturn(ReportPerpetualTaskStateChangeResponse.newBuilder().build())
        .when(ngManagerServiceDriver)
        .reportPerpetualTaskStateChange(any());
    forwardingPerpetualTaskServiceClient.onTaskStateChange("taskId", newPerpetualTaskResponse, oldTaskResponse);

    verify(ngManagerServiceDriver, times(1)).reportPerpetualTaskStateChange(any());
  }

  @Test
  @Owner(developers = ROHIT_KUMAR)
  @Category(UnitTests.class)
  public void getValidationTask() {
    doReturn(ObtainPerpetualTaskValidationDetailsResponse.newBuilder()
                 .setSetupAbstractions(TaskSetupAbstractions.newBuilder().build())
                 .setDetails(TaskDetails.newBuilder()
                                 .setMode(TaskMode.SYNC)
                                 .setType(TaskType.newBuilder().setType("HTTP").build())
                                 .build())
                 .build())
        .when(ngManagerServiceDriver)
        .obtainPerpetualTaskValidationDetails(any());
    final DelegateTask validationTask = forwardingPerpetualTaskServiceClient.getValidationTask(
        PerpetualTaskClientContext.builder().clientParams(ImmutableMap.of("key", "value")).build(), ACCOUNT_ID);
    assertThat(validationTask.getData().getTaskType()).isEqualTo("HTTP");
    assertThat(validationTask.getData().isAsync()).isFalse();
  }
}