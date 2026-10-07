# Postman Verification Matrix (mục XII)

Collection: `postman/Lab41.postman_collection.json` (mỗi request có test script sẵn). Cột "Kết quả thực tế" để em tự điền khi chạy trên máy mình.

| # | Request | Expected | Request trong collection | Evidence | Kết quả thực tế |
|---|---|---|---|---|---|
| 1 | POST /api/products valid | 201 + Location + body | 01 | Screenshot | |
| 2 | POST invalid name | 400 error JSON | 02 | Error JSON | |
| 3 | POST invalid categoryId | 404 (theo contract của service) | 03 | Error JSON | |
| 4 | GET existing id | 200 | 04 | Body | |
| 5 | GET missing id | 404 | 05 | Error JSON | |
| 6 | PUT existing id | 200 updated body | 06 | Updated body | |
| 7 | DELETE existing id | 204 no body | 19 | No body | |
| 8 | GET page=0&size=5 | 200, tối đa 5 items | 07 | Page JSON | |
| 9 | Sort by price | Đúng thứ tự | 08 | So sánh giá trị | |
| 10 | Filter category | Đúng tập con | 09 | Response | |
| 11 | Create embedded Review | Product document thay đổi | 12 | Compass | |
| 12 | Duplicate unique sku | Controlled error (409) | 14 + 15 | HTTP + log | |
| 13 | Compass verify collections | `products`, `categories` tồn tại | — | Screenshot | |
| 14 | Frontend/consumer | Contract không đổi | — | Optional | |

## Break-It scenarios (mục 15.1)
| # | Kịch bản | Cách làm | Kỳ vọng |
|---|---|---|---|
| 37 | Dừng MongoDB service rồi gọi API | `net stop MongoDB` rồi gọi GET | 503 `DB_UNAVAILABLE` (sau timeout ~30s của driver) |
| 38 | categoryId không tồn tại | Postman 03 | 404 |
| 39 | price âm | Postman 16 | 400 |
| 40 | JSON sai kiểu | Postman 17 | 400 |
| 41 | sort field ngoài whitelist | Postman 11 | 400 |
| 42 | Embed 1.000 review giả | Gọi POST review nhiều lần | Document phình to → minh hoạ vì sao design xấu |
| 43 | Xoá Category đang được tham chiếu | Postman 18 | 409 (muốn thấy dangling reference: tạm bỏ check trong `CategoryService.delete`) |
| 44 | Duplicate sku | Postman 14 + 15 | 409 |
