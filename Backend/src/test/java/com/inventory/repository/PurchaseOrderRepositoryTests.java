package com.inventory.repository;

import com.inventory.model.Product;
import com.inventory.model.PurchaseOrder;
import com.inventory.model.PurchaseOrderItem;
import com.inventory.model.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
public class PurchaseOrderRepositoryTests {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    void createPurchaseOrder_manually_and_verifyItemsAndSupplier() {
        Supplier sup = new Supplier();
        sup.name = "PO Test Supplier";
        supplierRepository.save(sup);

        Product p = new Product();
        p.sku = "PO-SKU-1";
        p.name = "POProduct";
        p.supplier = sup;
        productRepository.save(p);

        PurchaseOrder po = new PurchaseOrder();
        po.supplier = sup;
        po.status = "CREATED";

        PurchaseOrderItem poi = new PurchaseOrderItem();
        poi.product = p;
        poi.quantity = 4;
        poi.unitPrice = p.unitPrice;

        po.items = new java.util.ArrayList<>();
        po.items.add(poi);

        purchaseOrderRepository.save(po);

        var all = purchaseOrderRepository.findAll();
        assertThat(all).isNotEmpty();

        PurchaseOrder created = all.get(0);
        assertThat(created.items).isNotNull();
        assertThat(created.items.size()).isGreaterThanOrEqualTo(1);

        // verify supplier id of PO equals supplier id of product referenced in item
        var item = created.items.get(0);
        assertThat(item.product).isNotNull();
        assertThat(item.product.supplier).isNotNull();
        assertThat(item.product.supplier.id).isEqualTo(created.supplier.id);
    }
}
