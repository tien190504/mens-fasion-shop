# Đánh Giá Kiểm Thử API Backend MVP

Ngày thực hiện: 2026-06-05

Bản kiểm tra này chỉ đánh giá giao diện API backend cho các hạng mục MVP của Phase 1 dựa trên hình ảnh được cung cấp. Hành vi của frontend nằm ngoài phạm vi kiểm tra.

## Kiểm Chứng (Verification)

- Đã thêm cấu trúc cơ sở dữ liệu PostgreSQL Flyway: `src/main/resources/db/migration/V1__init_schema.sql`.
- Đã thêm các bài kiểm thử tích hợp (integration tests): `src/test/java/com/powerranger/fashion_shop_backend/FashionShopBackendApplicationTests.java`.
- Đã chạy lệnh `mvnw test`: dự án build thành công. Các bài kiểm thử tích hợp đã được bỏ qua tự động tại local do Testcontainers không thể kết nối tới Docker Desktop.
- Để chạy các kiểm thử API thực tế, hãy khởi động Docker Desktop và chạy lại lệnh `mvnw test`.

## Ma Trận Trạng Thái MVP

| Phân hệ MVP | Trạng thái | Minh chứng API | Kiểm thử trong luồng tích hợp | Khoảng trống còn thiếu |
| --- | --- | --- | --- | --- |
| Trang sản phẩm + biến thể | Một phần | `GET /api/v1/products`, `GET /api/v1/products/{slug}`, admin `POST/PUT/DELETE /api/v1/products`; phản hồi trả về bao gồm ảnh, biến thể, mã SKU, size, màu sắc, số lượng tồn kho. | Tạo sản phẩm, hiển thị danh sách, xem chi tiết, số lượng tồn kho sau khi thay đổi tồn kho/đơn hàng. | Chưa có API chuyên dụng để sắp xếp thứ tự ảnh hoặc phóng to ngoài URL ảnh; thiếu endpoint CRUD riêng cho biến thể; các giá trị enum không hợp lệ có thể vẫn trả về lỗi chung. |
| Danh mục + bộ lọc | Một phần | `GET /api/v1/categories`, admin CRUD danh mục; `GET /api/v1/brands`, admin CRUD thương hiệu; bộ lọc sản phẩm hỗ trợ `keyword`, `categoryId`, `brandId`, `gender`. | Tạo thương hiệu/danh mục và danh sách sản phẩm được lọc. | Thiếu bộ lọc size/màu sắc/khoảng giá; thiếu chỉ mục tìm kiếm toàn văn PostgreSQL; các endpoint `GET /api/v1/brands/{slug}` và `GET /api/v1/categories/{slug}` chưa được mở công khai trong `SecurityConfig`. |
| Giỏ hàng khách + đăng nhập | Một phần / có lỗi | `GET /api/v1/cart`, `POST /api/v1/cart/items`, `PUT /api/v1/cart/items/{itemId}`, `DELETE`; service hỗ trợ `sessionToken` và logic gộp giỏ hàng. | Thêm/cập nhật giỏ hàng khi đã đăng nhập. | Các endpoint giỏ hàng khách bị chặn bởi bảo mật do `/api/v1/cart/**` yêu cầu đăng nhập; luồng gộp giỏ hàng khi đăng nhập không thể truy cập được thông qua luồng khách công khai. |
| Thanh toán + đặt hàng | Một phần | `POST /api/v1/orders` tạo đơn hàng từ giỏ hàng đã đăng nhập, tính toán tổng phụ/phí vận chuyển, lưu phương thức thanh toán `paymentMethod`, trừ tồn kho và ghi nhận lịch sử biến động kho. | Đặt đơn hàng COD từ giỏ hàng. | Thiếu API giao dịch thanh toán/callback cho MoMo, VNPay, chuyển khoản ngân hàng, xác nhận COD hoặc cập nhật bảng `payment_transactions`. |
| Vận chuyển + theo dõi | Một phần | Admin `POST /api/v1/admin/shipments`, `PATCH /api/v1/admin/shipments/{id}/status`, `GET /api/v1/admin/shipments/order/{orderId}`. | Admin tạo vận đơn, cập nhật trạng thái, lấy thông tin vận đơn theo đơn hàng. | Thiếu endpoint cho khách hàng theo dõi công khai theo đơn hàng/mã vận đơn; thiếu tích hợp với các đơn vị vận chuyển GHN/GHTK. |
| Tài khoản + địa chỉ | Hầu như hoàn thành | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/users/profile`, CRUD địa chỉ dưới `/api/v1/addresses`. | Đăng ký admin/user, tạo/cập nhật/liệt kê địa chỉ. | Thiếu API cập nhật profile; thiếu quên/đặt lại mật khẩu; thiếu tích hợp chọn địa chỉ nhận hàng trong luồng đặt hàng (ngoài việc nhập text thuần). |
| Quản lý đơn hàng | Một phần | User `GET /api/v1/orders`, `GET /api/v1/orders/{id}`; admin `GET /api/v1/admin/orders`, `PATCH /api/v1/admin/orders/{id}/status`. | Danh sách/chi tiết đơn hàng của người dùng, phân quyền admin, cập nhật trạng thái phía admin. | Thiếu API xuất hóa đơn; thiếu quy tắc chuyển đổi trạng thái; thiếu endpoint hiển thị lịch sử trạng thái đơn hàng. Việc ghi nhận `oldStatus` đã được sửa trong lượt này. |
| Quản lý tồn kho | Một phần | Admin `POST /api/v1/admin/inventory/movements`; việc đặt hàng/hủy đơn điều chỉnh tồn kho và ghi nhận lịch sử biến động kho. | Admin điều chỉnh tồn kho và xác minh số lượng tồn kho sản phẩm. | `GET /api/v1/admin/inventory/variants/{variantId}/movements` trả về trực tiếp thực thể JPA, dẫn đến nguy cơ lỗi tuần tự hóa tải chậm (lazy serialization errors); cần đổi sang trả về DTO. |
| Tìm kiếm sản phẩm | Một phần | `GET /api/v1/products?keyword=...` sử dụng so khớp `LIKE` không phân biệt chữ hoa thường trên tên sản phẩm. | Tìm kiếm theo từ khóa kết hợp bộ lọc danh mục/thương hiệu/giới tính. | Không sử dụng tìm kiếm toàn văn PostgreSQL; thiếu endpoint tự động gợi ý; tìm kiếm bỏ qua mô tả, thương hiệu, danh mục, kích thước, màu sắc. |

## Các Lỗi Đã Được Khắc Phục Trong Quá Trình Đánh Giá

- Bổ sung schema Flyway bị thiếu để cơ sở dữ liệu PostgreSQL sạch đáp ứng được cấu hình `spring.jpa.hibernate.ddl-auto=validate`.
- Sửa truy vấn JPA trong `PaymentTransactionRepository` để khớp với trường thực thể `transactionRef` hiện có.
- Sửa lỗi lưu vết sử dụng mã coupon của đơn hàng để bổ sung `order_id`.
- Sửa lỗi lưu lịch sử trạng thái đơn hàng để đảm bảo `oldStatus` được ghi lại chính xác trước khi thay đổi.

## Các Bài Kiểm Thử Nghiệm Thu Cần Bổ Sung Sau Khi Hoàn Thành Chức Năng

- Giỏ hàng khách: khách chưa đăng nhập sử dụng `sessionToken` có thể thêm/cập nhật sản phẩm, sau đó người dùng đăng nhập có thể gộp giỏ hàng khách.
- Bộ lọc sản phẩm: kích thước, màu sắc, giá tối thiểu/tối đa, danh mục, thương hiệu và giới tính có thể được lọc kết hợp.
- Tìm kiếm: tìm kiếm toàn văn PostgreSQL xếp hạng theo tên/mô tả sản phẩm và tự động gợi ý từ khóa phù hợp.
- Thanh toán: tạo đơn hàng với MoMo/VNPay/chuyển khoản ngân hàng, lưu trữ dữ liệu `payment_transactions`, xử lý callback từ cổng thanh toán và cập nhật `paymentStatus`.
- Theo dõi vận đơn: khách hàng có thể tra cứu hành trình vận chuyển của mình mà không cần quyền quản trị viên.
- Lịch sử sổ kho: endpoint danh sách biến động kho trả về DTOs, không trả trực tiếp thực thể JPA tránh lỗi lazy loading.
