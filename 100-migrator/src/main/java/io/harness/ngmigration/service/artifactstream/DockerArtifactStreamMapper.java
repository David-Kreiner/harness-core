/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ngmigration.service.artifactstream;

import static software.wings.ngmigration.NGMigrationEntityType.CONNECTOR;

import io.harness.cdng.artifact.bean.yaml.DockerHubArtifactConfig;
import io.harness.cdng.artifact.bean.yaml.PrimaryArtifact;
import io.harness.delegate.task.artifacts.ArtifactSourceType;
import io.harness.ngmigration.beans.MigrationInputDTO;
import io.harness.ngmigration.beans.NGYamlFile;
import io.harness.ngmigration.beans.NgEntityDetail;
import io.harness.ngmigration.utils.MigratorUtility;
import io.harness.pms.yaml.ParameterField;

import software.wings.beans.artifact.ArtifactStream;
import software.wings.beans.artifact.DockerArtifactStream;
import software.wings.ngmigration.CgEntityId;
import software.wings.ngmigration.CgEntityNode;

import java.util.Map;
import java.util.Set;

public class DockerArtifactStreamMapper implements ArtifactStreamMapper {
  @Override
  public PrimaryArtifact getArtifactDetails(MigrationInputDTO inputDTO, Map<CgEntityId, CgEntityNode> entities,
      Map<CgEntityId, Set<CgEntityId>> graph, ArtifactStream artifactStream,
      Map<CgEntityId, NGYamlFile> migratedEntities) {
    DockerArtifactStream dockerArtifactStream = (DockerArtifactStream) artifactStream;
    NgEntityDetail connector =
        migratedEntities.get(CgEntityId.builder().type(CONNECTOR).id(dockerArtifactStream.getSettingId()).build())
            .getNgEntityDetail();
    return PrimaryArtifact.builder()
        .sourceType(ArtifactSourceType.DOCKER_REGISTRY)
        .spec(DockerHubArtifactConfig.builder()
                  .connectorRef(ParameterField.createValueField(MigratorUtility.getIdentifierWithScope(connector)))
                  .imagePath(ParameterField.createValueField(dockerArtifactStream.getImageName()))
                  .tag(ParameterField.createValueField("<+input>"))
                  .build())
        .build();
  }
}
