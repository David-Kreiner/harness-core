/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ngmigration.service.step;

import static io.harness.ngmigration.utils.MigratorUtility.RUNTIME_INPUT;

import io.harness.data.structure.EmptyPredicate;
import io.harness.ngmigration.beans.StepOutput;
import io.harness.ngmigration.beans.SupportStatus;
import io.harness.ngmigration.beans.WorkflowMigrationContext;
import io.harness.ngmigration.expressions.step.JiraFunctor;
import io.harness.ngmigration.expressions.step.StepExpressionFunctor;
import io.harness.ngmigration.utils.MigratorUtility;
import io.harness.plancreator.steps.AbstractStepNode;
import io.harness.pms.yaml.ParameterField;
import io.harness.steps.StepSpecTypeConstants;
import io.harness.steps.jira.beans.JiraField;
import io.harness.steps.jira.create.JiraCreateStepInfo;
import io.harness.steps.jira.create.JiraCreateStepNode;
import io.harness.steps.jira.update.JiraUpdateStepInfo;
import io.harness.steps.jira.update.JiraUpdateStepNode;
import io.harness.steps.jira.update.beans.TransitionTo;

import software.wings.beans.GraphNode;
import software.wings.beans.PhaseStep;
import software.wings.beans.WorkflowPhase;
import software.wings.sm.State;
import software.wings.sm.states.collaboration.JiraCreateUpdate;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.rcarz.jiraclient.Field;
import org.apache.commons.lang3.StringUtils;

public class JiraCreateUpdateStepMapperImpl extends StepMapper {
  @Override
  public SupportStatus stepSupportStatus(GraphNode graphNode) {
    return SupportStatus.SUPPORTED;
  }

  @Override
  public String getStepType(GraphNode stepYaml) {
    JiraCreateUpdate state = (JiraCreateUpdate) getState(stepYaml);
    switch (state.getJiraAction()) {
      case UPDATE_TICKET:
        return StepSpecTypeConstants.JIRA_UPDATE;
      case CREATE_TICKET:
        return StepSpecTypeConstants.JIRA_CREATE;
      default:
        throw new IllegalStateException("Unsupported Approval Type");
    }
  }

  @Override
  public State getState(GraphNode stepYaml) {
    Map<String, Object> properties = getProperties(stepYaml);
    JiraCreateUpdate state = new JiraCreateUpdate(stepYaml.getName());
    state.parseProperties(properties);
    return state;
  }

  @Override
  public AbstractStepNode getSpec(WorkflowMigrationContext context, GraphNode graphNode) {
    JiraCreateUpdate state = (JiraCreateUpdate) getState(graphNode);
    switch (state.getJiraAction()) {
      case UPDATE_TICKET:
        return buildUpdate(state);
      case CREATE_TICKET:
        return buildCreate(state);
      default:
        throw new IllegalStateException("Unsupported Approval Type");
    }
  }

  @Override
  public boolean areSimilar(GraphNode stepYaml1, GraphNode stepYaml2) {
    JiraCreateUpdate state1 = (JiraCreateUpdate) getState(stepYaml1);
    JiraCreateUpdate state2 = (JiraCreateUpdate) getState(stepYaml2);
    if (!state2.getJiraAction().equals(state1.getJiraAction())) {
      return false;
    }
    return true;
  }

  private JiraCreateStepNode buildCreate(JiraCreateUpdate state) {
    JiraCreateStepNode stepNode = new JiraCreateStepNode();
    baseSetup(state, stepNode);
    JiraCreateStepInfo stepInfo = JiraCreateStepInfo.builder()
                                      .connectorRef(RUNTIME_INPUT)
                                      .projectKey(ParameterField.createValueField(state.getProject()))
                                      .issueType(ParameterField.createValueField(state.getIssueType()))
                                      .fields(getFields(state))
                                      .delegateSelectors(ParameterField.createValueField(Collections.emptyList()))
                                      .build();

    stepNode.setJiraCreateStepInfo(stepInfo);
    return stepNode;
  }

  private static List<JiraField> getFields(JiraCreateUpdate state) {
    List<JiraField> jiraFields = new ArrayList<>();
    addJiraField(jiraFields, Field.SUMMARY, state.getSummary());
    addJiraField(jiraFields, Field.DESCRIPTION, state.getDescription());
    addJiraField(jiraFields, Field.PRIORITY, state.getPriority());
    addJiraField(jiraFields, Field.COMMENT, state.getComment());
    addJiraField(jiraFields, Field.STATUS, state.getStatus());
    if (EmptyPredicate.isNotEmpty(state.getLabels())) {
      addJiraField(jiraFields, Field.LABELS, String.join(",", state.getLabels()));
    }
    if (EmptyPredicate.isEmpty(state.getCustomFieldsMap())) {
      return jiraFields;
    }
    state.getCustomFieldsMap().forEach((key, value) -> addJiraField(jiraFields, key, value.getFieldValue()));
    return jiraFields;
  }

  private static void addJiraField(List<JiraField> jiraFields, String key, String value) {
    if (StringUtils.isNotBlank(value)) {
      jiraFields.add(JiraField.builder().name(key).value(ParameterField.createValueField(value)).build());
    }
  }

  private JiraUpdateStepNode buildUpdate(JiraCreateUpdate state) {
    JiraUpdateStepNode stepNode = new JiraUpdateStepNode();
    baseSetup(state, stepNode);
    TransitionTo transitionTo = null;
    if (StringUtils.isNotBlank(state.getStatus())) {
      transitionTo = TransitionTo.builder().status(ParameterField.createValueField(state.getStatus())).build();
    }
    JiraUpdateStepInfo stepInfo = JiraUpdateStepInfo.builder()
                                      .connectorRef(RUNTIME_INPUT)
                                      .issueKey(ParameterField.createValueField(state.getIssueId()))
                                      .transitionTo(transitionTo)
                                      .fields(getFields(state))
                                      .delegateSelectors(ParameterField.createValueField(Collections.emptyList()))
                                      .build();
    stepNode.setJiraUpdateStepInfo(stepInfo);
    return stepNode;
  }

  @Override
  public List<StepExpressionFunctor> getExpressionFunctor(
      WorkflowMigrationContext context, WorkflowPhase phase, PhaseStep phaseStep, GraphNode graphNode) {
    String sweepingOutputName = getSweepingOutputName(graphNode);
    if (StringUtils.isEmpty(sweepingOutputName)) {
      return Collections.emptyList();
    }
    return Lists.newArrayList(String.format("context.%s", sweepingOutputName), String.format("%s", sweepingOutputName))
        .stream()
        .map(exp
            -> StepOutput.builder()
                   .stageIdentifier(MigratorUtility.generateIdentifier(phase.getName()))
                   .stepIdentifier(MigratorUtility.generateIdentifier(graphNode.getName()))
                   .stepGroupIdentifier(MigratorUtility.generateIdentifier(phaseStep.getName()))
                   .expression(exp)
                   .build())
        .map(JiraFunctor::new)
        .collect(Collectors.toList());
  }
}
