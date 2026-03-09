package com.byteme.app;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrganisationBadgeTest {

    @Test
    void testEntityStateAndGetters() {
        OrganisationBadge orgBadge = new OrganisationBadge();
        UUID orgId = UUID.randomUUID();
        UUID badgeId = UUID.randomUUID();
        Instant now = Instant.now();

        orgBadge.setOrgId(orgId);
        orgBadge.setBadgeId(badgeId);
        orgBadge.setAwardedAt(now);

        assertEquals(orgId, orgBadge.getOrgId());
        assertEquals(badgeId, orgBadge.getBadgeId());
        assertEquals(now, orgBadge.getAwardedAt());
    }

    @Test
    void testKeyClassLogic() {
        OrganisationBadge.Key key = new OrganisationBadge.Key();
        UUID orgId = UUID.randomUUID();
        UUID badgeId = UUID.randomUUID();

        key.setOrgId(orgId);
        key.setBadgeId(badgeId);

        assertEquals(orgId, key.getOrgId());
        assertEquals(badgeId, key.getBadgeId());
    }

    @Test
    void testDefaultValues() {
        OrganisationBadge orgBadge = new OrganisationBadge();

        assertNotNull(orgBadge.getAwardedAt(), "awardedAt should be initialized by default");
        assertTrue(orgBadge.getAwardedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testRelationships() {
        OrganisationBadge orgBadge = new OrganisationBadge();
        Organisation org = new Organisation();
        Badge badge = new Badge();

        orgBadge.setOrganisation(org);
        orgBadge.setBadge(badge);

        assertEquals(org, orgBadge.getOrganisation());
        assertEquals(badge, orgBadge.getBadge());
    }
}