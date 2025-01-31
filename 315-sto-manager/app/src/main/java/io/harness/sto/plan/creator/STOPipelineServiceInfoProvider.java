/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.sto.plan.creator;

import static io.harness.pms.yaml.YAMLFieldNameConstants.STEP;
import static io.harness.pms.yaml.YAMLFieldNameConstants.STEPS;
import static io.harness.pms.yaml.YAMLFieldNameConstants.STRATEGY;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.steps.StepSpecTypeConstants;
import io.harness.ci.creator.variables.BackgroundStepVariableCreator;
import io.harness.ci.creator.variables.PluginStepVariableCreator;
import io.harness.ci.creator.variables.RunStepVariableCreator;
import io.harness.ci.creator.variables.STOCommonStepVariableCreator;
import io.harness.ci.creator.variables.STOStageVariableCreator;
import io.harness.ci.creator.variables.STOStepVariableCreator;
import io.harness.ci.creator.variables.SecurityStepVariableCreator;
import io.harness.ci.plancreator.BackgroundStepPlanCreator;
import io.harness.ci.plancreator.InitializeStepPlanCreator;
import io.harness.ci.plancreator.PluginStepPlanCreator;
import io.harness.ci.plancreator.RunStepPlanCreator;
import io.harness.ci.plancreator.SecurityStepPlanCreator;
import io.harness.filters.EmptyAnyFilterJsonCreator;
import io.harness.filters.ExecutionPMSFilterJsonCreator;
import io.harness.filters.ParallelGenericFilterJsonCreator;
import io.harness.filters.StepGroupPmsFilterJsonCreator;
import io.harness.plancreator.execution.ExecutionPmsPlanCreator;
import io.harness.plancreator.stages.parallel.ParallelPlanCreator;
import io.harness.plancreator.steps.NGStageStepsPlanCreator;
import io.harness.plancreator.steps.StepGroupPMSPlanCreator;
import io.harness.plancreator.strategy.StrategyConfigPlanCreator;
import io.harness.pms.contracts.steps.StepInfo;
import io.harness.pms.contracts.steps.StepMetaData;
import io.harness.pms.sdk.core.pipeline.filters.FilterJsonCreator;
import io.harness.pms.sdk.core.pipeline.variables.StepGroupVariableCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PartialPlanCreator;
import io.harness.pms.sdk.core.plan.creation.creators.PipelineServiceInfoProvider;
import io.harness.pms.sdk.core.variables.EmptyAnyVariableCreator;
import io.harness.pms.sdk.core.variables.EmptyVariableCreator;
import io.harness.pms.sdk.core.variables.StrategyVariableCreator;
import io.harness.pms.sdk.core.variables.VariableCreator;
import io.harness.pms.utils.InjectorUtils;
import io.harness.pms.yaml.YAMLFieldNameConstants;
import io.harness.sto.STOStepType;
import io.harness.sto.plan.creator.filter.STOStageFilterJsonCreator;
import io.harness.sto.plan.creator.stage.SecurityStagePMSPlanCreator;
import io.harness.sto.plan.creator.step.STOGenericStepPlanCreator;
import io.harness.sto.plan.creator.step.STOStepFilterJsonCreatorV2;
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
@OwnedBy(HarnessTeam.STO)
public class STOPipelineServiceInfoProvider implements PipelineServiceInfoProvider {
  private static final String LITE_ENGINE_TASK = "liteEngineTask";
  @Inject InjectorUtils injectorUtils;

  @Override
  public List<PartialPlanCreator<?>> getPlanCreators() {
    List<PartialPlanCreator<?>> planCreators = new LinkedList<>();
    planCreators.add(new SecurityStagePMSPlanCreator());

    planCreators.addAll(Arrays.asList(STOStepType.values())
                            .stream()
                            .map(STOPipelineServiceInfoProvider::getPlanCreator)
                            .collect(Collectors.toList()));

    planCreators.add(new RunStepPlanCreator());
    planCreators.add(new BackgroundStepPlanCreator());
    planCreators.add(new SecurityStepPlanCreator());
    planCreators.add(new NGStageStepsPlanCreator());
    planCreators.add(new StepGroupPMSPlanCreator());
    planCreators.add(new PluginStepPlanCreator());
    planCreators.add(new ParallelPlanCreator());
    planCreators.add(new StrategyConfigPlanCreator());
    planCreators.add(new ExecutionPmsPlanCreator());
    planCreators.add(new InitializeStepPlanCreator());

    injectorUtils.injectMembers(planCreators);
    return planCreators;
  }

