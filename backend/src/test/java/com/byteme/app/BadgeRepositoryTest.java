package com.byteme.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BadgeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BadgeRepository badgeRepository;

    @Test
    void testFindByCode_Success() {
        Badge badge = new Badge();
        badge.setName("Early Bird");
        badge.setCode("EARLY_BIRD_2026");
        
        entityManager.persistAndFlush(badge);

        Optional<Badge> found = badgeRepository.findByCode("EARLY_BIRD_2026");

        assertTrue(found.isPresent());
        assertEquals("Early Bird", found.get().getName());
        assertEquals("EARLY_BIRD_2026", found.get().getCode());
    }

    @Test
    void testFindByCode_NotFound() {
        Optional<Badge> found = badgeRepository.findByCode("NON_EXISTENT_CODE");

        assertFalse(found.isPresent());
    }

    @Test
    void testSaveAndFindById() {
        Badge badge = new Badge();
        badge.setName("Sustainability Hero");
        badge.setCode("HERO_001");

        Badge savedBadge = badgeRepository.save(badge);
        entityManager.flush();
        entityManager.clear();

        Optional<Badge> retrieved = badgeRepository.findById(savedBadge.getBadgeId());

        assertTrue(retrieved.isPresent());
        assertEquals("HERO_001", retrieved.get().getCode());
        assertNotNull(retrieved.get().getBadgeId());
    }

    @Test
    void testUniqueCodeConstraint() {
        Badge badge1 = new Badge();
        badge1.setName("First");
        badge1.setCode("UNIQUE_CODE");
        entityManager.persistAndFlush(badge1);

        Badge badge2 = new Badge();
        badge2.setName("Second");
        badge2.setCode("UNIQUE_CODE");

        assertThrows(DataIntegrityViolationException.class, () -> {
            badgeRepository.saveAndFlush(badge2);
        });
    }

    @Test
    void testNotNullConstraints() {
        Badge badge = new Badge();
        // Assuming 'name' or 'code' is @Column(nullable = false)
        
        assertThrows(DataIntegrityViolationException.class, () -> {
            badgeRepository.saveAndFlush(badge);
        });
    }

    @Test
    void testDelete() {
        Badge badge = new Badge();
        badge.setName("To Be Deleted");
        badge.setCode("DELETE_ME");
        Badge saved = entityManager.persistAndFlush(badge);

        badgeRepository.delete(saved);
        entityManager.flush();

        Optional<Badge> found = badgeRepository.findById(saved.getBadgeId());
        assertFalse(found.isPresent());
    }
}