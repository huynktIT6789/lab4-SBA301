# Evidence checklist E01–E15

| Mã | Evidence | Trạng thái | Ở đâu / em cần làm gì |
|---|---|---|---|
| E01 | Git branch + commit history | **Em tự làm** | `git init`, tạo branch `feature/lab41-mongodb`, commit theo từng bước (hoặc copy code vào repo Lab 04 của em, tạo branch rồi commit) |
| E02 | Screenshot MongoDB service Running | **Em tự chụp** | Services → MongoDB → Running, hoặc `sc query MongoDB` |
| E03 | Compass connection localhost | **Em tự chụp** | Compass connect `mongodb://localhost:27017` |
| E04 | application.properties MongoDB URI | Có | `src/main/resources/application.properties` |
| E05 | Product/Category document classes | Có | `src/main/java/com/example/lab41/model/` |
| E06 | Embed-vs-Reference Decision Sheet | Có | `docs/E06-embed-vs-reference-decision-sheet.md` |
| E07 | MongoRepository code | Có | `repository/ProductRepository.java`, `CategoryRepository.java` |
| E08 | Service validation cho manual reference | Có | `ProductService.validateCategory`, `CategoryService.delete` |
| E09 | REST Controller contract comparison | Có | `docs/appendix-C-upgrade-trace.md` + `docs/E14-jpa-vs-mongodb-reflection.md` |
| E10 | Postman collection + verification matrix | Collection có; **em chạy và chụp** | `postman/…json`, `docs/verification-matrix.md` (điền cột kết quả) |
| E11 | Compass product document + embedded review | **Em tự chụp** | Chạy Postman 12 rồi mở `products` trong Compass, mở mảng `reviews` |
| E12 | Index screenshot | **Em tự chụp** | Compass → `products` → tab Indexes: `_id_`, `categoryId_1`, `sku_1` |
| E13 | Automated test result | **Em chạy và chụp** | `mvn test` (ProductRepositoryTest cần Docker, nếu không có sẽ "skipped") |
| E14 | JPA vs MongoDB comparison reflection | Có | `docs/E14-jpa-vs-mongodb-reflection.md` |
| E15 | README run instructions | Có | `README.md` |

Lưu ý: giảng viên có yêu cầu **giải thích được ít nhất một quyết định embed/reference và một thay đổi architecture** – đọc kỹ E06 và E14 trước khi nộp/bảo vệ.
