/*
 * Copyright 2023 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.cdng.provision.terraformcloud.steps;

import static io.harness.cdng.provision.terraformcloud.outcome.TerraformCloudRunOutcome.OUTCOME_NAME;

import static java.lang.String.format;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.cdng.executables.CdTaskExecutable;
import io.harness.cdng.provision.terraformcloud.TerraformCloudConstants;
import io.harness.cdng.provision.terraformcloud.TerraformCloudRollbackStepParameters;
import io.harness.cdng.provision.terraformcloud.TerraformCloudStepHelper;
import io.harness.cdng.provision.terraformcloud.dal.TerraformCloudConfig;
import io.harness.cdng.provision.terraformcloud.dal.TerraformCloudConfigDAL;
import io.harness.cdng.provision.terraformcloud.outcome.TerraformCloudRunOutcome;
import io.harness.cdng.provision.terraformcloud.output.TerraformCloudConfigSweepingOutput;
import io.harness.cdng.stepsdependency.constants.OutcomeExpressionConstants;
import io.harness.common.ParameterFieldHelper;
import io.harness.connector.helper.EncryptionHelper;
import io.harness.delegate.beans.TaskData;
import io.harness.delegate.beans.connector.terraformcloudconnector.TerraformCloudConnectorDTO;
import io.harness.delegate.beans.terraformcloud.RollbackType;
import io.harness.delegate.beans.terraformcloud.TerraformCloudTaskParams;
import io.harness.delegate.beans.terraformcloud.TerraformCloudTaskType;
import io.harness.delegate.task.terraformcloud.TerraformCloudCommandUnit;
import io.harness.delegate.task.terraformcloud.response.TerraformCloudRollbackTaskResponse;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.WingsException;
import io.harness.executions.steps.ExecutionNodeType;
import io.harness.logging.CommandExecutionStatus;
import io.harness.logging.UnitProgress;
import io.harness.persistence.HIterator;
import io.harness.plancreator.steps.TaskSelectorYaml;
import io.harness.plancreator.steps.common.StepElementParameters;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.contracts.execution.tasks.SkipTaskRequest;
import io.harness.pms.contracts.execution.tasks.TaskRequest;
import io.harness.pms.contracts.steps.StepCategory;
import io.harness.pms.contracts.steps.StepType;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.rbac.PipelineRbacHelper;
import io.harness.pms.sdk.core.data.OptionalSweepingOutput;
import io.harness.pms.sdk.core.plan.creation.yaml.StepOutcomeGroup;
import io.harness.pms.sdk.core.resolver.RefObjectUtils;
import io.harness.pms.sdk.core.resolver.outputs.ExecutionSweepingOutputService;
import io.harness.pms.sdk.core.steps.io.StepInputPackage;
import io.harness.pms.sdk.core.steps.io.StepResponse;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepOutcome;
import io.harness.pms.sdk.core.steps.io.StepResponse.StepResponseBuilder;
import io.harness.serializer.KryoSerializer;
import io.harness.steps.StepHelper;
import io.harness.steps.StepUtils;
import io.harness.steps.TaskRequestsUtils;
import io.harness.supplier.ThrowingSupplier;

import software.wings.beans.TaskType;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@OwnedBy(HarnessTeam.CDP)
@Slf4j
public class TerraformCloudRollbackStep extends CdTaskExecutable<TerraformCloudRollbackTaskResponse> {
  public static final StepType STEP_TYPE = StepType.newBuilder()
                                               .setType(ExecutionNodeType.TERRAFORM_CLOUD_ROLLBACK.getYamlType())
                                               .setStepCategory(StepCategory.STEP)
                                               .build();

  @Inject private PipelineRbacHelper pipelineRbacHelper;
  @Inject @Named("referenceFalseKryoSerializer") private KryoSerializer referenceFalseKryoSerializer;
  @Inject private StepHelper stepHelper;
  @Inject private TerraformCloudStepHelper helper;
  @Inject private EncryptionHelper encryptionHelper;
  @Inject private TerraformCloudConfigDAL terraformCloudConfigDAL;
  @Inject ExecutionSweepingOutputService executionSweepingOutputService;

  @Override
  public Class getStepParametersClass() {
    return StepElementParameters.class;
  }

  @Override
  public void validateResources(Ambiance ambiance, StepElementParameters stepParameters) {
    // nothing to validate here
  }

  @Override
  public TaskRequest obtainTaskAfterRbac(
      Ambiance ambiance, StepElementParameters stepElementParameters, StepInputPackage inputPackage) {
    log.info("Starting execution ObtainTask after Rbac for the Terraform Cloud Rollback Step");
    TerraformCloudRollbackStepParameters rollbackStepParameters =
        (TerraformCloudRollbackStepParameters) stepElementParameters.getSpec();

    String provisionerIdentifier =
        ParameterFieldHelper.getParameterFieldValue(rollbackStepParameters.getProvisionerIdentifier());
    String entityId = helper.generateFullIdentifier(provisionerIdentifier, ambiance);

    try (HIterator<TerraformCloudConfig> configIterator = terraformCloudConfigDAL.getIterator(ambiance, entityId)) {
      if (!configIterator.hasNext()) {
        return TaskRequest.newBuilder()
            .setSkipTaskRequest(
                SkipTaskRequest.newBuilder()
                    .setMessage(format(
                        "No successful Provisioning found with provisionerIdentifier: [%s]. Skipping terraform cloud rollback.",
                        provisionerIdentifier))
                    .build())
            .build();
      }

      TerraformCloudConfig rollbackConfig = null;
      TerraformCloudConfig currentConfig = null;
      while (configIterator.hasNext()) {
        rollbackConfig = configIterator.next();
        if (rollbackConfig.getPipelineExecutionId().equals(ambiance.getPlanExecutionId())) {
          if (currentConfig == null) {
            currentConfig = rollbackConfig;
          }
        } else {
          // Found previous successful terraform cloud config
          break;
        }
      }

      RollbackType rollbackTaskType;
      if (rollbackConfig == currentConfig) {
        log.info(format(
            "No previous successful Terraform cloud execution exists with the identifier : [%s], hence Destroying.",
            provisionerIdentifier));
        rollbackTaskType = RollbackType.DESTROY;
      } else {
        log.info(format("Inheriting Terraform Cloud Config from last successful Terraform Cloud Pipeline Execution  %s",
            rollbackConfig));
        rollbackTaskType = RollbackType.APPLY;
      }

      executionSweepingOutputService.consume(ambiance, OutcomeExpressionConstants.TERRAFORM_CLOUD_CONFIG,
          TerraformCloudConfigSweepingOutput.builder()
              .terraformCloudConfig(rollbackConfig)
              .rollbackTaskType(rollbackTaskType)
              .build(),
          StepOutcomeGroup.STEP.name());

      TerraformCloudConnectorDTO terraformCloudConnector =
          helper.getTerraformCloudConnectorWithRef(rollbackConfig.getConnectorRef(), ambiance);

      TerraformCloudTaskParams terraformCloudTaskParams =
          TerraformCloudTaskParams.builder()
              .terraformCloudTaskType(TerraformCloudTaskType.ROLLBACK)
              .accountId(AmbianceUtils.getAccountId(ambiance))
              .runId(rollbackConfig.getRunId())
              .rollbackType(rollbackTaskType)
              .entityId(entityId)
              .terraformCloudConnectorDTO(terraformCloudConnector)
              .discardPendingRuns(
                  ParameterFieldHelper.getBooleanParameterFieldValue(rollbackStepParameters.getDiscardPendingRuns()))
              .encryptionDetails(encryptionHelper.getEncryptionDetail(terraformCloudConnector.getCredential().getSpec(),
                  AmbianceUtils.getAccountId(ambiance), AmbianceUtils.getOrgIdentifier(ambiance),
                  AmbianceUtils.getProjectIdentifier(ambiance)))
              .message(ParameterFieldHelper.getParameterFieldValue(rollbackStepParameters.getMessage()))
              .build();

      TaskData taskData = TaskData.builder()
                              .async(true)
                              .taskType(TaskType.TERRAFORM_CLOUD_TASK_NG.name())
                              .timeout(StepUtils.getTimeoutMillis(
                                  stepElementParameters.getTimeout(), TerraformCloudConstants.DEFAULT_TIMEOUT))
                              .parameters(new Object[] {terraformCloudTaskParams})
                              .build();

      return TaskRequestsUtils.prepareCDTaskRequest(ambiance, taskData, referenceFalseKryoSerializer,
          Collections.singletonList(TerraformCloudCommandUnit.RUN.name()),
          TaskType.TERRAFORM_CLOUD_TASK_NG.getDisplayName(),
          TaskSelectorYaml.toTaskSelector(rollbackStepParameters.getDelegateSelectors()),
          stepHelper.getEnvironmentType(ambiance));
    }
  }

  @Override
  public StepResponse handleTaskResultWithSecurityContext(Ambiance ambiance,
      StepElementParameters stepElementParameters,
      ThrowingSupplier<TerraformCloudRollbackTaskResponse> responseSupplier) throws Exception {
    log.info("Handling Task result with Security Context for the Terraform Cloud Rollback Step");
    StepResponseBuilder stepResponseBuilder = StepResponse.builder();
    TerraformCloudRollbackTaskResponse terraformCloudRunTaskResponse = responseSupplier.get();
    List<UnitProgress> unitProgresses = terraformCloudRunTaskResponse.getUnitProgressData() == null
        ? Collections.emptyList()
        : terraformCloudRunTaskResponse.getUnitProgressData().getUnitProgresses();
    stepResponseBuilder.unitProgressList(unitProgresses);

    switch (terraformCloudRunTaskResponse.getCommandExecutionStatus()) {
      case SUCCESS:
        stepResponseBuilder.status(Status.SUCCEEDED);
        break;
      case FAILURE:
        stepResponseBuilder.status(Status.FAILED);
        break;
      case RUNNING:
        stepResponseBuilder.status(Status.RUNNING);
        break;
      case QUEUED:
        stepResponseBuilder.status(Status.QUEUED);
        break;
      default:
        throw new InvalidRequestException("Unhandled type CommandExecutionStatus: "
                + terraformCloudRunTaskResponse.getCommandExecutionStatus().name(),
            WingsException.USER);
    }

    if (CommandExecutionStatus.SUCCESS == terraformCloudRunTaskResponse.getCommandExecutionStatus()) {
      String runId = terraformCloudRunTaskResponse.getRunId();
      OptionalSweepingOutput optionalSweepingOutput = executionSweepingOutputService.resolveOptional(
          ambiance, RefObjectUtils.getSweepingOutputRefObject(OutcomeExpressionConstants.TERRAFORM_CLOUD_CONFIG));
      TerraformCloudConfigSweepingOutput rollbackConfigOutput =
          (TerraformCloudConfigSweepingOutput) optionalSweepingOutput.getOutput();
      TerraformCloudConfig rollbackConfig = rollbackConfigOutput.getTerraformCloudConfig();

      if (rollbackConfigOutput.getRollbackTaskType() == RollbackType.APPLY) {
        rollbackConfig.setRunId(runId);
        helper.saveTerraformCloudConfig(rollbackConfig, ambiance);
      } else {
        terraformCloudConfigDAL.clearTerraformCloudConfig(ambiance, rollbackConfig.getEntityId());
      }

      stepResponseBuilder.stepOutcome(
          StepOutcome.builder()
              .name(OUTCOME_NAME)
              .outcome(
                  TerraformCloudRunOutcome.builder()
                      .detailedExitCode(terraformCloudRunTaskResponse.getDetailedExitCode())
                      .runId(runId)
                      .outputs(new HashMap<>(helper.parseTerraformOutputs(terraformCloudRunTaskResponse.getTfOutput())))
                      .build())
              .build());
    }
    return stepResponseBuilder.build();
  }
}
