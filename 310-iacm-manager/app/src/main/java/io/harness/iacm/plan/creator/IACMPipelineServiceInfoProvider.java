/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.iacm.plan.creator;

import static io.harness.pms.yaml.YAMLFieldNameConstants.STEP;
import static io.harness.pms.yaml.YAMLFieldNameConstants.STEPS;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.steps.StepSpecTypeConstants;
import io.harness.ci.creator.variables.PluginStepVariableCreator;
import io.harness.ci.plancreator.PluginStepPlanCreator;
import io.harness.filters.EmptyAnyFilterJsonCreator;
import io.harness.filters.ExecutionPMSFilterJsonCreator;
import io.harness.iacm.IACMStepType;
import io.harness.iacm.creator.variables.IACMStageVariableCreator;
import io.harness.iacm.plan.creator.filter.IACMStageFilterJsonCreator;
import io.harness.iacm.plan.creator.stage.IACMStagePMSPlanCreator;
import io.harness.iacm.plan.creator.step.IACMPMSStepFilterJsonCreator;
import io.harness.iacm.plan.creator.step.IACMStepFilterJsonCreatorV2;
import io.harness.iacm.plan.creator.step.IACMStepPlanCreator;
import io.harness.iacm.plan.creator.step.IACMTemplateStepVariableCreator;
import io.harness.iacm.plan.creator.step.IACMTerraformPlanStepVariableCreator;
import io.harness.plancreator.execution.ExecutionPmsPlanCreator;
import io.harness.plancreator.steps.NGStageStepsPlanCreator;
import io.harness.pms.contracts.steps.StepInfo;
import io.harness.pms.contracts.steps.StepMetaData;
import io.harness.pms.sdk.core.pipeline.filters.FilterJsonCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PartialPlanCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PipelineServiceInfoProvider;
import io.harness.pms.sdk.core.variables.EmptyAnyVariableCreator;
import io.harness.pms.sdk.core.variables.EmptyVariableCreator;
import io.harness.pms.sdk.core.variables.VariableCreator;
import io.harness.pms.utils.InjectorUtils;
import io.harness.pms.yaml.YAMLFieldNameConstants;
import io.harness.variables.ExecutionVariableCreator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@OwnedBy(HarnessTeam.IACM)
public class IACMPipelineServiceInfoProvider implements PipelineServiceInfoProvider {
  private static final String LITE_ENGINE_TASK = "liteEngineTask";
  @Inject InjectorUtils injectorUtils;

  @Override
  public List<PartialPlanCreator<?>> getPlanCreators() {
    List<PartialPlanCreator<?>> planCreators = new LinkedList<>();
    planCreators.add(new IACMStagePMSPlanCreator()); // Creation of the Stage execution plan
    planCreators.add(new IACMStepPlanCreator()); // Supported steps for the Stage
    planCreators.add(new PluginStepPlanCreator()); // Plugin step
    planCreators.add(new NGStageStepsPlanCreator()); // Rollback steps related
    planCreators.add(new ExecutionPmsPlanCreator()); // I think that the execution step is treated as a step also
    planCreators.addAll(
        Arrays.asList(IACMStepType.values()).stream().map(e -> e.getPlanCreator()).collect(Collectors.toList()));
    injectorUtils.injectMembers(planCreators);
    return planCreators;
  }

  @Override
  public List<FilterJsonCreator> getFilterJsonCreators() {
    List<FilterJsonCreator> filterJsonCreators = new ArrayList<>();
    filterJsonCreators.add(new IACMStageFilterJsonCreator()); // Filter for the Stage
    filterJsonCreators.add(new IACMPMSStepFilterJsonCreator()); // Filter for the supported stages in the stage
    filterJsonCreators.add(new IACMStepFilterJsonCreatorV2()); // Filter for the supported stages in the stage
    filterJsonCreators.add(new ExecutionPMSFilterJsonCreator()); // Filter for the Execution step
    filterJsonCreators.add(new EmptyAnyFilterJsonCreator(Set.of(STEPS))); // ??
    injectorUtils.injectMembers(filterJsonCreators);

    return filterJsonCreators;
  }

  @Override
  public List<VariableCreator> getVariableCreators() {
    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(new IACMStageVariableCreator()); // Variable creator for the stage
    variableCreators.add(new ExecutionVariableCreator()); // variable creator for the execution
    variableCreators.add(new PluginStepVariableCreator()); // variable creator for the plugin step
    variableCreators.add(new IACMTerraformPlanStepVariableCreator());
    variableCreators.add(new IACMTemplateStepVariableCreator());

    variableCreators.add(new EmptyAnyVariableCreator(Set.of(YAMLFieldNameConstants.PARALLEL, STEPS))); // ??
    variableCreators.add(new EmptyVariableCreator(STEP, Set.of(LITE_ENGINE_TASK)));
    return variableCreators;
  }

  private StepInfo createStepInfo(IACMStepType iacmStepType, String stepCategory) {
    return StepInfo.newBuilder()
        .setName(iacmStepType.getName())
        .setType(iacmStepType.getName())
        .setFeatureFlag(iacmStepType.getFeatureName().name())
        .setStepMetaData(StepMetaData.newBuilder().addFolderPaths(stepCategory).build())
        .build();
  }

  /**
   * Does this method requires to have all the steps supported as _steps_ and not as stage steps?
   *
   */
  @Override
  public List<StepInfo> getStepInfo() {
    StepInfo pluginStepInfo = StepInfo.newBuilder()
                                  .setName("Plugin")
                                  .setType(StepSpecTypeConstants.PLUGIN)
                                  .setStepMetaData(StepMetaData.newBuilder().addFolderPaths("Build").build())
                                  .build();

    List<StepInfo> stepInfos = new ArrayList<>();

    stepInfos.add(pluginStepInfo);
    Arrays.asList(IACMStepType.values())
        .forEach(e -> e.getStepCategories().forEach(category -> stepInfos.add(createStepInfo(e, category))));
    return stepInfos;
  }
}
