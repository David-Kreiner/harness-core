/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */
package io.harness.execution.expansion;

import io.harness.beans.FeatureName;
import io.harness.execution.NodeExecution;
import io.harness.execution.PlanExecutionExpansion;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.ambiance.Level;
import io.harness.pms.contracts.execution.Status;
import io.harness.pms.data.PmsOutcome;
import io.harness.pms.data.stepparameters.PmsStepParameters;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.serializer.recaster.RecastOrchestrationUtils;
import io.harness.repositories.planExecutionJson.PlanExecutionExpansionRepository;
import io.harness.utils.PmsFeatureFlagService;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Singleton
@Slf4j
public class PlanExpansionServiceImpl implements PlanExpansionService {
  @Inject PlanExecutionExpansionRepository planExecutionExpansionRepository;

  @Inject PmsFeatureFlagService pmsFeatureFlagService;

  @Override
  public void addStepInputs(Ambiance ambiance, PmsStepParameters stepInputs) {
    if (shouldSkipUpdate(ambiance) || stepInputs == null) {
      return;
    }
    Update update = new Update();
    String stepInputsKey =
        String.format("%s.%s", getExpansionPathUsingLevels(ambiance), PlanExpansionConstants.STEP_INPUTS);
    update.set(stepInputsKey, Document.parse(RecastOrchestrationUtils.pruneRecasterAdditions(stepInputs.clone())));
    planExecutionExpansionRepository.update(ambiance.getPlanExecutionId(), update);
  }

  @Override
  public void addNameAndIdentifier(NodeExecution nodeExecution) {
    if (shouldSkipUpdate(nodeExecution.getAmbiance())) {
      return;
    }
    Update update = new Update();
    String key = getExpansionPathUsingLevels(nodeExecution.getAmbiance());
    String nameKey = String.format("%s.%s", key, PlanExpansionConstants.NAME);
    String identifierKey = String.format("%s.%s", key, PlanExpansionConstants.IDENTIFIER);
    update.set(nameKey, nodeExecution.getName());
    update.set(identifierKey, nodeExecution.getIdentifier());
    planExecutionExpansionRepository.update(nodeExecution.getPlanExecutionId(), update);
  }

  @Override
  public void addOutcomes(Ambiance ambiance, String name, PmsOutcome outcome) {
    if (shouldSkipUpdate(ambiance) || outcome == null) {
      return;
    }
    Update update = new Update();
    // Todo: We should store json without recaster annotations if possible but since we might need to convert the json
    // back to a given object and for that we require __recast field. Therefore keeping those self annotations
    update.set(getExpansionPathUsingLevels(ambiance) + String.format(".%s.", PlanExpansionConstants.OUTCOME) + name,
        Document.parse(RecastOrchestrationUtils.pruneRecasterAdditions(outcome.clone())));

    planExecutionExpansionRepository.update(ambiance.getPlanExecutionId(), update);
  }

  @Override
  public void create(String planExecutionId) {
    planExecutionExpansionRepository.save(PlanExecutionExpansion.builder().planExecutionId(planExecutionId).build());
  }

  @Override
  public String resolveExpression(String planExecutionId, String expression) {
    Criteria criteria = Criteria.where("planExecutionId").is(planExecutionId);
    Query query = new Query(criteria);
    query.fields().include(String.format("%s.", PlanExpansionConstants.EXPANDED_JSON) + expression);
    return RecastOrchestrationUtils.pruneRecasterAdditions(
        planExecutionExpansionRepository.find(query).getExpandedJson());
  }

  @Override
  public void updateStatus(Ambiance ambiance, Status status) {
    if (shouldSkipUpdate(ambiance)) {
      return;
    }
    Update update = new Update();
    update.set(getExpansionPathUsingLevels(ambiance) + String.format(".%s", PlanExpansionConstants.STATUS), status);
    planExecutionExpansionRepository.update(ambiance.getPlanExecutionId(), update);
  }

  @VisibleForTesting
  String getExpansionPathUsingLevels(Ambiance ambiance) {
    List<Level> levels = ambiance.getLevelsList();
    List<String> keyList = new ArrayList<>();
    keyList.add(PlanExpansionConstants.EXPANDED_JSON);
    for (Level level : levels) {
      if (!level.getSkipExpressionChain()) {
        keyList.add(level.getIdentifier());
      }
    }
    return String.join(".", keyList);
  }

  private boolean shouldSkipUpdate(Ambiance ambiance) {
    return !pmsFeatureFlagService.isEnabled(
               AmbianceUtils.getAccountId(ambiance), FeatureName.PIE_EXECUTION_JSON_SUPPORT)
        || AmbianceUtils.obtainCurrentLevel(ambiance).getSkipExpressionChain();
  }
}
