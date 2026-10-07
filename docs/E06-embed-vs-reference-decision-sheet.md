# E06 – Embed-vs-Reference Decision Sheet (Phụ lục B)

| Quan hệ | Read together? | Shared? | Independent lifecycle? | Bounded? | Quyết định | Lý do |
|---|---|---|---|---|---|---|
| Product → Review | Có (trang chi tiết luôn hiện review) | Không (review thuộc đúng 1 product) | Không (xoá product thì review mất theo) | Có trong lab (số review nhỏ, kiểm soát) | **Embed** `Product.reviews` | Một lần đọc lấy đủ dữ liệu, thêm review atomic trong 1 document, không cần join. |
| Product → Category | Không bắt buộc (list product không cần toàn bộ category) | Có (nhiều product cùng category) | Có (tạo/đổi tên/xoá category độc lập) | Không áp dụng | **Reference** `categoryId` | Category dùng chung; đổi tên category chỉ sửa 1 chỗ; query category độc lập. |
| Product → Review (hệ thống thật, 500.000 review) | Không (chỉ cần trang đầu) | Không | Có thể (moderation, xoá riêng) | **Không** | **Collection `reviews` riêng** (`productId` + index, phân trang) | Embed mảng tăng vô hạn làm document phình to (giới hạn 16MB), mỗi lần đọc/ghi kéo cả mảng. |
| Order → OrderItem (gợi ý bài tập) | Có | Không | Không | Có | Embed + **snapshot** tên/giá tại thời điểm mua | Cần lịch sử giá ngay cả khi Product đổi giá. |

## Human Gate (mục 5.6)
1. **Vì sao Review embed, Category reference?** Review luôn đọc cùng Product, không dùng chung, không có lifecycle độc lập và bounded → embed. Category được nhiều Product dùng chung, có lifecycle riêng → reference.
2. **500.000 review có tiếp tục embed không?** Không. Mảng không giới hạn làm document tăng kích thước, vượt giới hạn 16MB, mỗi lần đọc/ghi kéo toàn bộ mảng và khó phân trang/query độc lập. Tách collection `reviews` có `productId` và index.
3. **MongoDB có foreign key không?** Không. Nếu `categoryId` sai thì **Service layer** chịu trách nhiệm (`ProductService.validateCategory` ném `ResourceNotFoundException` → 404).
4. **Khi nào duplication là chủ đích?** Khi cần snapshot lịch sử (vd `productName`, `unitPrice` trong `OrderItem`) hoặc denormalize field đọc nhiều/ít đổi (vd `categoryName` trong Product) để tránh lookup – chấp nhận tốn công đồng bộ khi nguồn đổi.
