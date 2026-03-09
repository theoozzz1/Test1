package com.byteme.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserAccountRepository userRepo;

    @Test
    void testSaveAndFindByEmail() {
        UserAccount user = new UserAccount();
        user.setEmail("test@byteme.com");
        user.setPasswordHash("hash123");
        user.setRole(UserAccount.Role.SELLER);
        entityManager.persistAndFlush(user);

        Optional<UserAccount> found = userRepo.findByEmail("test@byteme.com");

        assertTrue(found.isPresent());
        assertEquals("test@byteme.com", found.get().getEmail());
        assertEquals(UserAccount.Role.SELLER, found.get().getRole());
    }

    @Test
    void testExistsByEmail() {
        UserAccount user = new UserAccount();
        user.setEmail("exists@byteme.com");
        user.setPasswordHash("hash");
        user.setRole(UserAccount.Role.ORG_ADMIN);
        entityManager.persistAndFlush(user);

        assertTrue(userRepo.existsByEmail("exists@byteme.com"));
        assertFalse(userRepo.existsByEmail("notfound@byteme.com"));
    }

    @Test
    void testUniqueEmailConstraint() {
        UserAccount user1 = new UserAccount();
        user1.setEmail("duplicate@byteme.com");
        user1.setPasswordHash("hash1");
        user1.setRole(UserAccount.Role.SELLER);
        entityManager.persistAndFlush(user1);

        UserAccount user2 = new UserAccount();
        user2.setEmail("duplicate@byteme.com"); // Same email
        user2.setPasswordHash("hash2");
        user2.setRole(UserAccount.Role.ORG_ADMIN);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepo.saveAndFlush(user2);
        }, "Should throw exception due to unique email constraint");
    }
}