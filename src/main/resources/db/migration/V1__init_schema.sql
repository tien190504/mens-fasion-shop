-- =====================================================================
--  FASHION SHOP - PostgreSQL Database Schema (FULL / v2)
--  Web bán hàng thời trang / giày dép
--  PostgreSQL 14+
-- =====================================================================
--  Đã gộp đầy đủ:
--   - Schema gốc (catalog, variant, cart, order, payment, review...)
--   - Guest cart (giỏ hàng không cần login)
--   - Giới hạn mã giảm giá theo từng user
--   - order_status_history  (lịch sử trạng thái đơn)
--   - inventory_movements   (sổ kho ghi +/- tồn)
--   - coupon_products / coupon_categories (mã giảm theo SP/danh mục)
--   - shipments             (thông tin vận chuyển)
--   - product_categories    (1 SP thuộc nhiều danh mục - hybrid)
-- =====================================================================

BEGIN;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
--  ENUM TYPES
-- =====================================================================
CREATE TYPE order_status AS ENUM (
    'pending', 'confirmed', 'processing', 'shipping',
    'delivered', 'cancelled', 'refunded'
);

CREATE TYPE payment_status AS ENUM (
    'unpaid', 'paid', 'partially_refunded', 'refunded', 'failed'
);

CREATE TYPE payment_method AS ENUM (
    'cod', 'momo', 'vnpay', 'bank_transfer', 'credit_card'
);

CREATE TYPE discount_type AS ENUM ('percentage', 'fixed_amount');

CREATE TYPE gender_target AS ENUM ('men', 'women', 'unisex', 'kids');

-- Lý do thay đổi tồn kho (sổ kho)
CREATE TYPE inventory_reason AS ENUM (
    'purchase',     -- nhập hàng (+)
    'sale',         -- bán ra    (-)
    'return',       -- khách trả (+)
    'cancellation', -- hủy đơn, hoàn kho (+)
    'adjustment'    -- kiểm kê / chỉnh tay (+/-)
);

-- Trạng thái vận chuyển
CREATE TYPE shipment_status AS ENUM (
    'pending', 'picked_up', 'in_transit',
    'delivered', 'failed', 'returned'
);