  @Override
  public List<FilterJsonCreator> getFilterJsonCreators() {
    List<FilterJsonCreator> filterJsonCreators = new ArrayList<>();
    filterJsonCreators.add(new STOStageFilterJsonCreator());
    filterJsonCreators.add(new STOStepFilterJsonCreatorV2());
    filterJsonCreators.add(new StepGroupPmsFilterJsonCreator());
    filterJsonCreators.add(new ExecutionPMSFilterJsonCreator());
    filterJsonCreators.add(new ParallelGenericFilterJsonCreator());
    filterJsonCreators.add(new EmptyAnyFilterJsonCreator(Set.of(STRATEGY, STEPS)));
    injectorUtils.injectMembers(filterJsonCreators);

    return filterJsonCreators;
  }

  @Override
  public List<VariableCreator> getVariableCreators() {
    List<VariableCreator> variableCreators = new ArrayList<>();
    variableCreators.add(new STOStageVariableCreator());
    variableCreators.add(new ExecutionVariableCreator());
    variableCreators.add(new STOStepVariableCreator());
    variableCreators.add(new StepGroupVariableCreator());
    variableCreators.add(new STOCommonStepVariableCreator());
    variableCreators.add(new RunStepVariableCreator());
    variableCreators.add(new BackgroundStepVariableCreator());
    variableCreators.add(new SecurityStepVariableCreator());
    variableCreators.add(new PluginStepVariableCreator());
    variableCreators.add(new StrategyVariableCreator());
    variableCreators.add(new EmptyAnyVariableCreator(Set.of(YAMLFieldNameConstants.PARALLEL, STEPS)));
    variableCreators.add(new EmptyVariableCreator(STEP, Set.of(LITE_ENGINE_TASK)));

    return variableCreators;
  }

  private static PartialPlanCreator<?> getPlanCreator(STOStepType stepType) {
    return new STOGenericStepPlanCreator(stepType);
  }

  private StepInfo createStepInfo(STOStepType stoStepType, String stepCategory) {
    return StepInfo.newBuilder()
        .setName(stoStepType.getName())
        .setType(stoStepType.getName())
        .setFeatureFlag(stoStepType.getFeatureName().name())
        .setStepMetaData(StepMetaData.newBuilder().addFolderPaths(stepCategory).build())
        .build();
  }

  @Override
  public List<StepInfo> getStepInfo() {
    StepInfo securityStepInfo = StepInfo.newBuilder()
                                    .setName("Security")
                                    .setType(StepSpecTypeConstants.SECURITY)
                                    .setStepMetaData(StepMetaData.newBuilder().addFolderPaths("Security").build())
                                    .build();
    StepInfo runStepInfo = StepInfo.newBuilder()
                               .setName("Run")
                               .setType(StepSpecTypeConstants.RUN)
                               .setStepMetaData(StepMetaData.newBuilder().addFolderPaths("Build").build())
                               .build();
    StepInfo backgroundStepInfo = StepInfo.newBuilder()
                                      .setName("Background")
                                      .setType(StepSpecTypeConstants.BACKGROUND)
                                      .setStepMetaData(StepMetaData.newBuilder().addFolderPaths("Build").build())
                                      .build();

    List<StepInfo> stepInfos = new ArrayList<>();

    stepInfos.add(securityStepInfo);
    stepInfos.add(runStepInfo);
    stepInfos.add(backgroundStepInfo);
    Arrays.asList(STOStepType.values())
        .forEach(e -> e.getStepCategories().forEach(category -> stepInfos.add(createStepInfo(e, category))));

    return stepInfos;
  }
}
