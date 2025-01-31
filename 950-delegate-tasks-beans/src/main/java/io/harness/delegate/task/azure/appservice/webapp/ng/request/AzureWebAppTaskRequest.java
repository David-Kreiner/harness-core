/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.delegate.task.azure.appservice.webapp.ng.request;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.DecryptableEntity;
import io.harness.delegate.beans.executioncapability.ExecutionCapabilityDemander;
import io.harness.delegate.beans.logstreaming.CommandUnitsProgress;
import io.harness.delegate.task.TaskParameters;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppInfraDelegateConfig;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppRequestType;
import io.harness.security.encryption.EncryptedDataDetail;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

@OwnedBy(CDP)
public interface AzureWebAppTaskRequest extends TaskParameters, ExecutionCapabilityDemander {
  String getAccountId();
  AzureWebAppRequestType getRequestType();
  AzureWebAppInfraDelegateConfig getInfrastructure();
  CommandUnitsProgress getCommandUnitsProgress();
  List<Pair<DecryptableEntity, List<EncryptedDataDetail>>> fetchDecryptionDetails();
}
