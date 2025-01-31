/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.repositories;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotation.HarnessRepo;
import io.harness.annotations.dev.OwnedBy;
import io.harness.steps.resourcerestraint.beans.ResourceRestraint;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@OwnedBy(PIPELINE)
@HarnessRepo
@Transactional
public interface ResourceRestraintRepository extends CrudRepository<ResourceRestraint, String> {
  List<ResourceRestraint> findByUuidIn(Set<String> resourceRestraintUuids);
  Optional<ResourceRestraint> findByNameAndAccountId(String name, String accountId);

  /**
   * Deletes all records matching given accountId
   * Uses - uniqueName idx
   * @param accountId
   * @return
   */
  long deleteByAccountId(String accountId);
}
