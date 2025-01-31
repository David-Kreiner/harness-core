/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.service.instancesyncperpetualtaskinfo;

import static io.harness.annotations.dev.HarnessTeam.DX;

import io.harness.annotations.dev.OwnedBy;
import io.harness.dtos.instancesyncperpetualtaskinfo.InstanceSyncPerpetualTaskInfoDTO;

import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@OwnedBy(DX)
public interface InstanceSyncPerpetualTaskInfoService {
  Optional<InstanceSyncPerpetualTaskInfoDTO> findByInfrastructureMappingId(String infrastructureMappingId);

  Optional<InstanceSyncPerpetualTaskInfoDTO> findByPerpetualTaskId(String accountIdentifier, String perpetualTaskId);

  InstanceSyncPerpetualTaskInfoDTO save(InstanceSyncPerpetualTaskInfoDTO instanceSyncPerpetualTaskInfoDTO);

  void deleteById(String accountIdentifier, String instanceSyncPerpetualTaskInfoId);

  InstanceSyncPerpetualTaskInfoDTO updateDeploymentInfoDetailsList(
      InstanceSyncPerpetualTaskInfoDTO instanceSyncPerpetualTaskInfoDTO);

  /**
   * Deletes all instance sync perpetual task info linked to a particular harness project.
   * @param accountIdentifier  the account id
   * @return boolean to indicate if deletion was successful
   */
  @NotNull boolean deleteAllInstanceSyncPTs(@NotEmpty String accountIdentifier);
}
