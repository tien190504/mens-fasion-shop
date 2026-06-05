# MVP Backend API Audit

Date: 2026-06-05

This audit checks only the backend API surface for the Phase 1 MVP items from the image. Frontend behavior is out of scope.

## Verification

- Added PostgreSQL Flyway schema: `src/main/resources/db/migration/V1__init_schema.sql`.
- Added integration tests: `src/test/java/com/powerranger/mens_fashion_backend/MensFashionBackendApplicationTests.java`.
- Ran `mvnw test`: build passed. The integration tests were skipped locally because Docker/Testcontainers could not connect to Docker Desktop.
- To execute the API tests for real, start Docker Desktop and rerun `mvnw test`.

## MVP Status Matrix

| MVP area | Status | API evidence | Tested in integration flow | Remaining gaps |
| --- | --- | --- | --- | --- |
| Product page + variants | Partial | `GET /api/v1/products`, `GET /api/v1/products/{slug}`, admin `POST/PUT/DELETE /api/v1/products`; responses include images, variants, SKU, size, color, stock. | Product create, list, detail, stock after inventory/order. | No dedicated zoom/image ordering API beyond image URLs; no variant-level CRUD endpoint; invalid enum values can still return generic errors. |
| Categories + filters | Partial | `GET /api/v1/categories`, admin category CRUD; `GET /api/v1/brands`, admin brand CRUD; product filter supports `keyword`, `categoryId`, `brandId`, `gender`. | Brand/category create and filtered product list. | Missing size/color/price filters; no PostgreSQL full-text index; `GET /api/v1/brands/{slug}` and `GET /api/v1/categories/{slug}` are not public in `SecurityConfig`. |
| Cart guest + login | Partial / bug | `GET /api/v1/cart`, `POST /api/v1/cart/items`, `PUT /api/v1/cart/items/{itemId}`, `DELETE`; service supports `sessionToken` and merge logic. | Logged-in add/update cart. | Guest cart endpoints are blocked by security because `/api/v1/cart/**` requires auth; merge-on-login is not reachable through a public guest flow. |
| Checkout + payment | Partial | `POST /api/v1/orders` creates order from logged-in cart, calculates subtotal/shipping, stores `paymentMethod`, decrements stock, records inventory movement. | Place COD order from cart. | No payment transaction API/callback for MoMo, VNPay, bank transfer, COD confirmation, or `payment_transactions` updates. |
| Shipping + tracking | Partial | Admin `POST /api/v1/admin/shipments`, `PATCH /api/v1/admin/shipments/{id}/status`, `GET /api/v1/admin/shipments/order/{orderId}`. | Admin create shipment, update status, get shipment by order. | No customer/public tracking endpoint by order/tracking number; no carrier integration with GHN/GHTK. |
| Account + addresses | Mostly done | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/users/profile`, address CRUD under `/api/v1/addresses`. | Register admin/user, create/update/list address. | No profile update API; no forgot/reset password; no address selection during checkout except raw shipping address text. |
| Order management | Partial | User `GET /api/v1/orders`, `GET /api/v1/orders/{id}`; admin `GET /api/v1/admin/orders`, `PATCH /api/v1/admin/orders/{id}/status`. | User order list/detail, admin permission, admin status update. | No invoice API; no status transition rules; no exposed status history endpoint. `oldStatus` recording was corrected in this pass. |
| Inventory management | Partial | Admin `POST /api/v1/admin/inventory/movements`; order placement/cancel adjusts stock and logs movements. | Admin stock adjustment and product stock verification. | `GET /api/v1/admin/inventory/variants/{variantId}/movements` returns JPA entities directly, which risks lazy serialization errors; should return DTOs. |
| Product search | Partial | `GET /api/v1/products?keyword=...` uses case-insensitive `LIKE` against product name. | Keyword search with category/brand/gender filters. | Not PostgreSQL full-text search; no autocomplete endpoint; search ignores description, brand, category, size, color. |

## Implemented Fixes During Audit

- Added the missing Flyway schema so a clean PostgreSQL database can satisfy `spring.jpa.hibernate.ddl-auto=validate`.
- Fixed `PaymentTransactionRepository` derived query to use the existing `transactionRef` entity field.
- Fixed order coupon usage persistence to include `order_id`.
- Fixed order status history so `oldStatus` is captured before changing the order status.

## Follow-up Acceptance Tests To Add After Features Exist

- Guest cart: unauthenticated `sessionToken` can add/update items, then authenticated user can merge guest cart.
- Product filters: size, color, min/max price, category, brand, and gender can be combined.
- Search: PostgreSQL full-text search ranks product name/description and autocomplete returns suggestions.
- Payments: create order with MoMo/VNPay/bank transfer, persist `payment_transactions`, handle gateway callback, update `paymentStatus`.
- Shipping tracking: customer can track own shipment without admin privileges.
- Inventory movement history: movement list endpoint returns DTOs and does not expose or serialize lazy JPA entities.
