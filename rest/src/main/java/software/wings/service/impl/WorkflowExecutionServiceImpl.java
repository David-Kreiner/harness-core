/**
 *
 */

package software.wings.service.impl;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.mongodb.morphia.mapping.Mapper.ID_KEY;
import static software.wings.api.WorkflowElement.WorkflowElementBuilder.aWorkflowElement;
import static software.wings.beans.ElementExecutionSummary.ElementExecutionSummaryBuilder.anElementExecutionSummary;
import static software.wings.dl.PageRequest.Builder.aPageRequest;
import static software.wings.sm.InstanceStatusSummary.InstanceStatusSummaryBuilder.anInstanceStatusSummary;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.wings.api.CommandStateExecutionData;
import software.wings.api.InstanceElement;
import software.wings.api.PhaseElement;
import software.wings.api.ServiceElement;
import software.wings.api.ServiceTemplateElement;
import software.wings.api.SimpleWorkflowParam;
import software.wings.api.WorkflowElement;
import software.wings.app.MainConfiguration;
import software.wings.beans.Application;
import software.wings.beans.CanaryWorkflowExecutionAdvisor;
import software.wings.beans.CountsByStatuses;
import software.wings.beans.ElementExecutionSummary;
import software.wings.beans.EntityType;
import software.wings.beans.Environment;
import software.wings.beans.ErrorCode;
import software.wings.beans.ExecutionArgs;
import software.wings.beans.Graph.Node;
import software.wings.beans.Orchestration;
import software.wings.beans.OrchestrationWorkflow;
import software.wings.beans.Pipeline;
import software.wings.beans.RequiredExecutionArgs;
import software.wings.beans.SearchFilter;
import software.wings.beans.SearchFilter.Operator;
import software.wings.beans.Service;
import software.wings.beans.ServiceInstance;
import software.wings.beans.SortOrder.OrderType;
import software.wings.beans.Workflow;
import software.wings.beans.WorkflowExecution;
import software.wings.beans.WorkflowType;
import software.wings.beans.artifact.Artifact;
import software.wings.beans.command.ServiceCommand;
import software.wings.common.Constants;
import software.wings.dl.PageRequest;
import software.wings.dl.PageResponse;
import software.wings.dl.WingsDeque;
import software.wings.dl.WingsPersistence;
import software.wings.exception.WingsException;
import software.wings.service.intfc.AppService;
import software.wings.service.intfc.ArtifactService;
import software.wings.service.intfc.EnvironmentService;
import software.wings.service.intfc.ServiceInstanceService;
import software.wings.service.intfc.ServiceResourceService;
import software.wings.service.intfc.WorkflowExecutionService;
import software.wings.service.intfc.WorkflowService;
import software.wings.sm.ContextElement;
import software.wings.sm.ContextElementType;
import software.wings.sm.ExecutionEventAdvisor;
import software.wings.sm.ExecutionInterrupt;
import software.wings.sm.ExecutionInterruptManager;
import software.wings.sm.ExecutionStatus;
import software.wings.sm.InstanceStatusSummary;
import software.wings.sm.StateExecutionInstance;
import software.wings.sm.StateMachine;
import software.wings.sm.StateMachineExecutionCallback;
import software.wings.sm.StateMachineExecutionSimulator;
import software.wings.sm.StateMachineExecutor;
import software.wings.sm.StateType;
import software.wings.sm.WorkflowStandardParams;
import software.wings.sm.states.ElementStateExecutionData;
import software.wings.utils.MapperUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.executable.ValidateOnExecution;

/**
 * The Class WorkflowExecutionServiceImpl.
 *
 * @author Rishi
 */
