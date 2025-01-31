/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.pms.pipelinestage.step;

import static io.harness.rule.OwnerRule.PRASHANTSHARMA;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.harness.CategoryTest;
import io.harness.accesscontrol.clients.AccessControlClient;
import io.harness.category.element.UnitTests;
import io.harness.engine.execution.PipelineStageResponseData;
import io.harness.engine.executions.node.NodeExecutionService;
import io.harness.execution.NodeExecution;
import io.harness.execution.NodeExecution.NodeExecutionKeys;
import io.harness.execution.PlanExecution;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.AsyncExecutableResponse;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.plan.ExecutionMetadata;
import io.harness.pms.contracts.plan.PipelineStageInfo;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.pipelinestage.PipelineStageStepParameters;
import io.harness.pms.pipelinestage.helper.PipelineStageHelper;
import io.harness.pms.pipelinestage.outcome.PipelineStageOutcome;
import io.harness.pms.pipelinestage.output.PipelineStageSweepingOutput;
import io.harness.pms.plan.execution.PipelineExecutor;
import io.harness.pms.plan.execution.PlanExecutionInterruptType;
import io.harness.pms.plan.execution.PlanExecutionResponseDto;
import io.harness.pms.plan.execution.service.PMSExecutionService;
import io.harness.pms.sdk.core.data.OptionalSweepingOutput;
import io.harness.pms.sdk.core.resolver.RefObjectUtils;
import io.harness.pms.sdk.core.resolver.outputs.ExecutionSweepingOutputService;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.yaml.ParameterField;
import io.harness.rule.Owner;
import io.harness.security.SecurityContextBuilder;
import io.harness.tasks.ResponseData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PipelineStageStepTest extends CategoryTest {
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock PMSExecutionService pmsExecutionService;
  @Mock PipelineStageHelper pipelineStageHelper;
  @Mock AccessControlClient client;
  @Mock PipelineExecutor pipelineExecutor;
  @Mock ExecutionSweepingOutputService sweepingOutputService;
  @Mock NodeExecutionService nodeExecutionService;
  @InjectMocks PipelineStageStep pipelineStageStep;

  String planExecutionId = "planExecutionId";
  String projectId = "projectId";
  String ordId = "orgId";
  Map<String, String> setup = new HashMap<>();
  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testAbort() {
    String firstCallBackId = "callBack1";
    String secondCallBackId = "callBack2";
    Ambiance ambiance = Ambiance.newBuilder().build();

    pipelineStageStep.handleAbort(ambiance, PipelineStageStepParameters.builder().build(),
        AsyncExecutableResponse.newBuilder().addCallbackIds(firstCallBackId).addCallbackIds(secondCallBackId).build());
    verify(pmsExecutionService, times(1)).registerInterrupt(PlanExecutionInterruptType.ABORTALL, firstCallBackId, null);
    assertThat(SecurityContextBuilder.getPrincipal()).isNotNull();
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testPipelineStageInfo() {
    setup.put("projectIdentifier", projectId);
    setup.put("orgIdentifier", ordId);
    Ambiance ambiance = Ambiance.newBuilder()
                            .setPlanExecutionId(planExecutionId)
                            .putAllSetupAbstractions(setup)
                            .setMetadata(ExecutionMetadata.newBuilder().setRunSequence(40).build())
                            .build();

    PipelineStageStepParameters stepParameters =
        PipelineStageStepParameters.builder().stageNodeId("stageNodeId").build();
    PipelineStageInfo info = pipelineStageStep.prepareParentStageInfo(ambiance, stepParameters);
    assertThat(info.getHasParentPipeline()).isEqualTo(true);
    assertThat(info.getStageNodeId()).isEqualTo("stageNodeId");
    assertThat(info.getExecutionId()).isEqualTo(planExecutionId);
    assertThat(info.getProjectId()).isEqualTo(projectId);
    assertThat(info.getOrgId()).isEqualTo(ordId);
    assertThat(info.getRunSequence()).isEqualTo(40);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testStepParameters() {
    assertThat(pipelineStageStep.getStepParametersClass()).isEqualTo(PipelineStageStepParameters.class);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testValidateResource() {
    setup.put("projectIdentifier", projectId);
    setup.put("orgIdentifier", ordId);
    Ambiance ambiance = Ambiance.newBuilder()
                            .setPlanExecutionId(planExecutionId)
                            .putAllSetupAbstractions(setup)
                            .setMetadata(ExecutionMetadata.newBuilder().setRunSequence(40).build())
                            .build();

    PipelineStageStepParameters stepParameters =
        PipelineStageStepParameters.builder().stageNodeId("stageNodeId").build();
    pipelineStageStep.validateResources(ambiance, stepParameters);
    verify(pipelineStageHelper, times(1)).validateResource(client, ambiance, stepParameters);
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testExecuteAsyncAfterRbac() {
    setup.put("projectIdentifier", projectId);
    setup.put("orgIdentifier", ordId);
    Ambiance ambiance = Ambiance.newBuilder()
                            .setPlanExecutionId(planExecutionId)
                            .putAllSetupAbstractions(setup)
                            .setMetadata(ExecutionMetadata.newBuilder().setRunSequence(40).build())
                            .build();

    PipelineStageStepParameters stepParameters =
        PipelineStageStepParameters.builder().stageNodeId("stageNodeId").build();

    PipelineStageInfo info = pipelineStageStep.prepareParentStageInfo(ambiance, stepParameters);
    doReturn(PlanExecutionResponseDto.builder().planExecution(PlanExecution.builder().uuid("uuid").build()).build())
        .when(pipelineExecutor)
        .runPipelineAsChildPipeline(ambiance.getSetupAbstractions().get("accountId"), stepParameters.getOrg(),
            stepParameters.getProject(), stepParameters.getPipeline(), ambiance.getMetadata().getModuleType(),
            stepParameters.getPipelineInputs(), false, false, stepParameters.getInputSetReferences(), info,
            ambiance.getMetadata().getIsDebug());

    doReturn(null)
        .when(sweepingOutputService)
        .consume(ambiance, PipelineStageSweepingOutput.OUTPUT_NAME,
            PipelineStageSweepingOutput.builder().childExecutionId("uuid").build(), StepCategory.STAGE.name());

    AsyncExecutableResponse asyncExecutableResponse =
        pipelineStageStep.executeAsyncAfterRbac(ambiance, stepParameters, null);

    // to verify if principal is set
    assertThat(SecurityContextBuilder.getPrincipal()).isNotNull();
    assertThat(asyncExecutableResponse.getCallbackIdsList().size()).isEqualTo(1);
    assertThat(asyncExecutableResponse.getCallbackIds(0)).isEqualTo("uuid");
  }

  @Test
  @Owner(developers = PRASHANTSHARMA)
  @Category(UnitTests.class)
  public void testHandleAsyncResponse() {
    setup.put("projectIdentifier", projectId);
    setup.put("orgIdentifier", ordId);
    Ambiance ambiance = Ambiance.newBuilder()
                            .setPlanExecutionId(planExecutionId)
                            .putAllSetupAbstractions(setup)
                            .setMetadata(ExecutionMetadata.newBuilder().setRunSequence(40).build())
                            .build();

    String planExecutionId = "planExecutionId";
    PipelineStageStepParameters stepParameters =
        PipelineStageStepParameters.builder()
            .stageNodeId("stageNodeId")
            .outputs(ParameterField.<Map<String, ParameterField<String>>>builder().build())
            .build();

    doReturn(OptionalSweepingOutput.builder().build())
        .when(sweepingOutputService)
        .resolveOptional(ambiance, RefObjectUtils.getSweepingOutputRefObject(PipelineStageSweepingOutput.OUTPUT_NAME));
    Map<String, ResponseData> responseDataMap = new HashMap<>();
    responseDataMap.put(planExecutionId, PipelineStageResponseData.builder().status(Status.SUCCEEDED).build());
    StepResponse stepResponse = pipelineStageStep.handleAsyncResponse(ambiance, stepParameters, responseDataMap);
    assertThat(stepResponse.getStatus()).isEqualTo(Status.FAILED);

    PipelineStageSweepingOutput output =
        PipelineStageSweepingOutput.builder().childExecutionId(planExecutionId).build();
    doReturn(OptionalSweepingOutput.builder().found(true).output(output).build())
        .when(sweepingOutputService)
        .resolveOptional(ambiance, RefObjectUtils.getSweepingOutputRefObject(PipelineStageSweepingOutput.OUTPUT_NAME));

    doReturn(PipelineStageOutcome.builder().build()).when(pipelineStageHelper).resolveOutputVariables(any(), any());
    doReturn(Optional.of(NodeExecution.builder().ambiance(ambiance).build()))
        .when(nodeExecutionService)
        .getPipelineNodeExecutionWithProjections(
            output.getChildExecutionId(), Collections.singleton(NodeExecutionKeys.ambiance));

    stepResponse = pipelineStageStep.handleAsyncResponse(ambiance, stepParameters, responseDataMap);

    assertThat(stepResponse.getStatus()).isEqualTo(Status.SUCCEEDED);
  }
}
