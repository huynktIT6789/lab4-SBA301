# Phụ lục C – JPA → MongoDB Upgrade Trace (cũng là H.1 File-by-file change map)

| Artifact | Lab 04 JPA | Lab 04.1 MongoDB | Keep/Change | Evidence |
|---|---|---|---|---|
| `pom.xml` | `spring-boot-starter-data-jpa` + JDBC driver | `spring-boot-starter-data-mongodb` | Change | `pom.xml` |
| `application.properties` | `spring.datasource.*`, `spring.jpa.*` | `spring.data.mongodb.uri`, `auto-index-creation` | Change | `src/main/resources/application.properties` |
| Product model | `@Entity @Table`, `Long id` + `@GeneratedValue` | `@Document(collection="products")`, `String id`, `@Indexed` | Change | `model/Product.java` |
| Relationship | `@ManyToOne` Category, `@OneToMany` Review (FK/JOIN) | `categoryId` (manual reference), `List<Review>` embedded | Change | `model/Review.java`, `model/Category.java`, E06 |
| Repository | `JpaRepository<Product, Long>` | `MongoRepository<Product, String>` | Change | `repository/ProductRepository.java` |
| Service | Business rules + `@Transactional` | Business rules + validate manual reference, sort whitelist | Keep (phần lớn) | `service/ProductService.java` |
| Controller | `@RestController`, DTO, `ResponseEntity` | Như cũ; `id` kiểu `String` | Keep (gần như) | `controller/ProductController.java` |
| Exception handler | `ApiError` + NOT_FOUND/VALIDATION | Như cũ + mapping `DuplicateKeyException` (409), DB down (503) | Keep (+thêm) | `exception/GlobalExceptionHandler.java` |
| Postman collection | CRUD + validation | Giữ nguyên contract, thêm review/sku/sort cases | Keep (+thêm) | `postman/Lab41.postman_collection.json` |
| Persistence tests | `@DataJpaTest` + H2 | `@DataMongoTest` + Testcontainers | Change | `ProductRepositoryTest.java` |
| Controller / Service tests | MockMvc / Mockito | Giữ nguyên cách làm | Keep | `ProductControllerTest.java`, `ProductServiceTest.java` |
| React frontend | Axios/Fetch | Không đổi | Keep | — |

## Kiến trúc cần vẽ lại (H.2)
```
Before: React -> REST Controller -> Service -> JpaRepository   -> SQL Server
After : React -> REST Controller -> Service -> MongoRepository -> MongoDB

 [ HTTP API contract ]      stable
          |
 [ application logic ]      stable (query/reference logic đổi nhẹ)
          |
 [ persistence adapter ]    <-- replaced
```