@Singleton
@ValidateOnExecution
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {
  private static final String COMMAND_NAME_PREF = "Command: ";
  private static final String WORKFLOW_NAME_PREF = "Workflow: ";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject private MainConfiguration mainConfiguration;
  @Inject private WingsPersistence wingsPersistence;
  @Inject private StateMachineExecutor stateMachineExecutor;
  @Inject private EnvironmentService environmentService;
  @Inject private ExecutionInterruptManager executionInterruptManager;
  @Inject private ServiceResourceService serviceResourceService;
  @Inject private ServiceInstanceService serviceInstanceService;
  @Inject private ArtifactService artifactService;
  @Inject private StateMachineExecutionSimulator stateMachineExecutionSimulator;
  @Inject private GraphRenderer graphRenderer;
  @Inject private AppService appService;
  @Inject private WorkflowService workflowService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void trigger(String appId, String stateMachineId, String executionUuid, String executionName) {
    trigger(appId, stateMachineId, executionUuid, executionName, null);
  }

  /**
   * Trigger.
   *
   * @param appId          the app id
   * @param stateMachineId the state machine id
   * @param executionUuid  the execution uuid
   * @param executionName  the execution name
   * @param callback       the callback
   */
  void trigger(String appId, String stateMachineId, String executionUuid, String executionName,
      StateMachineExecutionCallback callback) {
    stateMachineExecutor.execute(appId, stateMachineId, executionUuid, executionName, null, callback);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PageResponse<WorkflowExecution> listExecutions(
      PageRequest<WorkflowExecution> pageRequest, boolean includeGraph) {
    return listExecutions(pageRequest, includeGraph, false, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PageResponse<WorkflowExecution> listExecutions(PageRequest<WorkflowExecution> pageRequest,
      boolean includeGraph, boolean runningOnly, boolean withBreakdownAndSummary) {
    PageResponse<WorkflowExecution> res = wingsPersistence.query(WorkflowExecution.class, pageRequest);
    if (res == null || res.size() == 0) {
      return res;
    }
    if (withBreakdownAndSummary) {
      res.forEach(this ::refreshBreakdown);

      res.forEach(this ::refreshSummaries);
    }

    if (!includeGraph) {
      return res;
    }
    for (WorkflowExecution workflowExecution : res) {
      if (!runningOnly || workflowExecution.isRunningStatus() || workflowExecution.isPausedStatus()) {
        // populateGraph(workflowExecution, null, null, null, false);
        populateNodeHierarchy(workflowExecution, false);
      }
    }
    return res;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WorkflowExecution getExecutionDetails(String appId, String workflowExecutionId) {
    return getExecutionDetails(appId, workflowExecutionId, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WorkflowExecution getExecutionDetails(
      String appId, String workflowExecutionId, List<String> expandedGroupIds) {
    WorkflowExecution workflowExecution = getExecutionDetailsWithoutGraph(appId, workflowExecutionId);

    if (expandedGroupIds == null) {
      expandedGroupIds = new ArrayList<>();
    }
    if (workflowExecution != null) {
      populateNodeHierarchy(workflowExecution, false);
    }
    workflowExecution.setExpandedGroupIds(expandedGroupIds);
    return workflowExecution;
  }

  @Override
  public WorkflowExecution getExecutionDetailsWithoutGraph(String appId, String workflowExecutionId) {
    WorkflowExecution workflowExecution = wingsPersistence.get(WorkflowExecution.class, appId, workflowExecutionId);

    if (workflowExecution.getExecutionArgs() != null) {
      if (workflowExecution.getExecutionArgs().getServiceInstanceIdNames() != null) {
        PageRequest<ServiceInstance> pageRequest =
            aPageRequest()
                .addFilter("appId", Operator.EQ, appId)
                .addFilter("uuid", Operator.IN,
                    workflowExecution.getExecutionArgs().getServiceInstanceIdNames().keySet().toArray())
                .build();
        workflowExecution.getExecutionArgs().setServiceInstances(
            serviceInstanceService.list(pageRequest).getResponse());
      }
      if (workflowExecution.getExecutionArgs().getArtifactIdNames() != null) {
        PageRequest<Artifact> pageRequest =
            aPageRequest()
                .addFilter("appId", Operator.EQ, appId)
                .addFilter(
                    "uuid", Operator.IN, workflowExecution.getExecutionArgs().getArtifactIdNames().keySet().toArray())
                .build();
        workflowExecution.getExecutionArgs().setArtifacts(artifactService.list(pageRequest, false).getResponse());
      }
    }
    refreshBreakdown(workflowExecution);
    refreshSummaries(workflowExecution);
    return workflowExecution;
  }

  private void populateNodeHierarchy(WorkflowExecution workflowExecution, boolean expandLastOnly) {
    List<StateExecutionInstance> allInstances = queryAllInstances(workflowExecution);
    if (allInstances == null || allInstances.isEmpty()) {
      return;
    }
    Map<String, StateExecutionInstance> allInstancesIdMap =
        allInstances.stream().collect(toMap(StateExecutionInstance::getUuid, identity()));

    StateMachine sm =
        wingsPersistence.get(StateMachine.class, workflowExecution.getAppId(), workflowExecution.getStateMachineId());
    String commandName = null;
    if (workflowExecution.getExecutionArgs() != null) {
      commandName = workflowExecution.getExecutionArgs().getCommandName();
    }
    List<StateExecutionInstance> pausedInstances =
        allInstances.stream()
            .filter(i -> (i.getStatus() == ExecutionStatus.PAUSED || i.getStatus() == ExecutionStatus.PAUSED_ON_ERROR))
            .collect(Collectors.toList());
    if (pausedInstances != null && !pausedInstances.isEmpty()) {
      workflowExecution.setStatus(ExecutionStatus.PAUSED);
    }
    workflowExecution.setExecutionNode(
        graphRenderer.generateHierarchyNode(allInstancesIdMap, sm.getInitialStateName(), null, true, true));
  }

  private List<StateExecutionInstance> queryAllInstances(WorkflowExecution workflowExecution) {
    PageRequest<StateExecutionInstance> req = aPageRequest()
                                                  .withLimit(PageRequest.UNLIMITED)
                                                  .addFilter("appId", Operator.EQ, workflowExecution.getAppId())
                                                  .addFilter("executionUuid", Operator.EQ, workflowExecution.getUuid())
                                                  .addFieldsExcluded("contextElements", "callback")
                                                  .build();

    return wingsPersistence.query(StateExecutionInstance.class, req);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WorkflowExecution triggerPipelineExecution(String appId, String pipelineId, ExecutionArgs executionArgs) {
    return triggerPipelineExecution(appId, pipelineId, executionArgs, null);
  }

  /**
   * Trigger pipeline execution workflow execution.
   *
   * @param appId                   the app id
   * @param pipelineId              the pipeline id
   * @param executionArgs           the execution args
   * @param workflowExecutionUpdate the workflow execution update  @return the workflow execution
   * @return the workflow execution
   */
  public WorkflowExecution triggerPipelineExecution(
      String appId, String pipelineId, ExecutionArgs executionArgs, WorkflowExecutionUpdate workflowExecutionUpdate) {
    Pipeline pipeline = wingsPersistence.get(Pipeline.class, appId, pipelineId);
    if (pipeline == null) {
      throw new WingsException(ErrorCode.NON_EXISTING_PIPELINE);
    }
    List<WorkflowExecution> runningWorkflowExecutions =
        getRunningWorkflowExecutions(WorkflowType.PIPELINE, appId, pipelineId);
    if (runningWorkflowExecutions != null) {
      for (WorkflowExecution workflowExecution : runningWorkflowExecutions) {
        if (workflowExecution.getStatus() == ExecutionStatus.NEW) {
          throw new WingsException(ErrorCode.PIPELINE_ALREADY_TRIGGERED, "pilelineName", pipeline.getName());
        }
        if (workflowExecution.getStatus() == ExecutionStatus.RUNNING) {
          // Analyze if pipeline is in initial stage
        }
      }
    }

    StateMachine stateMachine = workflowService.readLatest(appId, pipelineId);
    if (stateMachine == null) {
      throw new WingsException("No stateMachine associated with " + pipelineId);
    }
    WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setAppId(appId);
    workflowExecution.setWorkflowId(pipelineId);
    workflowExecution.setWorkflowType(WorkflowType.PIPELINE);
    workflowExecution.setStateMachineId(stateMachine.getUuid());

    WorkflowStandardParams stdParams = new WorkflowStandardParams();
    stdParams.setAppId(appId);
    if (executionArgs.getArtifacts() != null && !executionArgs.getArtifacts().isEmpty()) {
      stdParams.setArtifactIds(
          executionArgs.getArtifacts().stream().map(Artifact::getUuid).collect(Collectors.toList()));
    }

    return triggerExecution(workflowExecution, stateMachine, workflowExecutionUpdate, stdParams);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WorkflowExecution triggerOrchestrationExecution(
      String appId, String envId, String orchestrationId, ExecutionArgs executionArgs) {
    return triggerOrchestrationWorkflowExecution(appId, envId, orchestrationId, executionArgs, null);
  }

  /**
   * Trigger orchestration execution workflow execution.
   *
   * @param appId                   the app id
   * @param envId                   the env id
   * @param orchestrationId         the orchestration id
   * @param executionArgs           the execution args
   * @param workflowExecutionUpdate the workflow execution update
   * @return the workflow execution
   */
  @Override
  public WorkflowExecution triggerOrchestrationExecution(String appId, String envId, String orchestrationId,
      ExecutionArgs executionArgs, WorkflowExecutionUpdate workflowExecutionUpdate) {
    List<WorkflowExecution> runningWorkflowExecutions =
        getRunningWorkflowExecutions(WorkflowType.ORCHESTRATION, appId, orchestrationId);
    if (runningWorkflowExecutions != null && runningWorkflowExecutions.size() > 0) {
      throw new WingsException("Orchestration has already been triggered");
    }
    // TODO - validate list of artifact Ids if it's matching for all the services involved in this orchestration

    StateMachine stateMachine = workflowService.readLatest(appId, orchestrationId);
    if (stateMachine == null) {
      throw new WingsException("No stateMachine associated with " + orchestrationId);
    }

    Orchestration orchestration = wingsPersistence.get(Orchestration.class, appId, orchestrationId);

    WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setAppId(appId);
    workflowExecution.setEnvId(envId);
    workflowExecution.setWorkflowId(orchestrationId);
    workflowExecution.setName(WORKFLOW_NAME_PREF + orchestration.getName());
    workflowExecution.setWorkflowType(WorkflowType.ORCHESTRATION);
    workflowExecution.setStateMachineId(stateMachine.getUuid());
    workflowExecution.setExecutionArgs(executionArgs);

    WorkflowStandardParams stdParams = new WorkflowStandardParams();
    stdParams.setAppId(appId);
    stdParams.setEnvId(envId);
    if (executionArgs.getArtifacts() != null && !executionArgs.getArtifacts().isEmpty()) {
      stdParams.setArtifactIds(
          executionArgs.getArtifacts().stream().map(Artifact::getUuid).collect(Collectors.toList()));
    }
    stdParams.setExecutionCredential(executionArgs.getExecutionCredential());

    return triggerExecution(workflowExecution, stateMachine, workflowExecutionUpdate, stdParams);
  }

  /**
   * Trigger orchestration execution workflow execution.
   *
   * @param appId                   the app id
   * @param envId                   the env id
   * @param orchestrationId         the orchestration id
   * @param executionArgs           the execution args
   * @param workflowExecutionUpdate the workflow execution update
   * @return the workflow execution
   */
  public WorkflowExecution triggerOrchestrationWorkflowExecution(String appId, String envId, String orchestrationId,
      ExecutionArgs executionArgs, WorkflowExecutionUpdate workflowExecutionUpdate) {
    List<WorkflowExecution> runningWorkflowExecutions =
        getRunningWorkflowExecutions(WorkflowType.ORCHESTRATION_WORKFLOW, appId, orchestrationId);
    if (runningWorkflowExecutions != null && runningWorkflowExecutions.size() > 0) {
      throw new WingsException("Orchestration Workflow has already been triggered");
    }
    // TODO - validate list of artifact Ids if it's matching for all the services involved in this orchestration

    StateMachine stateMachine = workflowService.readLatest(appId, orchestrationId);
    if (stateMachine == null) {
      throw new WingsException("No stateMachine associated with " + orchestrationId);
    }

    OrchestrationWorkflow orchestration = wingsPersistence.get(OrchestrationWorkflow.class, appId, orchestrationId);

    WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setAppId(appId);
    workflowExecution.setEnvId(envId);
    workflowExecution.setWorkflowId(orchestrationId);
    workflowExecution.setName(WORKFLOW_NAME_PREF + orchestration.getName());
    workflowExecution.setWorkflowType(WorkflowType.ORCHESTRATION_WORKFLOW);
    workflowExecution.setStateMachineId(stateMachine.getUuid());
    workflowExecution.setExecutionArgs(executionArgs);

    WorkflowStandardParams stdParams = new WorkflowStandardParams();
    stdParams.setAppId(appId);
    stdParams.setEnvId(envId);
    if (executionArgs.getArtifacts() != null && !executionArgs.getArtifacts().isEmpty()) {
      stdParams.setArtifactIds(
          executionArgs.getArtifacts().stream().map(Artifact::getUuid).collect(Collectors.toList()));
    }
    stdParams.setExecutionCredential(executionArgs.getExecutionCredential());

    return triggerExecution(
        workflowExecution, stateMachine, new CanaryWorkflowExecutionAdvisor(), workflowExecutionUpdate, stdParams);
  }

  private WorkflowExecution triggerExecution(WorkflowExecution workflowExecution, StateMachine stateMachine,
      WorkflowExecutionUpdate workflowExecutionUpdate, WorkflowStandardParams stdParams,
      ContextElement... contextElements) {
    return triggerExecution(workflowExecution, stateMachine, null, workflowExecutionUpdate, stdParams, contextElements);
  }

  private WorkflowExecution triggerExecution(WorkflowExecution workflowExecution, StateMachine stateMachine,
      ExecutionEventAdvisor workflowExecutionAdvisor, WorkflowExecutionUpdate workflowExecutionUpdate,
      WorkflowStandardParams stdParams, ContextElement... contextElements) {
    Application app = appService.get(workflowExecution.getAppId());
    workflowExecution.setAppName(app.getName());
    if (workflowExecution.getEnvId() != null) {
      Environment env = environmentService.get(workflowExecution.getAppId(), workflowExecution.getEnvId(), false);
      workflowExecution.setEnvName(env.getName());
      workflowExecution.setEnvType(env.getEnvironmentType());
    }

    if (workflowExecution.getExecutionArgs() != null) {
      if (workflowExecution.getExecutionArgs().getServiceInstances() != null) {
        List<String> serviceInstanceIds = workflowExecution.getExecutionArgs()
                                              .getServiceInstances()
                                              .stream()
                                              .map(ServiceInstance::getUuid)
                                              .collect(Collectors.toList());
        PageRequest<ServiceInstance> pageRequest = aPageRequest()
                                                       .addFilter("appId", Operator.EQ, workflowExecution.getAppId())
                                                       .addFilter("uuid", Operator.IN, serviceInstanceIds.toArray())
                                                       .build();
        List<ServiceInstance> serviceInstances = serviceInstanceService.list(pageRequest).getResponse();

        if (serviceInstances == null || serviceInstances.size() != serviceInstanceIds.size()) {
          logger.error("Service instances argument and valid service instance retrieved size not matching");
          throw new WingsException(ErrorCode.INVALID_REQUEST, "message", "Invalid service instances");
        }
        workflowExecution.getExecutionArgs().setServiceInstanceIdNames(
            serviceInstances.stream().collect(Collectors.toMap(ServiceInstance::getUuid,
                serviceInstance -> serviceInstance.getHostName() + ":" + serviceInstance.getServiceName())));
      }

      if (workflowExecution.getExecutionArgs().getArtifacts() != null
          && !workflowExecution.getExecutionArgs().getArtifacts().isEmpty()) {
        List<String> artifactIds = workflowExecution.getExecutionArgs()
                                       .getArtifacts()
                                       .stream()
                                       .map(Artifact::getUuid)
                                       .collect(Collectors.toList());
        PageRequest<Artifact> pageRequest = aPageRequest()
                                                .addFilter("appId", Operator.EQ, workflowExecution.getAppId())
                                                .addFilter("uuid", Operator.IN, artifactIds.toArray())
                                                .build();
        List<Artifact> artifacts = artifactService.list(pageRequest, false).getResponse();

        if (artifacts == null || artifacts.size() != artifactIds.size()) {
          logger.error("Artifact argument and valid artifact retrieved size not matching");
          throw new WingsException(ErrorCode.INVALID_REQUEST, "message", "Invalid artifact");
        }
        workflowExecution.getExecutionArgs().setArtifactIdNames(
            artifacts.stream().collect(Collectors.toMap(Artifact::getUuid, Artifact::getDisplayName)));

        List<ServiceElement> services = new ArrayList<>();
        artifacts.forEach(artifact -> {
          artifact.getServiceIds().forEach(serviceId -> {
            Service service = serviceResourceService.get(artifact.getAppId(), serviceId);
            ServiceElement se = new ServiceElement();
            MapperUtils.mapObject(service, se);
            services.add(se);
          });
        });
        stdParams.setServices(services);
      }
      workflowExecution.setErrorStrategy(workflowExecution.getExecutionArgs().getErrorStrategy());
    }

    String workflowExecutionId = wingsPersistence.save(workflowExecution);
    StateExecutionInstance stateExecutionInstance = new StateExecutionInstance();
    stateExecutionInstance.setAppId(workflowExecution.getAppId());
    stateExecutionInstance.setExecutionName(workflowExecution.getName());
    stateExecutionInstance.setExecutionUuid(workflowExecutionId);
    stateExecutionInstance.setExecutionType(workflowExecution.getWorkflowType());

    if (workflowExecutionUpdate == null) {
      workflowExecutionUpdate = new WorkflowExecutionUpdate();
    }
    workflowExecutionUpdate.setAppId(workflowExecution.getAppId());
    workflowExecutionUpdate.setWorkflowExecutionId(workflowExecutionId);
    stateExecutionInstance.setCallback(workflowExecutionUpdate);
    if (workflowExecutionAdvisor != null) {
      stateExecutionInstance.setExecutionEventAdvisors(Lists.newArrayList(workflowExecutionAdvisor));
    }

    stdParams.setErrorStrategy(workflowExecution.getErrorStrategy());
    String workflowUrl = mainConfiguration.getPortal().getUrl() + "/"
        + String.format(mainConfiguration.getPortal().getExecutionUrlPattern(), workflowExecution.getAppId(),
              workflowExecution.getEnvId(), workflowExecution.getUuid());
    WorkflowElement workflowElement = aWorkflowElement()
                                          .withUuid(workflowExecutionId)
                                          .withName(workflowExecution.getName())
                                          .withUrl(workflowUrl)
                                          .build();
    stdParams.setWorkflowElement(workflowElement);

    WingsDeque<ContextElement> elements = new WingsDeque<>();
    elements.push(stdParams);
    if (contextElements != null) {
      for (ContextElement contextElement : contextElements) {
        elements.push(contextElement);
      }
    }
    stateExecutionInstance.setContextElements(elements);
    stateMachineExecutor.execute(stateMachine, stateExecutionInstance);

    // TODO: findAndModify
    Query<WorkflowExecution> query = wingsPersistence.createQuery(WorkflowExecution.class)
                                         .field("appId")
                                         .equal(workflowExecution.getAppId())
                                         .field(ID_KEY)
                                         .equal(workflowExecutionId)
                                         .field("status")
                                         .equal(ExecutionStatus.NEW);
    UpdateOperations<WorkflowExecution> updateOps = wingsPersistence.createUpdateOperations(WorkflowExecution.class)
                                                        .set("status", ExecutionStatus.RUNNING)
                                                        .set("startTs", System.currentTimeMillis());

    wingsPersistence.update(query, updateOps);

    workflowExecution =
        wingsPersistence.get(WorkflowExecution.class, workflowExecution.getAppId(), workflowExecutionId);
    notifyWorkflowExecution(workflowExecution);
    return workflowExecution;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WorkflowExecution triggerEnvExecution(String appId, String envId, ExecutionArgs executionArgs) {
    return triggerEnvExecution(appId, envId, executionArgs, null);
  }

  @Override
  public void incrementInProgressCount(String appId, String workflowExecutionId, int inc) {
    UpdateOperations<WorkflowExecution> ops = wingsPersistence.createUpdateOperations(WorkflowExecution.class);
    ops.inc("breakdown.inprogress", inc);
    wingsPersistence.update(wingsPersistence.createQuery(WorkflowExecution.class)
                                .field("appId")
                                .equal(appId)
                                .field(ID_KEY)
                                .equal(workflowExecutionId),
        ops);
  }

  @Override
  public void incrementSuccess(String appId, String workflowExecutionId, int inc) {
    UpdateOperations<WorkflowExecution> ops = wingsPersistence.createUpdateOperations(WorkflowExecution.class);
    ops.inc("breakdown.success", inc);
    ops.inc("breakdown.inprogress", -1 * inc);
    wingsPersistence.update(wingsPersistence.createQuery(WorkflowExecution.class)
                                .field("appId")
                                .equal(appId)
                                .field(ID_KEY)
                                .equal(workflowExecutionId),
        ops);
  }

  @Override
  public void incrementFailed(String appId, String workflowExecutionId, Integer inc) {
    UpdateOperations<WorkflowExecution> ops = wingsPersistence.createUpdateOperations(WorkflowExecution.class);
    ops.inc("breakdown.failed", inc);
    ops.inc("breakdown.inprogress", -1 * inc);
    wingsPersistence.update(wingsPersistence.createQuery(WorkflowExecution.class)
                                .field("appId")
                                .equal(appId)
                                .field(ID_KEY)
                                .equal(workflowExecutionId),
        ops);
  }

  /**
   * Trigger env execution workflow execution.
   *
   * @param appId                   the app id
   * @param envId                   the env id
   * @param executionArgs           the execution args
   * @param workflowExecutionUpdate the workflow execution update
   * @return the workflow execution
   */
  WorkflowExecution triggerEnvExecution(
      String appId, String envId, ExecutionArgs executionArgs, WorkflowExecutionUpdate workflowExecutionUpdate) {
    if (executionArgs.getWorkflowType() == WorkflowType.ORCHESTRATION) {
      logger.debug("Received an orchestrated execution request");
      if (executionArgs.getOrchestrationId() == null) {
        logger.error("orchestrationId is null for an orchestrated execution");
        throw new WingsException(
            ErrorCode.INVALID_REQUEST, "message", "orchestrationId is null for an orchestrated execution");
      }
      return triggerOrchestrationExecution(appId, envId, executionArgs.getOrchestrationId(), executionArgs);
    } else if (executionArgs.getWorkflowType() == WorkflowType.SIMPLE) {
      logger.debug("Received an simple execution request");
      if (executionArgs.getServiceId() == null) {
        logger.error("serviceId is null for a simple execution");
        throw new WingsException(ErrorCode.INVALID_REQUEST, "message", "serviceId is null for a simple execution");
      }
      if (executionArgs.getServiceInstances() == null || executionArgs.getServiceInstances().size() == 0) {
        logger.error("serviceInstances are empty for a simple execution");
        throw new WingsException(
            ErrorCode.INVALID_REQUEST, "message", "serviceInstances are empty for a simple execution");
      }

      return triggerSimpleExecution(appId, envId, executionArgs, workflowExecutionUpdate);

    } else {
      throw new WingsException(ErrorCode.INVALID_ARGUMENT, "args", "workflowType");
    }
  }

  /**
   * Trigger simple execution workflow execution.
   *
   * @param appId         the app id
   * @param envId         the env id
   * @param executionArgs the execution args
   * @return the workflow execution
   */
  private WorkflowExecution triggerSimpleExecution(
      String appId, String envId, ExecutionArgs executionArgs, WorkflowExecutionUpdate workflowExecutionUpdate) {
    Workflow workflow = workflowService.readLatestSimpleWorkflow(appId);
    String orchestrationId = workflow.getUuid();

    StateMachine stateMachine = workflowService.readLatest(appId, orchestrationId);
    if (stateMachine == null) {
      throw new WingsException("No stateMachine associated with " + orchestrationId);
    }

    WorkflowExecution workflowExecution = new WorkflowExecution();
    workflowExecution.setAppId(appId);
    workflowExecution.setEnvId(envId);
    workflowExecution.setWorkflowType(WorkflowType.SIMPLE);
    workflowExecution.setStateMachineId(stateMachine.getUuid());
    workflowExecution.setTotal(executionArgs.getServiceInstances().size());
    Service service = serviceResourceService.get(appId, executionArgs.getServiceId());
    workflowExecution.setName(COMMAND_NAME_PREF + service.getName() + "/" + executionArgs.getCommandName());
    workflowExecution.setWorkflowId(workflow.getUuid());
    workflowExecution.setExecutionArgs(executionArgs);

    WorkflowStandardParams stdParams = new WorkflowStandardParams();
    stdParams.setAppId(appId);
    stdParams.setEnvId(envId);
    if (executionArgs.getArtifacts() != null && !executionArgs.getArtifacts().isEmpty()) {
      stdParams.setArtifactIds(
          executionArgs.getArtifacts().stream().map(Artifact::getUuid).collect(Collectors.toList()));
    }
    stdParams.setExecutionCredential(executionArgs.getExecutionCredential());

    SimpleWorkflowParam simpleOrchestrationParams = new SimpleWorkflowParam();
    simpleOrchestrationParams.setServiceId(executionArgs.getServiceId());
    if (executionArgs.getServiceInstances() != null) {
      simpleOrchestrationParams.setInstanceIds(
          executionArgs.getServiceInstances().stream().map(ServiceInstance::getUuid).collect(Collectors.toList()));
    }
    simpleOrchestrationParams.setExecutionStrategy(executionArgs.getExecutionStrategy());
    simpleOrchestrationParams.setCommandName(executionArgs.getCommandName());
    return triggerExecution(
        workflowExecution, stateMachine, workflowExecutionUpdate, stdParams, simpleOrchestrationParams);
  }

  private List<WorkflowExecution> getRunningWorkflowExecutions(
      WorkflowType workflowType, String appId, String workflowId) {
    PageRequest<WorkflowExecution> pageRequest = new PageRequest<>();

    SearchFilter filter = new SearchFilter();
    filter.setFieldName("appId");
    filter.setFieldValues(appId);
    filter.setOp(Operator.EQ);
    pageRequest.addFilter(filter);

    filter = new SearchFilter();
    filter.setFieldName("workflowId");
    filter.setFieldValues(workflowId);
    filter.setOp(Operator.EQ);
    pageRequest.addFilter(filter);

    filter = new SearchFilter();
    filter.setFieldName("workflowType");
    filter.setFieldValues(workflowType);
    filter.setOp(Operator.EQ);
    pageRequest.addFilter(filter);

    filter = new SearchFilter();
    filter.setFieldName("status");
    List<Object> statuses = new ArrayList<>();
    statuses.add(ExecutionStatus.NEW);
    statuses.add(ExecutionStatus.RUNNING);
    filter.setFieldValues(statuses);
    filter.setOp(Operator.IN);
    pageRequest.addFilter(filter);

    PageResponse<WorkflowExecution> pageResponse = wingsPersistence.query(WorkflowExecution.class, pageRequest);
    if (pageResponse == null) {
      return null;
    }
    return pageResponse.getResponse();
  }

  @Override
  public ExecutionInterrupt triggerExecutionInterrupt(ExecutionInterrupt executionInterrupt) {
    String executionUuid = executionInterrupt.getExecutionUuid();
    WorkflowExecution workflowExecution =
        wingsPersistence.get(WorkflowExecution.class, executionInterrupt.getAppId(), executionUuid);
    if (workflowExecution == null) {
      throw new WingsException(
          ErrorCode.INVALID_ARGUMENT, "args", "no workflowExecution for executionUuid:" + executionUuid);
    }

    return executionInterruptManager.registerExecutionInterrupt(executionInterrupt);
  }

  @Override
  public RequiredExecutionArgs getRequiredExecutionArgs(String appId, String envId, ExecutionArgs executionArgs) {
    if (executionArgs.getWorkflowType() == null) {
      logger.error("workflowType is null");
      throw new WingsException(ErrorCode.INVALID_REQUEST, "message", "workflowType is null");
    }

    if (executionArgs.getWorkflowType() == WorkflowType.ORCHESTRATION
        || executionArgs.getWorkflowType() == WorkflowType.ORCHESTRATION_WORKFLOW) {
      logger.debug("Received an orchestrated execution request");
      if (executionArgs.getOrchestrationId() == null) {
        logger.error("orchestrationId is null for an orchestrated execution");
        throw new WingsException(
            ErrorCode.INVALID_REQUEST, "message", "orchestrationId is null for an orchestrated execution");
      }

      OrchestrationWorkflow orchestration =
          wingsPersistence.get(OrchestrationWorkflow.class, appId, executionArgs.getOrchestrationId());
      if (orchestration == null) {
        logger.error("Invalid orchestrationId");
        throw new WingsException(
            ErrorCode.INVALID_REQUEST, "message", "Invalid orchestrationId: " + executionArgs.getOrchestrationId());
      }

      StateMachine stateMachine = workflowService.readLatest(appId, executionArgs.getOrchestrationId());
      if (stateMachine == null) {
        throw new WingsException(ErrorCode.INVALID_REQUEST, "message", "Associated state machine not found");
      }

      RequiredExecutionArgs requiredExecutionArgs = new RequiredExecutionArgs();
      requiredExecutionArgs.setEntityTypes(orchestration.getRequiredEntityTypes());
      return requiredExecutionArgs;
      // return stateMachineExecutionSimulator.getRequiredExecutionArgs(appId, envId, stateMachine, executionArgs);

    } else if (executionArgs.getWorkflowType() == WorkflowType.SIMPLE) {
      logger.debug("Received an simple execution request");
      if (executionArgs.getServiceId() == null) {
        logger.error("serviceId is null for a simple execution");
        throw new WingsException(ErrorCode.INVALID_REQUEST, "message", "serviceId is null for a simple execution");
      }
      if (executionArgs.getServiceInstances() == null || executionArgs.getServiceInstances().size() == 0) {
        logger.error("serviceInstances are empty for a simple execution");
        throw new WingsException(
            ErrorCode.INVALID_REQUEST, "message", "serviceInstances are empty for a simple execution");
      }
      RequiredExecutionArgs requiredExecutionArgs = new RequiredExecutionArgs();
      if (StringUtils.isNotBlank(executionArgs.getCommandName())) {
        ServiceCommand command = serviceResourceService.getCommandByName(
            appId, executionArgs.getServiceId(), envId, executionArgs.getCommandName());
        if (command.getCommand().isArtifactNeeded()) {
          requiredExecutionArgs.getEntityTypes().add(EntityType.ARTIFACT);
        }
      }
      List<String> serviceInstanceIds =
          executionArgs.getServiceInstances().stream().map(ServiceInstance::getUuid).collect(Collectors.toList());
      Set<EntityType> infraReqEntityTypes =
          stateMachineExecutionSimulator.getInfrastructureRequiredEntityType(appId, serviceInstanceIds);
      if (infraReqEntityTypes != null) {
        requiredExecutionArgs.getEntityTypes().addAll(infraReqEntityTypes);
      }
      return requiredExecutionArgs;
    }

    return null;
  }

  private void notifyWorkflowExecution(WorkflowExecution workflowExecution) {
    EntityType entityType = EntityType.ORCHESTRATED_DEPLOYMENT;
    if (workflowExecution.getWorkflowType() == WorkflowType.SIMPLE) {
      entityType = EntityType.SIMPLE_DEPLOYMENT;
    }
    //
    //    History history =
    //    History.Builder.aHistory().withAppId(workflowExecution.getAppId()).withEventType(EventType.CREATED).withEntityType(entityType)
    //        .withEntityId(workflowExecution.getUuid()).withEntityName(workflowExecution.getName()).withEntityNewValue(workflowExecution)
    //        .withShortDescription(workflowExecution.getName() + " started").withTitle(workflowExecution.getName() + "
    //        started").build();
    //    historyService.createAsync(history);
  }

  @Override
  public CountsByStatuses getBreakdown(String appId, String workflowExecutionId) {
    WorkflowExecution workflowExecution = wingsPersistence.get(WorkflowExecution.class, appId, workflowExecutionId);
    refreshBreakdown(workflowExecution);
    return workflowExecution.getBreakdown();
  }

  @Override
  public Node getExecutionDetailsForNode(String appId, String workflowExecutionId, String stateExecutionInstanceId) {
    StateExecutionInstance stateExecutionInstance =
        wingsPersistence.get(StateExecutionInstance.class, appId, stateExecutionInstanceId);
    return graphRenderer.convertToNode(stateExecutionInstance);
  }

  @Override
  public void deleteByWorkflow(String appId, String workflowId) {
    wingsPersistence.createQuery(WorkflowExecution.class)
        .field("appId")
        .equal(appId)
        .field("workflowId")
        .equal(workflowId)
        .asList()
        .forEach(workflowExecution -> {
          wingsPersistence.delete(workflowExecution);
          wingsPersistence.createQuery(StateExecutionInstance.class)
              .field("appId")
              .equal(appId)
              .field("stateMachineId")
              .equal(workflowExecution.getStateMachineId())
              .forEach(stateExecutionInstance -> {
                wingsPersistence.delete(stateExecutionInstance);
                wingsPersistence.delete(wingsPersistence.createQuery(ExecutionInterrupt.class)
                                            .field("appId")
                                            .equal(appId)
                                            .field("stateExecutionInstanceId")
                                            .equal(stateExecutionInstance.getUuid()));
              });
        });
  }

  private void refreshSummaries(WorkflowExecution workflowExecution) {
    if (workflowExecution.getServiceExecutionSummaries() != null) {
      return;
    }

    List<ElementExecutionSummary> serviceExecutionSummaries = new ArrayList<>();
    if (workflowExecution.getWorkflowType() == WorkflowType.ORCHESTRATION_WORKFLOW) {
      OrchestrationWorkflow orchestrationWorkflow =
          workflowService.readOrchestrationWorkflow(workflowExecution.getAppId(), workflowExecution.getWorkflowId());
      if (orchestrationWorkflow != null) {
        List<Service> services = orchestrationWorkflow.getServices();
        if (services != null) {
          services.forEach(service -> {
            ServiceElement serviceElement = ServiceElement.Builder.aServiceElement()
                                                .withUuid(service.getUuid())
                                                .withName(service.getName())
                                                .build();
            ElementExecutionSummary elementSummary = anElementExecutionSummary()
                                                         .withContextElement(serviceElement)
                                                         .withStatus(ExecutionStatus.QUEUED)
                                                         .build();
            serviceExecutionSummaries.add(elementSummary);
          });
        }
      }
    }
    Map<String, ElementExecutionSummary> serviceExecutionSummaryMap = serviceExecutionSummaries.stream().collect(
        Collectors.toMap(summary -> summary.getContextElement().getUuid(), Function.identity()));

    populateServiceSummary(serviceExecutionSummaryMap, workflowExecution);

    if (!serviceExecutionSummaryMap.isEmpty()) {
      Collections.sort(serviceExecutionSummaries, ElementExecutionSummary.startTsComparator);
      workflowExecution.setServiceExecutionSummaries(serviceExecutionSummaries);

      if (workflowExecution.getStatus() == ExecutionStatus.SUCCESS
          || workflowExecution.getStatus() == ExecutionStatus.FAILED
          || workflowExecution.getStatus() == ExecutionStatus.ERROR
          || workflowExecution.getStatus() == ExecutionStatus.ABORTED) {
        wingsPersistence.updateField(WorkflowExecution.class, workflowExecution.getUuid(), "serviceExecutionSummaries",
            workflowExecution.getServiceExecutionSummaries());
      }
    }
  }

  private void populateServiceSummary(
      Map<String, ElementExecutionSummary> serviceSummaryMap, WorkflowExecution workflowExecution) {
    PageRequest<StateExecutionInstance> pageRequest =
        aPageRequest()
            .addFilter("appId", Operator.EQ, workflowExecution.getAppId())
            .addFilter("executionUuid", Operator.EQ, workflowExecution.getUuid())
            .addFilter("stateType", Operator.IN, StateType.REPEAT.name(), StateType.FORK.name(),
                StateType.SUB_WORKFLOW.name(), StateType.PHASE.name(), StateType.PHASE_STEP.name())
            .addFilter("parentInstanceId", Operator.NOT_EXISTS, null)
            .addOrder("createdAt", OrderType.ASC)
            .build();

    PageResponse<StateExecutionInstance> pageResponse =
        wingsPersistence.query(StateExecutionInstance.class, pageRequest);

    if (pageResponse == null || pageResponse.isEmpty()) {
      return;
    }

    for (StateExecutionInstance stateExecutionInstance : pageResponse.getResponse()) {
      if (!(stateExecutionInstance.getStateExecutionData() instanceof ElementStateExecutionData)) {
        continue;
      }

      ElementStateExecutionData elementStateExecutionData =
          (ElementStateExecutionData) stateExecutionInstance.getStateExecutionData();
      if (elementStateExecutionData.getElementStatusSummary() == null
          || elementStateExecutionData.getElementStatusSummary().isEmpty()) {
        continue;
      }
      for (ElementExecutionSummary summary : elementStateExecutionData.getElementStatusSummary()) {
        ServiceElement serviceElement = getServiceElement(summary.getContextElement());
        if (serviceElement == null) {
          continue;
        }
        ElementExecutionSummary serviceSummary = serviceSummaryMap.get(serviceElement.getUuid());
        if (serviceSummary == null) {
          serviceSummary =
              anElementExecutionSummary().withContextElement(serviceElement).withStatus(ExecutionStatus.QUEUED).build();
          serviceSummaryMap.put(serviceElement.getUuid(), serviceSummary);
        }
        if (serviceSummary.getStartTs() == null
            || (summary.getStartTs() != null && serviceSummary.getStartTs() > summary.getStartTs())) {
          serviceSummary.setStartTs(summary.getStartTs());
        }
        if (serviceSummary.getEndTs() == null
            || (summary.getEndTs() != null && serviceSummary.getEndTs() < summary.getEndTs())) {
          serviceSummary.setEndTs(summary.getEndTs());
        }
        if (serviceSummary.getInstanceStatusSummaries() == null && summary.getInstanceStatusSummaries() != null) {
          serviceSummary.setInstanceStatusSummaries(new ArrayList<>(summary.getInstanceStatusSummaries()));
        }
        serviceSummary.setStatus(summary.getStatus());
      }
    }
  }

  private ServiceElement getServiceElement(ContextElement contextElement) {
    if (contextElement == null) {
      return null;
    }
    switch (contextElement.getElementType()) {
      case SERVICE: {
        return (ServiceElement) contextElement;
      }
      case SERVICE_TEMPLATE: {
        return ((ServiceTemplateElement) contextElement).getServiceElement();
      }
      case INSTANCE: {
        return ((InstanceElement) contextElement).getServiceTemplateElement().getServiceElement();
      }
      case PARAM: {
        if (Constants.PHASE_PARAM.equals(contextElement.getName())) {
          return ((PhaseElement) contextElement).getServiceElement();
        }
        break;
      }
      default: {}
    }
    return null;
  }

  private void refreshBreakdown(WorkflowExecution workflowExecution) {
    if ((workflowExecution.getStatus() == ExecutionStatus.SUCCESS
            || workflowExecution.getStatus() == ExecutionStatus.FAILED
            || workflowExecution.getStatus() == ExecutionStatus.ERROR
            || workflowExecution.getStatus() == ExecutionStatus.ABORTED)
        && workflowExecution.getBreakdown() != null) {
      return;
    }

    StateMachine sm = wingsPersistence.get(StateMachine.class, workflowExecution.getStateMachineId());
    PageRequest<StateExecutionInstance> req =
        aPageRequest()
            .withLimit(PageRequest.UNLIMITED)
            .addFilter("appId", Operator.EQ, workflowExecution.getAppId())
            .addFilter("executionUuid", Operator.EQ, workflowExecution.getUuid())
            .addFieldsIncluded("uuid", "stateName", "contextElement", "parentInstanceId", "status")
            .build();
    PageResponse<StateExecutionInstance> res = wingsPersistence.query(StateExecutionInstance.class, req);
    CountsByStatuses breakdown = stateMachineExecutionSimulator.getStatusBreakdown(
        workflowExecution.getAppId(), workflowExecution.getEnvId(), sm, res.getResponse());
    int total = breakdown.getFailed() + breakdown.getSuccess() + breakdown.getInprogress() + breakdown.getQueued();

    workflowExecution.setBreakdown(breakdown);
    workflowExecution.setTotal(total);
    logger.info("Got the breakdown workflowExecution: {}, status: {}, breakdown: {}", workflowExecution.getUuid(),
        workflowExecution.getStatus(), breakdown);

    if (workflowExecution.getStatus() == ExecutionStatus.SUCCESS
        || workflowExecution.getStatus() == ExecutionStatus.FAILED
        || workflowExecution.getStatus() == ExecutionStatus.ERROR
        || workflowExecution.getStatus() == ExecutionStatus.ABORTED) {
      logger.info("Set the breakdown of the completed workflowExecution: {}, status: {}, breakdown: {}",
          workflowExecution.getUuid(), workflowExecution.getStatus(), breakdown);

      Query<WorkflowExecution> query = wingsPersistence.createQuery(WorkflowExecution.class)
                                           .field("appId")
                                           .equal(workflowExecution.getAppId())
                                           .field(ID_KEY)
                                           .equal(workflowExecution.getUuid());

      UpdateOperations<WorkflowExecution> updateOps = wingsPersistence.createUpdateOperations(WorkflowExecution.class);

      try {
        updateOps.set("breakdown", breakdown).set("total", total);
        UpdateResults updated = wingsPersistence.update(query, updateOps);
        logger.info("Updated : {} row", updated.getWriteResult().getN());
      } catch (java.lang.Exception e) {
        logger.error("Error in breakdown retrieval", e);
      }
    }
  }

  @Override
  public List<ElementExecutionSummary> getElementsSummary(
      String appId, String executionUuid, String parentStateExecutionInstanceId) {
    PageRequest<StateExecutionInstance> pageRequest =
        aPageRequest()
            .withLimit(PageRequest.UNLIMITED)
            .addFilter("appId", Operator.EQ, appId)
            .addFilter("executionUuid", Operator.EQ, executionUuid)
            .addFilter("parentInstanceId", Operator.IN, parentStateExecutionInstanceId)
            .addOrder("createdAt", OrderType.ASC)
            .build();

    PageResponse<StateExecutionInstance> pageResponse =
        wingsPersistence.query(StateExecutionInstance.class, pageRequest);
    if (pageResponse == null || pageResponse.isEmpty()) {
      return null;
    }

    List<StateExecutionInstance> contextTransitionInstances = pageResponse.getResponse()
                                                                  .stream()
                                                                  .filter(instance -> instance.isContextTransition())
                                                                  .collect(Collectors.toList());
    Map<String, StateExecutionInstance> prevInstanceIdMap =
        pageResponse.getResponse()
            .stream()
            .filter(instance -> instance.getPrevInstanceId() != null)
            .collect(Collectors.toMap(instance -> instance.getPrevInstanceId(), Function.identity()));

    List<ElementExecutionSummary> elementExecutionSummaries = new ArrayList<>();
    for (StateExecutionInstance stateExecutionInstance : contextTransitionInstances) {
      if (stateExecutionInstance.getContextElement() == null) {
        logger.error(
            "refreshSummary - no contextElement for stateExecutionInstance: {}", stateExecutionInstance.getUuid());
        continue;
      }
      ContextElement contextElement = stateExecutionInstance.getContextElement();
      ElementExecutionSummary elementExecutionSummary = anElementExecutionSummary()
                                                            .withContextElement(contextElement)
                                                            .withStartTs(stateExecutionInstance.getStartTs())
                                                            .build();

      List<InstanceStatusSummary> instanceStatusSummaries = new ArrayList<>();

      StateExecutionInstance last = stateExecutionInstance;
      for (StateExecutionInstance next = stateExecutionInstance; next != null;
           next = prevInstanceIdMap.get(next.getUuid())) {
        if ((StateType.REPEAT.name().equals(next.getStateType()) || StateType.FORK.name().equals(next.getStateType())
                || StateType.PHASE.name().equals(next.getStateType())
                || StateType.PHASE_STEP.name().equals(next.getStateType())
                || StateType.SUB_WORKFLOW.name().equals(next.getStateType()))
            && next.getStateExecutionData() instanceof ElementStateExecutionData) {
          ElementStateExecutionData elementStateExecutionData =
              (ElementStateExecutionData) next.getStateExecutionData();
          instanceStatusSummaries.addAll(elementStateExecutionData.getElementStatusSummary()
                                             .stream()
                                             .filter(e -> e.getInstanceStatusSummaries() != null)
                                             .flatMap(l -> l.getInstanceStatusSummaries().stream())
                                             .collect(Collectors.toList()));
        } else if (StateType.ECS_SERVICE_DEPLOY.name().equals(next.getStateType())
            && next.getStateExecutionData() instanceof CommandStateExecutionData) {
          CommandStateExecutionData commandStateExecutionData =
              (CommandStateExecutionData) next.getStateExecutionData();
          instanceStatusSummaries.addAll(commandStateExecutionData.getInstanceStatusSummaries());
        }
        last = next;
      }

      if (elementExecutionSummary.getEndTs() == null || elementExecutionSummary.getEndTs() < last.getEndTs()) {
        elementExecutionSummary.setEndTs(last.getEndTs());
      }
      if (contextElement.getElementType() == ContextElementType.INSTANCE) {
        instanceStatusSummaries.add(anInstanceStatusSummary()
                                        .withInstanceElement((InstanceElement) contextElement)
                                        .withStatus(last.getStatus())
                                        .build());
      }

      elementExecutionSummary.setStatus(last.getStatus());
      elementExecutionSummary.setInstanceStatusSummaries(instanceStatusSummaries);
      elementExecutionSummaries.add(elementExecutionSummary);
    }

    return elementExecutionSummaries;
  }
}
