package io.harness.gitsync.common.impl;

import static io.harness.rule.OwnerRule.HARI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.IdentifierRef;
import io.harness.category.element.UnitTests;
import io.harness.connector.ConnectorInfoDTO;
import io.harness.connector.ConnectorResponseDTO;
import io.harness.connector.services.ConnectorService;
import io.harness.delegate.beans.connector.scm.github.GithubApiAccessDTO;
import io.harness.delegate.beans.connector.scm.github.GithubConnectorDTO;
import io.harness.delegate.beans.git.YamlGitConfigDTO;
import io.harness.gitsync.GitSyncTestBase;
import io.harness.gitsync.common.dtos.GitFileContent;
import io.harness.gitsync.common.service.YamlGitConfigService;
import io.harness.ng.beans.PageRequest;
import io.harness.product.ci.scm.proto.FileContent;
import io.harness.product.ci.scm.proto.ListBranchesResponse;
import io.harness.rule.Owner;
import io.harness.service.ScmClient;
import io.harness.tasks.DecryptGitApiAccessHelper;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@OwnedBy(HarnessTeam.DX)
public class ScmManagerFacilitatorServiceImplTest extends GitSyncTestBase {
  @Mock ScmClient scmClient;
  @Mock DecryptGitApiAccessHelper decryptGitApiAccessHelper;
  @Mock ConnectorService connectorService;
  @Mock AbstractScmClientFacilitatorServiceImpl abstractScmClientFacilitatorService;
  @Mock YamlGitConfigService yamlGitConfigService;
  @InjectMocks @Inject ScmManagerFacilitatorServiceImpl scmManagerFacilitatorService;
  String accountIdentifier = "accountIdentifier";
  String projectIdentifier = "projectIdentifier";
  String orgIdentifier = "orgIdentifier";
  String connectorIdentifierRef = "connectorIdentifierRef";
  String repoURL = "repoURL";
  String yamlGitConfigIdentifier = "yamlGitConfigIdentifier";
  String filePath = "filePath";
  final String branch = "branch";
  FileContent fileContent = FileContent.newBuilder().build();
  final ListBranchesResponse listBranchesResponse =
      ListBranchesResponse.newBuilder().addBranches("master").addBranches("feature").build();

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(decryptGitApiAccessHelper.decryptScmApiAccess(any(), any(), any(), any()))
        .thenReturn(GithubConnectorDTO.builder().build());
    when(scmClient.getFileContent(any(), any())).thenReturn(fileContent);
    when(scmClient.listBranches(any())).thenReturn(listBranchesResponse);
    GithubConnectorDTO githubConnector =
        GithubConnectorDTO.builder().apiAccess(GithubApiAccessDTO.builder().build()).build();
    ConnectorInfoDTO connectorInfo = ConnectorInfoDTO.builder().connectorConfig(githubConnector).build();
    doReturn(Optional.of(ConnectorResponseDTO.builder().connector(connectorInfo).build()))
        .when(connectorService)
        .get(anyString(), anyString(), anyString(), anyString());
    when(abstractScmClientFacilitatorService.getYamlGitConfigDTO(
             accountIdentifier, orgIdentifier, projectIdentifier, yamlGitConfigIdentifier))
        .thenReturn(YamlGitConfigDTO.builder().build());
    when(abstractScmClientFacilitatorService.getConnectorIdentifierRef(any(), anyString(), anyString(), anyString()))
        .thenReturn(IdentifierRef.builder().build());
    when(yamlGitConfigService.get(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(YamlGitConfigDTO.builder()
                        .accountIdentifier(accountIdentifier)
                        .projectIdentifier(projectIdentifier)
                        .organizationIdentifier(orgIdentifier)
                        .gitConnectorRef(connectorIdentifierRef)
                        .build());
  }

  @Test
  @Owner(developers = HARI)
  @Category(UnitTests.class)
  public void listBranchesForRepoByConnectorTest() {
    final List<String> branches =
        scmManagerFacilitatorService.listBranchesForRepoByConnector(accountIdentifier, orgIdentifier, projectIdentifier,
            connectorIdentifierRef, repoURL, PageRequest.builder().pageIndex(0).pageSize(10).build(), "");
    assertThat(branches).isEqualTo(listBranchesResponse.getBranchesList());
  }

  @Test
  @Owner(developers = HARI)
  @Category(UnitTests.class)
  public void getFileContentTest() {
    final GitFileContent gitFileContent = scmManagerFacilitatorService.getFileContent(
        yamlGitConfigIdentifier, accountIdentifier, orgIdentifier, projectIdentifier, filePath, branch, null);
    assertThat(gitFileContent)
        .isEqualTo(
            GitFileContent.builder().content(fileContent.getContent()).objectId(fileContent.getBlobId()).build());
  }

  @Test
  @Owner(developers = HARI)
  @Category(UnitTests.class)
  public void isSaasGitTest() {
    List<String> repoURLs = new ArrayList<>(Arrays.asList(
        "www.github.com", "http://www.gitlab.com", "www.github.harness.com", "harness.github.com", "github.com"));
    List<Boolean> expected = new ArrayList<>(Arrays.asList(true, true, false, false, true));
    List<Boolean> actual = new ArrayList<>();
    repoURLs.forEach(repoURL -> actual.add(scmManagerFacilitatorService.isSaasGit(repoURL).isSaasGit()));
    assertThat(actual).isEqualTo(expected);
  }
}