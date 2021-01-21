package io.harness.perpetualtask.connector;

import static io.harness.NGConstants.CONNECTOR_HEARTBEAT_LOG_PREFIX;
import static io.harness.NGConstants.CONNECTOR_STRING;
import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.network.SafeHttpCall.execute;

import io.harness.annotations.dev.Module;
import io.harness.annotations.dev.TargetModule;
import io.harness.connector.ConnectivityStatus;
import io.harness.connector.ConnectorDTO;
import io.harness.connector.ConnectorInfoDTO;
import io.harness.connector.ConnectorValidationResult;
import io.harness.delegate.beans.connector.ConnectorHeartbeatDelegateResponse;
import io.harness.delegate.task.k8s.ConnectorValidationHandler;
import io.harness.grpc.utils.AnyUtils;
import io.harness.managerclient.DelegateAgentManagerClient;
import io.harness.ng.core.dto.ErrorDetail;
import io.harness.perpetualtask.PerpetualTaskExecutionParams;
import io.harness.perpetualtask.PerpetualTaskExecutor;
import io.harness.perpetualtask.PerpetualTaskId;
import io.harness.perpetualtask.PerpetualTaskResponse;
import io.harness.security.encryption.EncryptedDataDetail;
import io.harness.serializer.KryoSerializer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Response;

@Singleton
@AllArgsConstructor(onConstructor = @__({ @Inject }))
@Slf4j
@TargetModule(Module._930_DELEGATE_TASKS)
public class ConnectorHeartbeatPerpetualTaskExecutor implements PerpetualTaskExecutor {
  Map<String, ConnectorValidationHandler> connectorTypeToConnectorValidationHandlerMap;
  private KryoSerializer kryoSerializer;
  private DelegateAgentManagerClient delegateAgentManagerClient;

  @Override
  public PerpetualTaskResponse runOnce(
      PerpetualTaskId taskId, PerpetualTaskExecutionParams params, Instant heartbeatTime) {
    final ConnectorHeartbeatTaskParams taskParams =
        AnyUtils.unpack(params.getCustomizedParams(), ConnectorHeartbeatTaskParams.class);
    String accountId = taskParams.getAccountIdentifier();
    List<EncryptedDataDetail> encryptedDataDetails = getEncryptedDataDetails(taskParams);
    final ConnectorDTO connectorDTO = (ConnectorDTO) kryoSerializer.asObject(taskParams.getConnector().toByteArray());
    ConnectorInfoDTO connectorInfoDTO = connectorDTO.getConnectorInfo();
    String connectorMessage = String.format(CONNECTOR_STRING, connectorInfoDTO.getIdentifier(), accountId,
        connectorInfoDTO.getOrgIdentifier(), connectorInfoDTO.getProjectIdentifier());
    log.info("Starting validation task for the connector {}", connectorMessage);
    ConnectorValidationHandler connectorValidationHandler =
        connectorTypeToConnectorValidationHandlerMap.get(connectorInfoDTO.getConnectorType().toString());
    if (connectorValidationHandler == null) {
      log.info("The connector validation handler is not registered for the connector type "
          + connectorInfoDTO.getConnectorType());
    }
    ConnectorValidationResult connectorValidationResult = connectorValidationHandler.validate(
        connectorDTO.getConnectorInfo().getConnectorConfig(), accountId, encryptedDataDetails);
    connectorValidationResult.setTestedAt(System.currentTimeMillis());
    try {
      execute(delegateAgentManagerClient.publishConnectorHeartbeatResult(
          taskId.getId(), accountId, createHeartbeatResponse(accountId, connectorInfoDTO, connectorValidationResult)));
    } catch (Exception ex) {
      log.error("{} Failed to publish connector heartbeat result for task [{}] for the connector:[{}]", taskId.getId(),
          CONNECTOR_HEARTBEAT_LOG_PREFIX,
          String.format(CONNECTOR_STRING, connectorInfoDTO.getIdentifier(), accountId,
              connectorInfoDTO.getOrgIdentifier(), connectorInfoDTO.getProjectIdentifier()),
          ex);
    }
    log.info("Completed validation task for the connector {}", connectorMessage);
    return getPerpetualTaskResponse(connectorValidationResult);
  }

  private List<EncryptedDataDetail> getEncryptedDataDetails(ConnectorHeartbeatTaskParams taskParams) {
    byte[] encryptedDataDetailsByteArray = taskParams.getEncryptionDetails().toByteArray();
    if (encryptedDataDetailsByteArray.length != 0) {
      return (List<EncryptedDataDetail>) kryoSerializer.asObject(encryptedDataDetailsByteArray);
    }
    return null;
  }

  private ConnectorHeartbeatDelegateResponse createHeartbeatResponse(
      String accountId, ConnectorInfoDTO connectorInfoDTO, ConnectorValidationResult validationResult) {
    return ConnectorHeartbeatDelegateResponse.builder()
        .accountIdentifier(accountId)
        .orgIdentifier(connectorInfoDTO.getOrgIdentifier())
        .projectIdentifier(connectorInfoDTO.getProjectIdentifier())
        .identifier(connectorInfoDTO.getIdentifier())
        .connectorValidationResult(validationResult)
        .name(connectorInfoDTO.getName())
        .build();
  }

  private PerpetualTaskResponse getPerpetualTaskResponse(ConnectorValidationResult connectorValidationResult) {
    String message = "success";
    if (connectorValidationResult == null) {
      message = "Got Null connector validation result";
    } else if (connectorValidationResult.getStatus() != ConnectivityStatus.SUCCESS) {
      message = connectorValidationResult.getErrorSummary();
    }
    return PerpetualTaskResponse.builder().responseCode(Response.SC_OK).responseMessage(message).build();
  }

  private String getErrorMessage(List<ErrorDetail> errors) {
    if (isNotEmpty(errors) && errors.size() == 1) {
      return errors.get(0).getMessage();
    }
    return "Invalid Credentials";
  }

  @Override
  public boolean cleanup(PerpetualTaskId taskId, PerpetualTaskExecutionParams params) {
    return false;
  }
}