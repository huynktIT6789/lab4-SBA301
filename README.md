# SBA301 Lab 04.1 – REST + MongoDB

Spring Boot 3.4 RESTful Web Service (Product – Category – Review) dùng **Spring Data MongoDB**.
Nâng cấp từ Lab 04 (JPA/SQL): API contract giữ nguyên, persistence đổi sang MongoDB.

```
React/Postman -> REST Controller -> Service -> MongoRepository -> MongoDB
```

## Prerequisites
- JDK 21
- Maven 3.9+
- MongoDB Community Server (Windows service `MongoDB` đang **Running**)
- MongoDB Compass
- Postman
- (Tuỳ chọn) Docker – chỉ để chạy repository integration test bằng Testcontainers

## Database
```
mongodb://localhost:27017/sba301_lab41
```
Database/collection chỉ xuất hiện trong Compass sau khi app ghi document đầu tiên (seed tự chạy khi `categories` rỗng).

## Run
1. Start MongoDB service (`sc query MongoDB` → RUNNING)
2. Chạy app:
   ```
   mvn spring-boot:run
   ```
3. Mở Compass → connect `mongodb://localhost:27017` → database `sba301_lab41`
4. Import `postman/Lab41.postman_collection.json` vào Postman → chạy theo thứ tự 00 → 20
   (request 00 tạo category và tự lưu `categoryId`, request 01 tự lưu `productId`)

## Test
```
mvn test
```
| Test | Loại | Cần MongoDB? |
|---|---|---|
| `ProductServiceTest` | Service unit (Mockito) | Không |
| `ProductControllerTest` | `@WebMvcTest` + MockMvc | Không |
| `ProductRepositoryTest` | `@DataMongoTest` + Testcontainers | Cần Docker (tự bỏ qua nếu không có Docker) |

## API
| Method | URL | Kết quả |
|---|---|---|
| GET | `/api/products?keyword=&categoryId=&page=0&size=10&sortBy=name&direction=asc` | 200, `PageResponse` |
| GET | `/api/products/{id}` | 200 / 404 |
| POST | `/api/products` | 201 + `Location` / 400 / 404 / 409 |
| PUT | `/api/products/{id}` | 200 / 400 / 404 |
| DELETE | `/api/products/{id}` | 204 / 404 |
| POST | `/api/products/{id}/reviews` | 200 / 400 / 404 |
| GET/POST | `/api/categories` | endpoint phụ để lấy/tạo `categoryId` thật |
| DELETE | `/api/categories/{id}` | 204 / 404 / 409 nếu còn product tham chiếu |

Error format thống nhất: `{ "code": "NOT_FOUND", "message": "..." }`
(`NOT_FOUND` 404, `VALIDATION_ERROR` / `BAD_REQUEST` 400, `DUPLICATE_KEY` / `CONFLICT` 409, `DB_UNAVAILABLE` 503).

Guardrail: `sortBy` chỉ nhận `name | price | stock`; `size` bị chặn tối đa 50.

## Modeling decision
- **Review được embed** trong `Product.reviews`: luôn đọc cùng product, không có lifecycle độc lập, không dùng chung, và (trong lab) bounded. Thêm review là cập nhật atomic trên 1 document.
- **Category được reference bằng `categoryId`**: Category có lifecycle độc lập, được nhiều product dùng chung, và được query/cập nhật riêng. MongoDB không enforce FK nên `ProductService.validateCategory` kiểm tra tồn tại, `CategoryService.delete` chặn xoá category đang được tham chiếu.
- Nếu 1 product có hàng trăm nghìn review → **không embed nữa**, tách collection `reviews` (có `productId` + index) và phân trang.

Chi tiết: `docs/E06-embed-vs-reference-decision-sheet.md`.

## Ghi chú kỹ thuật
- `price` lưu `Decimal128` (`@Field(targetType = DECIMAL128)`) để không mất độ chính xác; trong Compass hiển thị `NumberDecimal("120.00")` thay vì string như ví dụ trong lab guide.
- Index: `categoryId` (filter theo category), `sku` (unique + sparse). Bật bằng `spring.data.mongodb.auto-index-creation=true`.
- `sku` là field tuỳ chọn thêm vào để thực hiện bài unique-index (#12, Break-It #44).
- Tắt seed: `app.seed.enabled=false`.

## Evidence (E01–E15)
Xem `docs/evidence-checklist.md` – ghi rõ file nào đã có sẵn trong repo và mục nào em phải tự chụp màn hình trên máy mình (E01–E03, E10–E13).
