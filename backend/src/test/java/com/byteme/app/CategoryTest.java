package com.byteme.app;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class CategoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testCategoryPersistence() {
        Category category = new Category();
        category.setName("Bakery");

        Category saved = entityManager.persistAndFlush(category);
        entityManager.clear();

        Category found = entityManager.find(Category.class, saved.getCategoryId());
        assertNotNull(found);
        assertEquals("Bakery", found.getName());
    }

    @Test
    void testCategoryNameCannotBeNull() {
        Category category = new Category();
        category.setName(null);

        assertThrows(RuntimeException.class, () -> {
            entityManager.persistAndFlush(category);
        });
    }

    @Test
    void testCategoryNameMustBeUnique() {
        Category cat1 = new Category();
        cat1.setName("Produce");
        entityManager.persistAndFlush(cat1);

        Category cat2 = new Category();
        cat2.setName("Produce");

        assertThrows(RuntimeException.class, () -> {
            entityManager.persistAndFlush(cat2);
        });
    }

    @Test
    void testIdGeneration() {
        Category category = new Category();
        category.setName("Dairy");

        Category saved = entityManager.persistAndFlush(category);
        
        assertNotNull(saved.getCategoryId());
    }

    @Test
    void testGettersAndSetters() {
        UUID id = UUID.fromString("9e6e782f-c229-416f-b5d5-65853b4c91ba");
        Category category = new Category();
        
        category.setCategoryId(id);
        category.setName("Frozen");

        assertEquals(id, category.getCategoryId());
        assertEquals("Frozen", category.getName());
    }
}