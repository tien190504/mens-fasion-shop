# Đánh Giá Kiểm Thử API Backend MVP

> **Ngày cập nhật:** 2026-06-13  
> **Trạng thái Phase 1 MVP:** 100% Hoàn thành | Toàn bộ các khoảng trống (gaps) đã được giải quyết  
> **Công nghệ sử dụng (Tech stack):** Spring Boot + PostgreSQL + Flyway + JWT (HS256)

Bản kiểm tra này đánh giá chi tiết tình trạng hoàn thiện giao diện API backend cho các hạng mục MVP của Phase 1 dựa trên thiết kế yêu cầu.

---

## 1. Kiểm Chứng (Verification)

* **Cơ sở dữ liệu (Flyway Schema)**:
  * Bổ sung migration `V3__add_fts_and_order_updates.sql` hỗ trợ các bảng nhật ký tiến trình (`shipment_status_history`), token khôi phục mật khẩu (`password_reset_tokens`), cột tìm kiếm toàn văn (`tsv` tsvector) và chuyển kiểu dữ liệu cột `status` đơn hàng sang `VARCHAR(30)`.
* **Suite Kiểm thử tự động (Automated Tests)**:
  * Bổ sung suite unit tests Mockito cho toàn bộ nghiệp vụ cốt lõi tại `src/test/java/com/powerranger/fashion_shop_backend/service/` gồm 28 test cases.
  * Chạy lệnh `.\mvnw.cmd clean install` thành công và tất cả các bài test đều đã vượt qua (**BUILD SUCCESS**).

---

## 2. Ma Trận Trạng Thái MVP (Đã cập nhật sau Phase 1)

