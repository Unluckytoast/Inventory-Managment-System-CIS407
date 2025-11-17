package com.inventory.repository;

import com.inventory.model.Product;
import com.inventory.model.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
public class ConstraintRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    void createProduct_withInvalidSupplierId_shouldFail() {
        Supplier fake = new Supplier();
        fake.id = 9999L; // non-existent supplier id

        Product p = new Product();
        p.sku = "BAD-SUP";
        p.name = "BadSupplierProduct";
        p.supplier = fake;

        // saving and flushing should try to insert product with FK to non-existent supplier and fail
        assertThrows(DataIntegrityViolationException.class, () -> {
            productRepository.saveAndFlush(p);
        });
    }

    @Test
    void deleteSupplier_referencedByProduct_shouldBlockOrCascade() {
        Supplier sup = new Supplier(); sup.name = "DelSup"; supplierRepository.saveAndFlush(sup);

        Product p = new Product(); p.sku = "REF-1"; p.name = "RefProd"; p.supplier = sup; productRepository.saveAndFlush(p);

        // Attempting to delete supplier referenced by product should cause a constraint violation on flush
        supplierRepository.delete(sup);
        assertThrows(DataIntegrityViolationException.class, () -> {
            supplierRepository.flush();
        });
    }
}
