# Inventory Management System — Backend

Summary of the backend as of 11/17/25

**Overview**
- Language & framework: Java (project configured for Java 21), Spring Boot 3.2.x, Spring Data JPA, Hibernate ORM (6.x).
- Purpose: backend services for an inventory management demo: products, suppliers, customers, stock, orders, and purchase orders.

**Build & Run (local development)**
- Build the project with Maven from the `Backend` folder:

	mvn -f "C:\Database Class projects\Inventory-Managment-System-CIS407\Backend\pom.xml" clean package

- Run the application (uses settings in `src/main/resources/application.properties`):

	mvn -f "C:\Database Class projects\Inventory-Managment-System-CIS407\Backend\pom.xml" spring-boot:run

**Testing**
- Run all tests (unit + integration) with the embedded H2 database (this is enforced for tests to keep them repeatable):

	mvn -f "C:\Database Class projects\Inventory-Managment-System-CIS407\Backend\pom.xml" test

- Tests use an in-memory H2 DB by default so they are isolated from the production SQLite configuration. Use the `-D` system properties if you need to run tests with a different profile.

**Database notes**
- Production/Default config: the project ships with a minimal custom `SQLiteDialect` at `com.inventory.config.SQLiteDialect` and `application.properties` references it with:

	spring.jpa.database-platform=com.inventory.config.SQLiteDialect

	This means the application expects to run against SQLite by default (the dialect class is present in `src/main/java/com/inventory/config/SQLiteDialect.java`).

- Tests: the test suite forces an embedded H2 datasource for stability and DDL compatibility. You do not need SQLite to run `mvn test`.

- Recommendation:
	- Keep `SQLiteDialect` if you intend to run the application against SQLite (lightweight local/demo runs). The current dialect is intentionally minimal and may not cover every advanced SQL mapping — it's suitable for simple usage.
	- If you will never use SQLite, remove the `spring.jpa.database-platform` setting from `application.properties` and delete `SQLiteDialect.java` to avoid carrying a custom dialect.
	- Alternatively, make the dialect selection conditional based on `spring.datasource.url` (I can help implement that if desired).

**Key packages and components**
- `com.inventory.model` — JPA entities: `Product`, `Supplier`, `Customer`, `Order`, `OrderItem`, `Stock`, `PurchaseOrder`, `PurchaseOrderItem`.
- `com.inventory.repository` — Spring Data JPA repositories for CRUD and simple queries.
- `com.inventory.service` — Business logic: stock management, order creation, reorder (purchase order) logic.
	- Notably, `StockService` reduces stock, clamps values >= 0, and triggers creation of `PurchaseOrder` when stock drops below a product's reorder point (when `targetStock` is set).
- `com.inventory.controller` — HTTP endpoints for Products, Suppliers, Orders, PurchaseOrders, Stock, Customers.
	- `ProductController` uses `@Valid` for product creation and `Product.name` is annotated with `@NotBlank`.
	- `OrderController` performs request validation on order items (product id presence, positive quantity), and the service ensures provided customer id exists.
	- `GlobalExceptionHandler` maps common exceptions (`NoSuchElementException` -> 404, `IllegalArgumentException` -> 400) so integration tests can assert HTTP status codes predictably.

**Testing notes & historical issues**
- Early test failures occurred because SQLite DDL and identity SQL expressions differ from H2 — tests now use H2 to avoid those problems.
- Avoid returning full JPA entity graphs from controller POST responses — tests observed JSON recursion/depth issues when large graphs were serialized. Controllers were modified to return minimal responses (e.g., created entity ID) in places where full graphs caused problems.
- Tests should avoid traversing lazy collections outside active transactions (LazyInitializationException). Prefer verifying DB state through repositories in tests.

**Performance / stability tests**
- There are lightweight sanity tests in `src/test/java/com/inventory/performance/PerformanceSanityTests.java` that insert ~100 products and list products/orders. These tests are intentionally small smoke tests, not benchmarks.

**How to add tests**
- Use `@SpringBootTest` for integration-style tests that require repositories and the Spring context.
- For repository-focused tests you may use `@DataJpaTest` if you want a slimmer context (requires configuring embedded DB for DDL).
- Keep tests repeatable: use in-memory DB, and ensure each test either cleans up or operates on a fresh DB (test framework creates fresh schema by default).

**Conventions and helpers**
- Entities use public fields for brevity in the demo — follow the same pattern when adding quick tests or seed data.
- Keep controller input validation simple (use `@Valid` + constraint annotations) and rely on the `GlobalExceptionHandler` to convert exceptions to client-friendly HTTP responses.

**Known limitations & TODOs**
- `SQLiteDialect` is minimal — if you rely on advanced SQL features, consider switching to a maintained dialect or a different production DB.
- No production-ready security or migration tooling is included (no Flyway/Liquibase configured). If you plan to use in production, add migrations and secure endpoints.

**Developer tips**
- To run a single test class locally:

	mvn -f "C:\Database Class projects\Inventory-Managment-System-CIS407\Backend\pom.xml" -Dtest=com.inventory.repository.ProductRepositoryTests test

- To run tests and show detailed SQL output, append `-Dspring.jpa.show-sql=true -Dspring.jpa.properties.hibernate.format_sql=true` to the `mvn test` command.

**Contact / Next steps**
- If you want me to:
	- Make `SQLiteDialect` conditional, or
	- Replace it with a fuller Hibernate 6-compatible implementation, or
	- Remove the dialect and default to H2, or
	- Add GET controllers for purchase orders and integration tests —
	tell me which direction and I'll implement it and run tests.

-- End of backend README