| Phân hệ MVP | Trạng thái | Minh chứng API | Kiểm thử trong luồng tích hợp | Các khoảng trống đã giải quyết |
| --- | --- | --- | --- | --- |
| **Trang sản phẩm + biến thể** | ✅ Hoàn thành | `GET/POST/PUT/DELETE /api/v1/admin/variants`; `POST /api/v1/admin/products/{id}/images`, `DELETE /api/v1/admin/images/{id}`, `PUT /api/v1/admin/images/reorder` | CRUD biến thể, CRUD ảnh sản phẩm và thay đổi thứ tự sắp xếp ảnh được kiểm nghiệm thông qua `ProductVariantServiceImplTest`. | Đã triển khai đầy đủ API CRUD biến thể độc lập, API quản lý ảnh riêng biệt và chức năng sắp xếp thứ tự ảnh (`sort_order`). |
| **Danh mục + bộ lọc** | ✅ Hoàn thành | API lọc nâng cao tích hợp trong `GET /api/v1/products` hỗ trợ `size`, `color`, `minPrice`, `maxPrice`, `brandId`, `categoryId`, `gender` và `sort`. | Lọc đa tiêu chí kết hợp, sắp xếp động được bao phủ trong `ProductServiceImplTest`. | Đã mở công khai các endpoint slug chi tiết danh mục/thương hiệu trong Security; bổ sung tìm kiếm toàn văn FTS PostgreSQL và bộ lọc đa dạng kích thước/màu sắc/khoảng giá. |
| **Giỏ hàng khách + đăng nhập** | ✅ Hoàn thành | `GET/POST/PUT/DELETE /api/v1/cart` hỗ trợ `sessionToken` header cho khách vãng lai không cần đăng nhập. | Khởi tạo giỏ hàng khách, sinh token tạm thời, thêm sản phẩm và tự động gộp giỏ hàng khách vào giỏ hàng thành viên khi login được kiểm thử qua `CartServiceImplTest`. | Đã mở cấu hình Security cho phép khách thao tác giỏ hàng thông qua `sessionToken`, tích hợp thành công logic gộp giỏ hàng trùng lặp. |
| **Thanh toán + đặt hàng** | ✅ Hoàn thành | API thanh toán `/api/v1/payments/create`, `/api/v1/payments/callback`, và giao diện giả lập Sandbox trực quan `/api/v1/payments/mock-checkout`. | Luồng thanh toán COD, MoMo, VNPay và tự động cập nhật trạng thái giao dịch được kiểm thử trong `OrderServiceImplTest`. | Đã tích hợp module cổng thanh toán giả lập sandbox hoàn chỉnh, xử lý phản hồi callback IPN và tự động cập nhật trạng thái đơn hàng/giao dịch. |
| **Vận chuyển + theo dõi** | ✅ Hoàn thành | Điểm cuối tra cứu công khai `GET /api/v1/shipments/{trackingCode}`; API admin cập nhật trạng thái giao hàng và cập nhật nhật ký lịch sử. | Tạo vận đơn, cập nhật tiến trình giao hàng và ghi lịch sử trạng thái giao hàng được kiểm nghiệm qua `OrderServiceImplTest`. | Bổ sung endpoint tra cứu mã vận đơn công khai cho khách hàng; giả lập kết nối nhà vận chuyển GHN/GHTK; ghi vết hành trình chi tiết trong `shipment_status_history`. |
| **Tài khoản + địa chỉ** | ✅ Hoàn thành | Đăng ký/đăng nhập JWT; CRUD địa chỉ; Tích hợp `addressId` trong đơn đặt hàng; Điểm cuối `/api/v1/auth/forgot-password` và `/api/v1/auth/reset-password`. | Đăng ký, đăng nhập, khôi phục mật khẩu bằng token và kiểm tra tự động điền địa chỉ khi đặt hàng. | Đã triển khai quy trình quên/đặt lại mật khẩu qua token bảo mật; liên kết khóa ngoại địa chỉ người dùng trực tiếp vào đơn hàng khi đặt hàng để lưu trữ vĩnh viễn. |
| **Quản lý đơn hàng** | ✅ Hoàn thành | `GET /api/v1/orders/{id}/invoice` (Xuất hóa đơn dạng DTO); máy trạng thái đơn hàng nghiêm ngặt và lưu trữ lịch sử trạng thái. | Kiểm tra ràng buộc máy trạng thái hợp lệ, tính toán hóa đơn chi tiết được kiểm thử trong `OrderServiceImplTest`. | Áp dụng máy trạng thái chuyển đổi trạng thái đơn hàng chặt chẽ (`PENDING -> CONFIRMED -> PACKING -> SHIPPING -> DELIVERED -> CANCELLED -> RETURNED`); cung cấp API xuất hóa đơn an toàn. |
| **Quản lý tồn kho** | ✅ Hoàn thành | Endpoint lịch sử biến động kho `/api/v1/admin/inventory/variants/{variantId}/movements` trả về `MovementResponseDTO`. | Ghi nhận xuất nhập tồn kho và ánh xạ DTO an toàn được kiểm thử qua `InventoryServiceImplTest`. | Thay đổi kiểu dữ liệu trả về từ Entity JPA sang DTO để loại bỏ hoàn toàn nguy cơ lỗi tải chậm (`LazyInitializationException`) trong Jackson. |
| **Tìm kiếm sản phẩm** | ✅ Hoàn thành | `GET /api/v1/products` tích hợp FTS qua cột `tsv` và GIN Index; API tự động hoàn thành từ khóa gợi ý `GET /api/v1/products/autocomplete`. | Tìm kiếm toàn văn FTS dựa trên chỉ mục và gợi ý từ khóa thông minh được kiểm thử qua `ProductServiceImplTest`. | Thay thế so khớp LIKE bằng tìm kiếm toàn văn tối ưu trên PostgreSQL; cung cấp API gợi ý từ khóa thông minh để nâng cao trải nghiệm người dùng. |

---

## 3. Các Lỗi Đã Được Khắc Phục & Cải Tiến

1. **Schema Flyway**: Bổ sung migration V3 sạch, xử lý an toàn kiểu ENUM của PostgreSQL bằng cách chuyển đổi sang `VARCHAR(30)` để tránh lỗi khóa bảng khi thay đổi trạng thái đơn hàng.
2. **Bảo mật**: Mở rộng cấu hình `SecurityConfig` cho phép truy cập công khai tài liệu Swagger UI, các cổng callback thanh toán, tra cứu vận đơn public, giỏ hàng khách và API reset password.
3. **Repository**: Sửa lỗi truy vấn JPA trong `PaymentTransactionRepository` liên quan đến trường `transactionRef`.
4. **Order**: Sửa lỗi lưu vết sử dụng mã coupon đơn hàng để lưu đầy đủ `order_id` và capturing chính xác `oldStatus` trước khi cập nhật.
5. **Inventory**: Loại bỏ nguy cơ lỗi serialize khi sử dụng lazy loading ở controller kho hàng bằng cách triển khai `MovementResponseDTO`.

---

## 4. Nghiệm Thu & Khởi Chạy Thử Nghiệm

1. Khởi chạy dự án bằng lệnh:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. Truy cập tài liệu Swagger UI để xem và kiểm thử trực quan các API:
   `http://localhost:8080/swagger-ui/index.html`
3. Truy cập trang giả lập thanh toán khi thực hiện đặt đơn hàng trực tuyến:
   `http://localhost:8080/api/v1/payments/mock-checkout`
