package com.inventory.repository;

import com.inventory.model.Product;
import com.inventory.model.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    void createProduct() {
        Supplier s = new Supplier(); s.name = "ProdSupplier"; supplierRepository.save(s);

        Product p = new Product();
        p.sku = "SKU-1";
        p.name = "Widget";
        p.description = "Test widget";
        p.supplier = s;
        p.unitPrice = 9.99;

        Product saved = productRepository.save(p);
        assertThat(saved).isNotNull();
        assertThat(saved.id).isNotNull();
    }

    @Test
    void getProduct() {
        Supplier s = new Supplier(); s.name = "Sx"; supplierRepository.save(s);
        Product p = new Product(); p.sku = "SKU-2"; p.name = "Gadget"; p.supplier = s; productRepository.save(p);

        var found = productRepository.findAll().stream().filter(x -> "SKU-2".equals(x.sku)).findFirst();
        assertThat(found).isPresent();
        assertThat(found.get().name).isEqualTo("Gadget");
    }

    @Test
    void updateProductFields() {
        Supplier s1 = new Supplier(); s1.name = "S1"; supplierRepository.save(s1);
        Supplier s2 = new Supplier(); s2.name = "S2"; supplierRepository.save(s2);

        Product p = new Product(); p.sku = "SKU-3"; p.name = "OldName"; p.unitPrice = 5.0; p.supplier = s1; Product saved = productRepository.save(p);

        saved.name = "NewName";
        saved.unitPrice = 7.5;
        saved.supplier = s2;
        Product updated = productRepository.save(saved);

        assertThat(updated.name).isEqualTo("NewName");
        assertThat(updated.unitPrice).isEqualTo(7.5);
        assertThat(updated.supplier).isNotNull();
        assertThat(updated.supplier.name).isEqualTo("S2");
    }

    @Test
    void deleteProduct() {
        Product p = new Product(); p.sku = "SKU-DEL"; p.name = "ToDelete"; Product saved = productRepository.save(p);
        productRepository.deleteById(saved.id);
        assertThat(productRepository.findById(saved.id)).isNotPresent();
    }
}
