package com.inventory.repository;

import com.inventory.model.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
public class SupplierRepositoryTests {

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    void createSupplier() {
        Supplier s = new Supplier();
        s.name = "Acme";
        s.contact = "Jane";
        s.phone = "555-1010";

        Supplier saved = supplierRepository.save(s);
        assertThat(saved).isNotNull();
        assertThat(saved.id).isNotNull();
    }

    @Test
    void getSupplierById() {
        Supplier s = new Supplier(); s.name = "S1"; Supplier saved = supplierRepository.save(s);
        var f = supplierRepository.findById(saved.id);
        assertThat(f).isPresent();
        assertThat(f.get().name).isEqualTo("S1");
    }

    @Test
    void getAllSuppliers() {
        supplierRepository.deleteAll();
        Supplier a = new Supplier(); a.name = "A"; supplierRepository.save(a);
        Supplier b = new Supplier(); b.name = "B"; supplierRepository.save(b);

        List<Supplier> all = supplierRepository.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void updateSupplier() {
        Supplier s = new Supplier(); s.name = "Old"; Supplier saved = supplierRepository.save(s);
        saved.name = "New"; Supplier updated = supplierRepository.save(saved);
        assertThat(updated.name).isEqualTo("New");
    }

    @Test
    void deleteSupplier() {
        Supplier s = new Supplier(); s.name = "ToDelete"; Supplier saved = supplierRepository.save(s);
        supplierRepository.deleteById(saved.id);
        assertThat(supplierRepository.findById(saved.id)).isNotPresent();
    }
}