-- =====================================================================
--  USERS / KHÁCH HÀNG
-- =====================================================================
-- @users: Tài khoản (cả khách lẫn admin), phân biệt bằng cờ is_admin.
CREATE TABLE users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(20)  UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    is_admin        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- @addresses: Sổ địa chỉ giao hàng của user (theo Tỉnh/Quận/Phường VN), 1 user nhiều địa chỉ.
CREATE TABLE addresses (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_name  VARCHAR(150) NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    province        VARCHAR(100) NOT NULL,   -- Tỉnh/Thành phố
    district        VARCHAR(100) NOT NULL,   -- Quận/Huyện
    ward            VARCHAR(100),            -- Phường/Xã
    street_address  VARCHAR(255) NOT NULL,   -- Số nhà, tên đường
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_addresses_user ON addresses(user_id);

-- =====================================================================
--  CATALOG: BRAND, CATEGORY
-- =====================================================================
-- @brands: Thương hiệu sản phẩm (Nike, Adidas...).
CREATE TABLE brands (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    slug        VARCHAR(140) NOT NULL UNIQUE,
    logo_url    VARCHAR(500),
    description TEXT
);

-- Danh mục dạng cây (self-reference)
-- @categories: Danh mục dạng cây (parent_id tự trỏ về chính nó) để lồng nhiều cấp.
CREATE TABLE categories (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id   BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL UNIQUE,
    description TEXT,
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_categories_parent ON categories(parent_id);

-- =====================================================================
--  PRODUCTS & VARIANTS
-- =====================================================================
-- @products: Mẫu mã sản phẩm chung (không có size/màu). Giá & tồn nằm ở product_variants.
CREATE TABLE products (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    brand_id        BIGINT REFERENCES brands(id) ON DELETE SET NULL,
    category_id     BIGINT REFERENCES categories(id) ON DELETE SET NULL, -- danh mục CHÍNH
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(220) NOT NULL UNIQUE,
    description     TEXT,
    gender          gender_target NOT NULL DEFAULT 'unisex',
    base_price      NUMERIC(12,2) NOT NULL CHECK (base_price >= 0),
    is_published    BOOLEAN NOT NULL DEFAULT TRUE,
    rating_avg      NUMERIC(3,2) NOT NULL DEFAULT 0,
    rating_count    INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_products_category  ON products(category_id);
CREATE INDEX idx_products_brand     ON products(brand_id);
CREATE INDEX idx_products_published ON products(is_published);

-- (HYBRID) 1 sản phẩm thuộc NHIỀU danh mục phụ (sale, bộ sưu tập, hàng mới...)
-- products.category_id vẫn là danh mục chính; bảng này là các danh mục thêm.
-- @product_categories: Bảng nối nhiều-nhiều cho danh mục PHỤ (sale, bộ sưu tập...); danh mục chính vẫn ở products.category_id.
CREATE TABLE product_categories (
    product_id  BIGINT NOT NULL REFERENCES products(id)   ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
);
CREATE INDEX idx_product_categories_cat ON product_categories(category_id);

-- @product_images: Nhiều ảnh cho 1 sản phẩm; lưu S3 key/URL chứ không lưu file. is_primary = ảnh đại diện.
CREATE TABLE product_images (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url   VARCHAR(500) NOT NULL,    -- nên lưu S3 key, vd: products/123/main.jpg
    alt_text    VARCHAR(200),
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_product_images_product ON product_images(product_id);

-- Biến thể: mỗi dòng = 1 SKU bán được, có tồn kho riêng
-- @product_variants: Từng tổ hợp size+màu = 1 SKU bán được, có giá và tồn kho riêng. Đây là thứ thực sự bán ra.
CREATE TABLE product_variants (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku             VARCHAR(60) NOT NULL UNIQUE,
    size            VARCHAR(20),
    color           VARCHAR(40),
    price           NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    stock_quantity  INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    image_url       VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (product_id, size, color)
);
CREATE INDEX idx_variants_product ON product_variants(product_id);

-- =====================================================================
--  CART / GIỎ HÀNG (hỗ trợ khách vãng lai - guest cart)
--  Một giỏ hàng thuộc về user đã login HOẶC một session khách.
-- =====================================================================
-- @carts: Giỏ hàng, thuộc về user đã login (user_id) HOẶC khách vãng lai (session_token).
CREATE TABLE carts (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE, -- NULL nếu khách
    session_token VARCHAR(100) UNIQUE,    -- token khách vãng lai (cookie/localStorage)
    expires_at    TIMESTAMPTZ,            -- để dọn giỏ hàng khách bỏ quên (tùy chọn)
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Bắt buộc phải có ít nhất user_id HOẶC session_token
    CONSTRAINT chk_cart_owner CHECK (user_id IS NOT NULL OR session_token IS NOT NULL)
);
CREATE INDEX idx_carts_session ON carts(session_token);
-- Luồng: khách chưa login dùng session_token; khi login thì gán user_id
-- vào cart đó (hoặc merge với cart sẵn có), rồi xóa session_token.

-- @cart_items: Từng dòng trong giỏ (1 variant + số lượng). Unique theo (cart, variant) để không trùng dòng.
CREATE TABLE cart_items (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id     BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id  BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity    INT NOT NULL CHECK (quantity > 0),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (cart_id, variant_id)
);

-- =====================================================================
--  COUPONS / MÃ GIẢM GIÁ
-- =====================================================================
-- @coupons: Mã giảm giá (theo % hoặc số tiền cố định), có giới hạn tổng lượt và lượt/user.
CREATE TABLE coupons (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                 VARCHAR(50) NOT NULL UNIQUE,
    description          VARCHAR(255),
    discount_type        discount_type NOT NULL,
    discount_value       NUMERIC(12,2) NOT NULL CHECK (discount_value > 0),
    min_order_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    max_discount_amount  NUMERIC(12,2),       -- trần giảm khi dùng %
    usage_limit          INT,                 -- tổng số lần dùng (NULL = vô hạn)
    usage_limit_per_user INT DEFAULT 1,       -- mỗi user dùng tối đa (NULL = vô hạn)
    used_count           INT NOT NULL DEFAULT 0,
    valid_from           TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_until          TIMESTAMPTZ,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE
);

-- Giới hạn mã chỉ áp cho 1 số SẢN PHẨM cụ thể.
-- QUY ƯỚC: nếu coupon KHÔNG có dòng nào ở đây (và coupon_categories) -> áp cho cả đơn.
-- @coupon_products: Giới hạn mã chỉ áp cho 1 số sản phẩm; rỗng = áp cả đơn.
CREATE TABLE coupon_products (
    coupon_id   BIGINT NOT NULL REFERENCES coupons(id)  ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (coupon_id, product_id)
);

-- Giới hạn mã chỉ áp cho 1 số DANH MỤC cụ thể.
-- @coupon_categories: Giới hạn mã chỉ áp cho 1 số danh mục; rỗng = áp cả đơn.
CREATE TABLE coupon_categories (
    coupon_id   BIGINT NOT NULL REFERENCES coupons(id)    ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (coupon_id, category_id)
);

-- =====================================================================
--  ORDERS / ĐƠN HÀNG
-- =====================================================================
-- @orders: Đơn hàng. Lưu snapshot người nhận + tiền; cho phép khách không login đặt (user_id nullable).
CREATE TABLE orders (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_code          VARCHAR(30) NOT NULL UNIQUE DEFAULT ('OD' || to_char(now(),'YYMMDD') || lpad(floor(random()*100000)::text,5,'0')),
    user_id             BIGINT REFERENCES users(id) ON DELETE SET NULL,
    coupon_id           BIGINT REFERENCES coupons(id) ON DELETE SET NULL,

    recipient_name      VARCHAR(150) NOT NULL,
    recipient_phone     VARCHAR(20)  NOT NULL,
    shipping_address    VARCHAR(500) NOT NULL,

    subtotal            NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    shipping_fee        NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_amount        NUMERIC(12,2) NOT NULL DEFAULT 0,

    status              order_status   NOT NULL DEFAULT 'pending',
    payment_status      payment_status NOT NULL DEFAULT 'unpaid',
    payment_method      payment_method NOT NULL DEFAULT 'cod',
    note                TEXT,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_user    ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at);

-- Chi tiết đơn: snapshot tên + giá tại thời điểm mua
-- @order_items: Chi tiết đơn, lưu SNAPSHOT tên+giá lúc mua để đơn cũ không đổi khi giá SP thay đổi.
CREATE TABLE order_items (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    variant_id      BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    product_name    VARCHAR(200) NOT NULL,
    variant_label   VARCHAR(80),
    sku             VARCHAR(60),
    unit_price      NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    quantity        INT NOT NULL CHECK (quantity > 0),
    line_total      NUMERIC(12,2) NOT NULL
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

-- LỊCH SỬ TRẠNG THÁI ĐƠN HÀNG
-- Mỗi lần đơn đổi trạng thái thì thêm 1 dòng. Giúp tra "đơn đang ở đâu".
-- @order_status_history: Lịch sử đổi trạng thái đơn (mỗi lần đổi thêm 1 dòng) để tra cứu/đối soát.
CREATE TABLE order_status_history (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    old_status  order_status,             -- NULL ở lần đầu tạo đơn
    new_status  order_status NOT NULL,
    changed_by  BIGINT REFERENCES users(id) ON DELETE SET NULL, -- admin thao tác (NULL = hệ thống)
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_order_status_history_order ON order_status_history(order_id);

-- Lịch sử giao dịch thanh toán
-- @payment_transactions: Lịch sử giao dịch thanh toán của đơn; gateway_response lưu raw JSON từ MoMo/VNPay.
CREATE TABLE payment_transactions (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    method              payment_method NOT NULL,
    amount              NUMERIC(12,2) NOT NULL,
    transaction_ref     VARCHAR(100),
    status              payment_status NOT NULL DEFAULT 'unpaid',
    gateway_response    JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payment_tx_order ON payment_transactions(order_id);

-- =====================================================================
--  SHIPMENTS / VẬN CHUYỂN
--  Tách thông tin giao hàng ra khỏi orders. 1 đơn có thể có nhiều kiện.
-- =====================================================================
-- @shipments: Thông tin vận chuyển của đơn (đơn vị giao, mã vận đơn, trạng thái). Hợp khi nối GHN/GHTK.
CREATE TABLE shipments (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    carrier             VARCHAR(80),       -- GHN, GHTK, Viettel Post, J&T...
    tracking_number     VARCHAR(100),      -- mã vận đơn
    status              shipment_status NOT NULL DEFAULT 'pending',
    shipping_fee        NUMERIC(12,2) NOT NULL DEFAULT 0,
    estimated_delivery  DATE,
    shipped_at          TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_shipments_order    ON shipments(order_id);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_number);

-- =====================================================================
--  INVENTORY MOVEMENTS / SỔ KHO
--  Ghi mọi thay đổi tồn kho (+/-). stock_quantity ở variant là số HIỆN TẠI;
--  bảng này là LỊCH SỬ để truy "vì sao tồn kho lệch".
-- =====================================================================
-- @inventory_movements: Sổ kho ghi mọi thay đổi tồn (+/-). stock_quantity là số hiện tại, bảng này là lịch sử.
CREATE TABLE inventory_movements (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    variant_id  BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    change_qty  INT NOT NULL,             -- + nhập/hoàn, - bán (KHÔNG được 0)
    reason      inventory_reason NOT NULL,
    order_id    BIGINT REFERENCES orders(id) ON DELETE SET NULL, -- nếu phát sinh từ đơn
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_change_qty_nonzero CHECK (change_qty <> 0)
);
CREATE INDEX idx_inventory_movements_variant ON inventory_movements(variant_id);

-- =====================================================================
--  COUPON USAGES / LỊCH SỬ DÙNG MÃ (để chặn dùng quá giới hạn theo user)
-- =====================================================================
-- @coupon_usages: Ghi lại ai-dùng-mã-nào-ở-đơn-nào, dùng để đếm và chặn vượt giới hạn lượt/user.
CREATE TABLE coupon_usages (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    coupon_id   BIGINT NOT NULL REFERENCES coupons(id)  ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    order_id    BIGINT NOT NULL REFERENCES orders(id)   ON DELETE CASCADE,
    used_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_coupon_usages_coupon_user ON coupon_usages(coupon_id, user_id);

-- =====================================================================
--  REVIEWS / ĐÁNH GIÁ
-- =====================================================================
-- @reviews: Đánh giá sản phẩm (1-5 sao). Mỗi user chỉ review 1 lần/SP; order_id để xác thực "đã mua".
CREATE TABLE reviews (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id    BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (product_id, user_id)
);
CREATE INDEX idx_reviews_product ON reviews(product_id);

-- =====================================================================
--  WISHLIST / DANH SÁCH YÊU THÍCH
-- =====================================================================
-- @wishlists: Danh sách sản phẩm yêu thích của user; unique (user, product) để không lưu trùng.
CREATE TABLE wishlists (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);

-- =====================================================================
--  BODY SIZE & AI SIZE RECOMMENDATION
-- =====================================================================

-- @user_measurements: Số đo cơ thể của user, dùng làm đầu vào cho AI gợi ý size. Mỗi user 1 dòng (cập nhật đè).
CREATE TABLE user_measurements (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    height_cm       NUMERIC(5,1) CHECK (height_cm > 0),  -- chiều cao (cm)
    weight_kg       NUMERIC(5,1) CHECK (weight_kg > 0),  -- cân nặng (kg)
    chest_cm        NUMERIC(5,1),        -- vòng ngực
    waist_cm        NUMERIC(5,1),        -- vòng eo
    hip_cm          NUMERIC(5,1),        -- vòng hông
    shoulder_cm     NUMERIC(5,1),        -- vai
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- @ai_size_recommendations: Cache kết quả AI gợi ý size cho (user, product) để khỏi gọi API lại.
CREATE TABLE ai_size_recommendations (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id       BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    recommended_size VARCHAR(20) NOT NULL,                   -- 'M', 'L', '42'...
    confidence       NUMERIC(4,3) CHECK (confidence BETWEEN 0 AND 1), -- 0.000 → 1.000
    reasoning        TEXT,                                   -- giải thích của AI
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);
CREATE INDEX idx_ai_size_user ON ai_size_recommendations(user_id);
-- LƯU Ý: gợi ý phụ thuộc số đo. Khi user cập nhật user_measurements, nên XÓA
-- các dòng cache ở đây để lần sau tính lại (hoặc kèm cột tham chiếu version số đo).

-- =====================================================================
--  TRIGGER: tự động cập nhật updated_at
-- =====================================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_products_updated BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_orders_updated   BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_carts_updated    BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_measurements_updated BEFORE UPDATE ON user_measurements
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Khi số đo của user thay đổi -> xóa cache gợi ý size của user đó
-- để lần sau hệ thống tính lại theo số đo mới (tránh gợi ý "kẹt" theo số đo cũ).
CREATE OR REPLACE FUNCTION clear_size_recommendation_cache()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM ai_size_recommendations WHERE user_id = NEW.user_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_clear_size_cache
    AFTER INSERT OR UPDATE OF height_cm, weight_kg, chest_cm, waist_cm, hip_cm, shoulder_cm
    ON user_measurements
    FOR EACH ROW EXECUTE FUNCTION clear_size_recommendation_cache();

COMMIT;

-- =====================================================================
--  SEED DATA - Dữ liệu mẫu
-- =====================================================================
BEGIN;

INSERT INTO brands (name, slug) VALUES
    ('Nike', 'nike'), ('Adidas', 'adidas'), ('Local Brand VN', 'local-brand-vn');

INSERT INTO categories (parent_id, name, slug) VALUES
    (NULL, 'Thời trang Nam', 'thoi-trang-nam'),
    (NULL, 'Thời trang Nữ', 'thoi-trang-nu'),
    (NULL, 'Giày dép', 'giay-dep'),
    (NULL, 'Hàng mới về', 'hang-moi-ve'),
    (NULL, 'Sale', 'sale');
INSERT INTO categories (parent_id, name, slug) VALUES
    (1, 'Áo thun Nam', 'ao-thun-nam'),
    (3, 'Giày sneaker', 'giay-sneaker');

INSERT INTO products (brand_id, category_id, name, slug, description, gender, base_price)
VALUES
    (3, 6, 'Áo thun Basic Cotton', 'ao-thun-basic-cotton',
     'Áo thun cotton 100%, form regular fit', 'unisex', 199000),
    (1, 7, 'Nike Air Force 1', 'nike-air-force-1',
     'Giày sneaker cổ điển màu trắng', 'unisex', 2890000);

-- 1 SP thuộc nhiều danh mục: áo thun cũng nằm trong "Hàng mới về" và "Sale"
INSERT INTO product_categories (product_id, category_id) VALUES
    (1, 4), (1, 5), (2, 4);

INSERT INTO product_variants (product_id, sku, size, color, price, stock_quantity) VALUES
    (1, 'AT-BASIC-M-DEN',   'M',  'Đen',   199000, 50),
    (1, 'AT-BASIC-L-DEN',   'L',  'Đen',   199000, 30),
    (1, 'AT-BASIC-M-TRANG', 'M',  'Trắng', 199000, 40),
    (1, 'AT-BASIC-L-TRANG', 'L',  'Trắng', 199000, 25);
INSERT INTO product_variants (product_id, sku, size, color, price, stock_quantity) VALUES
    (2, 'NK-AF1-40-TRANG', '40', 'Trắng', 2890000, 10),
    (2, 'NK-AF1-41-TRANG', '41', 'Trắng', 2890000, 8),
    (2, 'NK-AF1-42-TRANG', '42', 'Trắng', 2890000, 5);

INSERT INTO coupons (code, description, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit_per_user, valid_until)
VALUES
    ('WELCOME10', 'Giảm 10% cho đơn đầu tiên', 'percentage', 10, 200000, 100000, 1, now() + interval '30 days'),
    ('FREESHIP50', 'Giảm 50k phí ship', 'fixed_amount', 50000, 500000, NULL, 3, now() + interval '30 days');

COMMIT;

-- =====================================================================
--  VÍ DỤ TRUY VẤN
-- =====================================================================

-- 1) Tất cả sản phẩm trong danh mục "Sale" (gồm cả danh mục chính lẫn phụ)
-- SELECT DISTINCT p.id, p.name
-- FROM products p
-- LEFT JOIN product_categories pc ON pc.product_id = p.id
-- WHERE p.category_id = (SELECT id FROM categories WHERE slug='sale')
--    OR pc.category_id = (SELECT id FROM categories WHERE slug='sale');

-- 2) Kiểm tra user còn được dùng mã không (trước khi áp)
-- SELECT (count(*) < c.usage_limit_per_user) AS con_duoc_dung
-- FROM coupons c
-- LEFT JOIN coupon_usages cu ON cu.coupon_id = c.id AND cu.user_id = :user_id
-- WHERE c.code = :code
-- GROUP BY c.usage_limit_per_user;

-- 3) Lịch sử trạng thái của 1 đơn
-- SELECT old_status, new_status, created_at
-- FROM order_status_history WHERE order_id = :order_id ORDER BY created_at;

-- 4) Đối chiếu tồn kho từ sổ kho (tổng phải khớp stock_quantity)
-- SELECT v.sku, v.stock_quantity, COALESCE(SUM(im.change_qty),0) AS tong_so_kho
-- FROM product_variants v
-- LEFT JOIN inventory_movements im ON im.variant_id = v.id
-- GROUP BY v.sku, v.stock_quantity;
