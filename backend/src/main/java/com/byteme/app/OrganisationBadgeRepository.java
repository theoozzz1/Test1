package com.byteme.app;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrganisationBadgeRepository extends JpaRepository<OrganisationBadge, OrganisationBadge.Key> {
    List<OrganisationBadge> findByOrgId(UUID orgId);
}
