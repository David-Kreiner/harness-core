package software.wings.helpers.ext.k8s.request;

import io.harness.delegate.task.k8s.K8sTaskType;
import io.harness.k8s.model.HelmVersion;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import software.wings.beans.InstanceUnitType;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class K8sScaleTaskParameters extends K8sTaskParameters {
  private String workload;
  private Integer instances;
  private InstanceUnitType instanceUnitType;
  private Optional<Integer> maxInstances;
  private boolean skipSteadyStateCheck;
  @Builder
  public K8sScaleTaskParameters(String accountId, String appId, String commandName, String activityId,
      K8sTaskType k8sTaskType, K8sClusterConfig k8sClusterConfig, String workflowExecutionId, String releaseName,
      Integer timeoutIntervalInMin, String workload, Integer instances, InstanceUnitType instanceUnitType,
      Integer maxInstances, boolean skipSteadyStateCheck, HelmVersion helmVersion, boolean deprecateFabric8Enabled) {
    super(accountId, appId, commandName, activityId, k8sClusterConfig, workflowExecutionId, releaseName,
        timeoutIntervalInMin, k8sTaskType, helmVersion, deprecateFabric8Enabled);
    this.workload = workload;
    this.instances = instances;
    this.instanceUnitType = instanceUnitType;
    this.maxInstances = Optional.ofNullable(maxInstances);
    this.skipSteadyStateCheck = skipSteadyStateCheck;
  }
}
