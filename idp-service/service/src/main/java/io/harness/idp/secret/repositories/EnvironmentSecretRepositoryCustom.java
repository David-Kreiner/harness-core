/*
 * Copyright 2023 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.idp.secret.repositories;

import io.harness.idp.secret.beans.entity.EnvironmentSecretEntity;

import com.mongodb.client.result.UpdateResult;

public interface EnvironmentSecretRepositoryCustom {
  EnvironmentSecretEntity update(EnvironmentSecretEntity environmentSecretEntity);
}
