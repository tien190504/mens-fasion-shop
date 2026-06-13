# Đánh Giá Kiểm Thử API Backend MVP

> **Ngày cập nhật:** 2026-06-13  
> **Trạng thái Phase 1 MVP:** 100% Hoàn thành | Toàn bộ các khoảng trống (gaps) đã được giải quyết  
> **Công nghệ sử dụng (Tech stack):** Spring Boot + PostgreSQL + Flyway + JWT (HS256)

Bản kiểm tra này đánh giá chi tiết tình trạng hoàn thiện giao diện API backend cho các hạng mục MVP của Phase 1 dựa trên thiết kế yêu cầu.

---

## 1. Kiểm Chứng (Verification)

* **Cơ sở dữ liệu (Flyway Schema)**:
  * Bổ dung migration `V3__add_fts_and_order_updates.sql` hỗ trợ các bảng nhật ký tiến trình (`shipment_status_history`), token khôi phục mật khẩu (`password_reset_tokens`), cột tìm kiếm toàn văn (`tsv` tsvector) và chuyển kiểu dữ liệu cột `status` đơn hàng sang `VARCHAR(30)`.
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

## 3. Chi Tiết Các Điểm Cuối (Endpoints) Đã Hoàn Thành Theo Phân Hệ

### 📦 Trang sản phẩm + biến thể (Product page + variants)
* **Quản lý sản phẩm**:
  * `GET /api/v1/products` - Lọc/liệt kê sản phẩm (Công khai)
  * `GET /api/v1/products/{slug}` - Lấy chi tiết sản phẩm theo slug (Công khai)
  * `POST /api/v1/products` - Tạo sản phẩm mới (Admin)
  * `PUT /api/v1/products/{id}` - Cập nhật sản phẩm (Admin)
  * `DELETE /api/v1/products/{id}` - Xóa sản phẩm (Admin)
* **Quản lý biến thể**:
  * `GET /api/v1/admin/variants` - Liệt kê danh sách biến thể (Admin)
  * `GET /api/v1/admin/variants/{id}` - Lấy chi tiết biến thể theo ID (Admin)
  * `POST /api/v1/admin/variants` - Tạo mới biến thể (Admin)
  * `PUT /api/v1/admin/variants/{id}` - Cập nhật biến thể (Admin)
  * `DELETE /api/v1/admin/variants/{id}` - Xóa biến thể (Admin)
* **Quản lý ảnh sản phẩm**:
  * `POST /api/v1/admin/products/{id}/images` - Thêm ảnh sản phẩm mới (Admin)
  * `DELETE /api/v1/admin/images/{id}` - Xóa ảnh sản phẩm (Admin)
  * `PUT /api/v1/admin/images/reorder` - Sắp xếp thứ tự ảnh sản phẩm (Admin)

### 🏷️ Danh mục + bộ lọc (Categories + filters)
* **Quản lý danh mục**:
  * `GET /api/v1/categories` - Liệt kê tất cả danh mục (Công khai)
  * `GET /api/v1/categories/{slug}` - Lấy danh mục theo slug (Công khai)
  * `POST /api/v1/categories` - Tạo danh mục mới (Admin)
  * `PUT /api/v1/categories/{id}` - Cập nhật danh mục (Admin)
  * `DELETE /api/v1/categories/{id}` - Xóa danh mục (Admin)
* **Quản lý thương hiệu**:
  * `GET /api/v1/brands` - Liệt kê tất cả thương hiệu (Công khai)
  * `GET /api/v1/brands/{slug}` - Lấy thương hiệu theo slug (Công khai)
  * `POST /api/v1/brands` - Tạo thương hiệu mới (Admin)
  * `PUT /api/v1/brands/{id}` - Cập nhật thương hiệu (Admin)
  * `DELETE /api/v1/brands/{id}` - Xóa thương hiệu (Admin)
* **Tìm kiếm nâng cao & Bộ lọc**:
  * Tích hợp lọc theo `size`, `color`, `minPrice`, `maxPrice`, `brandId`, `categoryId`, `gender`, `sort` trực tiếp trên endpoint `GET /api/v1/products`.

### 🛒 Giỏ hàng khách + đăng nhập (Cart guest + login)
* **Thao tác giỏ hàng**:
  * `GET /api/v1/cart` - Lấy thông tin giỏ hàng hiện tại (Đăng nhập / Khách vãng lai với `sessionToken`)
  * `POST /api/v1/cart/items` - Thêm sản phẩm vào giỏ hàng (Đăng nhập / Khách vãng lai với `sessionToken`)
  * `PUT /api/v1/cart/items/{itemId}` - Cập nhật số lượng mặt hàng (Đăng nhập / Khách vãng lai với `sessionToken`)
  * `DELETE /api/v1/cart/items/{itemId}` - Xóa mặt hàng khỏi giỏ (Đăng nhập / Khách vãng lai với `sessionToken`)
  * `DELETE /api/v1/cart` - Xóa sạch giỏ hàng (Đăng nhập / Khách vãng lai với `sessionToken`)

### 💳 Thanh toán + đặt hàng (Checkout + payment)
* **Đặt hàng**:
  * `POST /api/v1/orders` - Đặt đơn hàng mới từ giỏ hàng (Đăng nhập)
