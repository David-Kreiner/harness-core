package software.wings.service.intfc;

import software.wings.beans.PipelineExecution;
import software.wings.beans.WorkflowExecution;
import software.wings.beans.artifact.Artifact;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.sm.ExecutionStatus;

/**
 * Created by anubhaw on 10/26/16.
 */
public interface PipelineService {
  /**
   * List pipeline executions page response.
   *
   * @param pageRequest the page request
   * @return the page response
   */
  PageResponse<PipelineExecution> listPipelineExecutions(PageRequest<PipelineExecution> pageRequest);

  /**
   * Update pipeline execution data.
   *  @param appId               the app id
   * @param pipelineExecutionId the pipeline execution id
   * @param workflowExecution   the workflow execution
   * @param startingStageExecution
   */
  void updatePipelineStageExecutionData(
      String appId, String pipelineExecutionId, WorkflowExecution workflowExecution, boolean startingStageExecution);

  /**
   * Update pipeline execution data.
   *
   * @param appId               the app id
   * @param pipelineExecutionId the pipeline execution id
   * @param artifact            the artifact
   */
  void updatePipelineExecutionData(String appId, String pipelineExecutionId, Artifact artifact);

  /**
   * Execute workflow execution.
   *
   * @param appId      the app id
   * @param pipelineId the pipeline id
   * @return the workflow execution
   */
  WorkflowExecution execute(String appId, String pipelineId);

  /**
   * Update pipeline execution data.
   *
   * @param appId               the app id
   * @param workflowExecutionId the workflow execution id
   * @param status              the status
   */
  void updatePipelineExecutionData(String appId, String workflowExecutionId, ExecutionStatus status);
}
