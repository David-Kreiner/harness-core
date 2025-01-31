/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.serializer.kryo;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.polling.contracts.PollingItem;
import io.harness.serializer.kryo.ProtobufKryoSerializer;

@OwnedBy(PIPELINE)
public class PollingItemKryoSerializer extends ProtobufKryoSerializer<PollingItem> {
  private static PollingItemKryoSerializer instance;

  public PollingItemKryoSerializer() {}

  public static synchronized PollingItemKryoSerializer getInstance() {
    if (instance == null) {
      instance = new PollingItemKryoSerializer();
    }
    return instance;
  }
}