* **Thanh toán giả lập**:
  * `POST /api/v1/payments/create` - Tạo giao dịch và lấy URL thanh toán (Đăng nhập)
  * `POST /api/v1/payments/ipn` - Xử lý thông báo tức thời IPN từ cổng thanh toán (Công khai)
  * `GET /api/v1/payments/callback` - Tiếp nhận callback phản hồi từ cổng thanh toán (Công khai)
  * `GET /api/v1/payments/mock-checkout` - Trang giao diện Sandbox giả lập thanh toán (Công khai)

### 🚚 Vận chuyển + theo dõi (Shipping + tracking)
* **Quản lý vận đơn (Admin)**:
  * `GET /api/v1/admin/shipments/order/{orderId}` - Lấy thông tin vận đơn theo đơn hàng (Admin)
  * `POST /api/v1/admin/shipments` - Tạo bản ghi vận đơn mới (Admin)
  * `PATCH /api/v1/admin/shipments/{id}/status` - Cập nhật tiến trình giao hàng (Admin)
* **Tra cứu công khai (Customer)**:
  * `GET /api/v1/shipments/{trackingCode}` - Tra cứu chi tiết tiến trình giao hàng công khai (Công khai)

### 👤 Tài khoản + địa chỉ (Account + addresses)
* **Xác thực & Người dùng**:
  * `POST /api/v1/auth/register` - Đăng ký tài khoản mới (Công khai)
  * `POST /api/v1/auth/login` - Đăng nhập nhận token JWT (Công khai)
  * `POST /api/v1/auth/forgot-password` - Gửi email/token yêu cầu khôi phục mật khẩu (Công khai)
  * `POST /api/v1/auth/reset-password` - Đặt lại mật khẩu mới dùng token (Công khai)
* **Quản lý địa chỉ**:
  * `GET /api/v1/addresses` - Xem sổ địa chỉ cá nhân (Đăng nhập)
  * `POST /api/v1/addresses` - Thêm địa chỉ mới (Đăng nhập)
  * `PUT /api/v1/addresses/{id}` - Cập nhật thông tin địa chỉ (Đăng nhập)
  * `DELETE /api/v1/addresses/{id}` - Xóa địa chỉ (Đăng nhập)

### 📋 Quản lý đơn hàng (Order management)
* **Theo dõi đơn hàng**:
  * `GET /api/v1/orders` - Danh sách đơn hàng cá nhân (Đăng nhập)
  * `GET /api/v1/orders/{id}` - Chi tiết đơn hàng cá nhân (Đăng nhập)
  * `GET /api/v1/admin/orders` - Lấy toàn bộ danh sách đơn hàng (Admin)
  * `PATCH /api/v1/admin/orders/{id}/status` - Cập nhật trạng thái đơn hàng (Admin)
* **Hóa đơn & Lịch sử**:
  * `GET /api/v1/orders/{id}/invoice` - Xem/xuất hóa đơn dạng DTO (Đăng nhập)

### 📊 Quản lý tồn kho (Inventory management)
* **Sổ kho & Phiếu kho**:
  * `GET /api/v1/admin/inventory/variants/{variantId}/movements` - Lấy lịch sử biến động thẻ kho (Admin)
  * `POST /api/v1/admin/inventory/movements` - Ghi nhận phiếu biến động kho (Admin)

### 🔍 Tìm kiếm sản phẩm (Product search)
* **Tìm kiếm & Gợi ý**:
  * `GET /api/v1/products` - Tìm kiếm toàn văn tối ưu qua cột `tsv` (Công khai)
  * `GET /api/v1/products/autocomplete` - Tự động gợi ý từ khóa thông minh (Công khai)

---

## 4. Các Lỗi Đã Được Khắc Phục & Cải Tiến

1. **Schema Flyway**: Bổ sung migration V3 sạch, xử lý an toàn kiểu ENUM của PostgreSQL bằng cách chuyển đổi sang `VARCHAR(30)` để tránh lỗi khóa bảng khi thay đổi trạng thái đơn hàng.
2. **Bảo mật**: Mở rộng cấu hình `SecurityConfig` cho phép truy cập công khai tài liệu Swagger UI, các cổng callback thanh toán, tra cứu vận đơn public, giỏ hàng khách và API reset password.
3. **Repository**: Sửa lỗi truy vấn JPA trong `PaymentTransactionRepository` liên quan đến trường `transactionRef`.
4. **Order**: Sửa lỗi lưu vết sử dụng mã coupon đơn hàng để lưu đầy đủ `order_id` và capturing chính xác `oldStatus` trước khi cập nhật.
5. **Inventory**: Loại bỏ nguy cơ lỗi serialize khi sử dụng lazy loading ở controller kho hàng bằng cách triển khai `MovementResponseDTO`.

---

## 5. Nghiệm Thu & Khởi Chạy Thử Nghiệm

1. Khởi chạy dự án bằng lệnh:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. Truy cập tài liệu Swagger UI để xem và kiểm thử trực quan các API:
   `http://localhost:8080/swagger-ui/index.html`
3. Truy cập trang giả lập thanh toán khi thực hiện đặt đơn hàng trực tuyến:
   `http://localhost:8080/api/v1/payments/mock-checkout`
