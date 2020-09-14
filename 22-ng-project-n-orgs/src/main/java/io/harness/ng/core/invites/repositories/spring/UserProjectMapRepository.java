package io.harness.ng.core.invites.repositories.spring;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotation.HarnessRepo;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.core.invites.entities.UserProjectMap;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

@HarnessRepo
@OwnedBy(PL)
public interface UserProjectMapRepository extends PagingAndSortingRepository<UserProjectMap, String> {
  Optional<UserProjectMap> findByUserIdAndAccountIdentifierAndOrgIdentifierAndProjectIdentifier(
      String userId, String accountIdentifier, String orgIdentifier, String projectIdentifier);
}
