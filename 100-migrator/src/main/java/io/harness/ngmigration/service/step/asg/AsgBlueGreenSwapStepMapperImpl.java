/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ngmigration.service.step.asg;

import io.harness.cdng.aws.asg.AsgBlueGreenSwapServiceStepInfo;
import io.harness.cdng.aws.asg.AsgBlueGreenSwapServiceStepNode;
import io.harness.executions.steps.StepSpecTypeConstants;
import io.harness.ngmigration.beans.SupportStatus;
import io.harness.ngmigration.beans.WorkflowMigrationContext;
import io.harness.ngmigration.service.step.StepMapper;
import io.harness.plancreator.steps.AbstractStepNode;
import io.harness.pms.yaml.ParameterField;

import software.wings.beans.GraphNode;
import software.wings.sm.State;
import software.wings.sm.states.AwsAmiSwitchRoutesState;

import java.util.Map;

public class AsgBlueGreenSwapStepMapperImpl extends StepMapper {
  @Override
  public String getStepType(GraphNode stepYaml) {
    return StepSpecTypeConstants.ASG_BLUE_GREEN_SWAP_SERVICE;
  }

  @Override
  public State getState(GraphNode stepYaml) {
    Map<String, Object> properties = getProperties(stepYaml);
    AwsAmiSwitchRoutesState state = new AwsAmiSwitchRoutesState(stepYaml.getName());
    state.parseProperties(properties);
    return state;
  }

  @Override
  public AbstractStepNode getSpec(WorkflowMigrationContext context, GraphNode graphNode) {
    AwsAmiSwitchRoutesState state = (AwsAmiSwitchRoutesState) getState(graphNode);

    AsgBlueGreenSwapServiceStepNode node = new AsgBlueGreenSwapServiceStepNode();
    baseSetup(state, node);
    node.setAsgBlueGreenSwapServiceStepInfo(
        AsgBlueGreenSwapServiceStepInfo.infoBuilder()
            .downsizeOldAsg(ParameterField.createValueField(state.isDownsizeOldAsg()))
            .build());

    return node;
  }

  @Override
  public boolean areSimilar(GraphNode stepYaml1, GraphNode stepYaml2) {
    return true;
  }

  @Override
  public SupportStatus stepSupportStatus(GraphNode graphNode) {
    return SupportStatus.SUPPORTED;
  }
}
