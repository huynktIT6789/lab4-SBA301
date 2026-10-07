# E14 – JPA vs MongoDB: Comparison & Reflection

## Kiến trúc
```
Before: React -> REST Controller -> Service -> JpaRepository   -> SQL Server
After : React -> REST Controller -> Service -> MongoRepository -> MongoDB

Stable boundary:
   [ HTTP API contract ]      <- giữ nguyên
   [ application logic ]      <- gần như giữ nguyên
   [ persistence adapter ]    <- thay thế
```

## Reflection bắt buộc (16.3)
1. **File giữ nguyên:** `ProductController` (gần như – chỉ `id` đổi `Long` → `String`), DTO, `GlobalExceptionHandler` (chỉ thêm mapping duplicate-key), phần lớn `ProductService`, Postman collection, React frontend.
2. **File thay đổi hoàn toàn:** `Product` model (`@Entity` → `@Document`, quan hệ → embed/reference), `ProductRepository` (`JpaRepository<Product, Long>` → `MongoRepository<Product, String>`), `application.properties` (datasource/JPA → MongoDB URI), `pom.xml` (data-jpa + driver SQL → data-mongodb), repository tests (`@DataJpaTest` → Mongo integration/Testcontainers).
3. **Vì sao Controller không phụ thuộc trực tiếp MongoRepository:** Controller chỉ lo HTTP concern. Nếu import repository, mọi thay đổi persistence sẽ lan lên tầng API, business rule bị rải vào controller, và không còn mock được Service để test contract mà không cần DB.
4. **MongoDB không có nghĩa là không cần schema/design:** Vẫn phải quyết định embed hay reference theo access pattern, kích thước bounded hay không, index theo query pattern, validation ở DTO/Service. Flexible schema chỉ dời trách nhiệm enforce từ DDL sang application.
5. **Quay lại SQL Server, frontend có cần đổi không?** Không, miễn API contract (method, URL, request/response DTO, error format) giữ nguyên. `id` là chuỗi opaque với frontend nên đổi `Long` ↔ `String` không ảnh hưởng nếu frontend không giả định kiểu số.

## So sánh chuyên sâu
| Khía cạnh | JPA / SQL | MongoDB | Trong lab này |
|---|---|---|---|
| Model | Bảng chuẩn hoá | Document/collection | `products` chứa review embedded |
| Relationship | FK + JOIN | Embed / reference | Review embed; Category reference bằng `categoryId` |
| Schema | DDL constraint | Shape linh hoạt | Validation nằm ở DTO + Service |
| Atomicity | Transaction nhiều bảng | Atomic ở 1 document | Thêm review = 1 document write |
| Query | JPQL/SQL | Derived/Mongo query/aggregation | Derived query + `Pageable` |
| ID | `Long` + `@GeneratedValue` | `ObjectId`/`String` | `String id` map vào `_id` |
| Index | RDBMS index | `@Indexed` | `categoryId`, `sku` (unique, sparse) |
| Test persistence | `@DataJpaTest` + H2 | Mongo integration / Testcontainers | `@DataMongoTest` + Testcontainers |
| Inspect | SQL client | Compass | Compass |
| Domain nhiều JOIN | Tự nhiên hơn | Kém tự nhiên | Không phải thế mạnh của Mongo |

## Phản biện kiến trúc (Phụ lục H.3)
77. Nếu Controller import `MongoRepository` thì database không còn "replaceable" vì HTTP layer bị buộc chặt vào công nghệ lưu trữ.
78. Nếu API trả `ObjectId`/BSON object thay vì `String` trong DTO, frontend bị coupling vào định dạng nội bộ của Mongo (vd `{"$oid": "..."}`) và đổi database sẽ vỡ client.
79. Không nhất thiết. Logs/events/catalog linh hoạt hợp Mongo, nhưng Order/Payment cần consistency mạnh nhiều bảng thì hợp SQL hơn.
80. Có (polyglot persistence): SQL Server cho Order/Payment/ledger cần transaction và ràng buộc; MongoDB cho product catalog, review, log/event có shape thay đổi.

## Human Verification Gate (20.2)
1. Controller không inject `MongoRepository` để giữ ranh giới API/persistence, dễ mock Service, dễ đổi DB.
2. Giữ method, URL, request/response DTO, status code và error format.
3. Frontend chỉ thấy `id` là chuỗi opaque trong JSON, không cần biết kiểu lưu trữ.
4. `@Entity` map class vào bảng quan hệ; `@Document` map class vào collection của MongoDB.
5. MongoDB tự sinh `_id` (ObjectId) khi lưu document mới nên không cần `@GeneratedValue`.
6. Review thuộc 1 product, đọc cùng product, không lifecycle riêng và bounded trong lab.
7. Category dùng chung, có lifecycle riêng và được query độc lập.
8. `Product.categoryId` vẫn giữ giá trị cũ (dangling reference) nếu Service không chặn; lab chặn bằng `CategoryService.delete` → 409.
9. Khi có field được filter/sort thường xuyên hoặc cần ràng buộc unique (vd `categoryId`, `sku`).
10. Cùng `Pageable`/`Page`/`Sort` và derived query naming.
11. Không; Mongo dùng derived query, `@Query` với JSON filter hoặc aggregation/`MongoTemplate`.
12. Không; `@DataJpaTest` dành cho JPA, repository Mongo dùng `@DataMongoTest`/Testcontainers.
13. Schema linh hoạt không đảm bảo dữ liệu hợp lệ; DTO/validation là ranh giới API và bảo vệ dữ liệu.
14. Core ledger ngân hàng, hệ thống tài chính nhiều bảng cần ràng buộc/transaction chặt.
15. Event/log, product catalog thuộc tính thay đổi, content/CMS, dữ liệu dạng document lồng nhau.

## Exit ticket
- **Giữ nguyên:** REST contract, DTO, error format, kiến trúc 3 lớp.
- **Buộc phải thay:** model/annotation, repository, cấu hình kết nối, chiến lược test persistence.
- **Quyết định embed/reference bảo vệ được:** Review embed vì đọc cùng product + bounded; Category reference vì dùng chung + lifecycle riêng.
- **Rủi ro khi chọn Mongo chỉ vì "linh hoạt":** mất ràng buộc toàn vẹn, document phình to, query/index thiếu kế hoạch, dữ liệu bẩn không ai enforce.
