-- =====================================================================
--  FASHION SHOP - PostgreSQL Database Schema (FULL / v2)
--  Web bÃ¡n hÃ ng thá»i trang / giÃ y dÃ©p
--  PostgreSQL 14+
-- =====================================================================
--  ÄÃ£ gá»™p Ä‘áº§y Ä‘á»§:
--   - Schema gá»‘c (catalog, variant, cart, order, payment, review...)
--   - Guest cart (giá» hÃ ng khÃ´ng cáº§n login)
--   - Giá»›i háº¡n mÃ£ giáº£m giÃ¡ theo tá»«ng user
--   - order_status_history  (lá»‹ch sá»­ tráº¡ng thÃ¡i Ä‘Æ¡n)
--   - inventory_movements   (sá»• kho ghi +/- tá»“n)
--   - coupon_products / coupon_categories (mÃ£ giáº£m theo SP/danh má»¥c)
--   - shipments             (thÃ´ng tin váº­n chuyá»ƒn)
--   - product_categories    (1 SP thuá»™c nhiá»u danh má»¥c - hybrid)
-- =====================================================================


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

-- LÃ½ do thay Ä‘á»•i tá»“n kho (sá»• kho)
CREATE TYPE inventory_reason AS ENUM (
    'purchase',     -- nháº­p hÃ ng (+)
    'sale',         -- bÃ¡n ra    (-)
    'return',       -- khÃ¡ch tráº£ (+)
    'cancellation', -- há»§y Ä‘Æ¡n, hoÃ n kho (+)
    'adjustment'    -- kiá»ƒm kÃª / chá»‰nh tay (+/-)
);

-- Tráº¡ng thÃ¡i váº­n chuyá»ƒn
CREATE TYPE shipment_status AS ENUM (
    'pending', 'picked_up', 'in_transit',
    'delivered', 'failed', 'returned'
);

-- =====================================================================
--  USERS / KHÃCH HÃ€NG
-- =====================================================================
-- @users: TÃ i khoáº£n (cáº£ khÃ¡ch láº«n admin), phÃ¢n biá»‡t báº±ng cá» is_admin.
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

-- @addresses: Sá»• Ä‘á»‹a chá»‰ giao hÃ ng cá»§a user (theo Tá»‰nh/Quáº­n/PhÆ°á»ng VN), 1 user nhiá»u Ä‘á»‹a chá»‰.
CREATE TABLE addresses (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_name  VARCHAR(150) NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    province        VARCHAR(100) NOT NULL,   -- Tá»‰nh/ThÃ nh phá»‘
    district        VARCHAR(100) NOT NULL,   -- Quáº­n/Huyá»‡n
    ward            VARCHAR(100),            -- PhÆ°á»ng/XÃ£
    street_address  VARCHAR(255) NOT NULL,   -- Sá»‘ nhÃ , tÃªn Ä‘Æ°á»ng
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_addresses_user ON addresses(user_id);

-- =====================================================================
--  CATALOG: BRAND, CATEGORY
-- =====================================================================
-- @brands: ThÆ°Æ¡ng hiá»‡u sáº£n pháº©m (Nike, Adidas...).
CREATE TABLE brands (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    slug        VARCHAR(140) NOT NULL UNIQUE,
    logo_url    VARCHAR(500),
    description TEXT
);

-- Danh má»¥c dáº¡ng cÃ¢y (self-reference)
-- @categories: Danh má»¥c dáº¡ng cÃ¢y (parent_id tá»± trá» vá» chÃ­nh nÃ³) Ä‘á»ƒ lá»“ng nhiá»u cáº¥p.
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
-- @products: Máº«u mÃ£ sáº£n pháº©m chung (khÃ´ng cÃ³ size/mÃ u). GiÃ¡ & tá»“n náº±m á»Ÿ product_variants.
CREATE TABLE products (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    brand_id        BIGINT REFERENCES brands(id) ON DELETE SET NULL,
    category_id     BIGINT REFERENCES categories(id) ON DELETE SET NULL, -- danh má»¥c CHÃNH
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

-- (HYBRID) 1 sáº£n pháº©m thuá»™c NHIá»€U danh má»¥c phá»¥ (sale, bá»™ sÆ°u táº­p, hÃ ng má»›i...)
-- products.category_id váº«n lÃ  danh má»¥c chÃ­nh; báº£ng nÃ y lÃ  cÃ¡c danh má»¥c thÃªm.
-- @product_categories: Báº£ng ná»‘i nhiá»u-nhiá»u cho danh má»¥c PHá»¤ (sale, bá»™ sÆ°u táº­p...); danh má»¥c chÃ­nh váº«n á»Ÿ products.category_id.
CREATE TABLE product_categories (
    product_id  BIGINT NOT NULL REFERENCES products(id)   ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
);
CREATE INDEX idx_product_categories_cat ON product_categories(category_id);

-- @product_images: Nhiá»u áº£nh cho 1 sáº£n pháº©m; lÆ°u S3 key/URL chá»© khÃ´ng lÆ°u file. is_primary = áº£nh Ä‘áº¡i diá»‡n.
CREATE TABLE product_images (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url   VARCHAR(500) NOT NULL,    -- nÃªn lÆ°u S3 key, vd: products/123/main.jpg
    alt_text    VARCHAR(200),
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_product_images_product ON product_images(product_id);

-- Biáº¿n thá»ƒ: má»—i dÃ²ng = 1 SKU bÃ¡n Ä‘Æ°á»£c, cÃ³ tá»“n kho riÃªng
-- @product_variants: Tá»«ng tá»• há»£p size+mÃ u = 1 SKU bÃ¡n Ä‘Æ°á»£c, cÃ³ giÃ¡ vÃ  tá»“n kho riÃªng. ÄÃ¢y lÃ  thá»© thá»±c sá»± bÃ¡n ra.
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
--  CART / GIá»Ž HÃ€NG (há»— trá»£ khÃ¡ch vÃ£ng lai - guest cart)
--  Má»™t giá» hÃ ng thuá»™c vá» user Ä‘Ã£ login HOáº¶C má»™t session khÃ¡ch.
-- =====================================================================
-- @carts: Giá» hÃ ng, thuá»™c vá» user Ä‘Ã£ login (user_id) HOáº¶C khÃ¡ch vÃ£ng lai (session_token).
CREATE TABLE carts (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT UNIQUE REFERENCES users(id) ON DELETE CASCADE, -- NULL náº¿u khÃ¡ch
    session_token VARCHAR(100) UNIQUE,    -- token khÃ¡ch vÃ£ng lai (cookie/localStorage)
    expires_at    TIMESTAMPTZ,            -- Ä‘á»ƒ dá»n giá» hÃ ng khÃ¡ch bá» quÃªn (tÃ¹y chá»n)
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Báº¯t buá»™c pháº£i cÃ³ Ã­t nháº¥t user_id HOáº¶C session_token
    CONSTRAINT chk_cart_owner CHECK (user_id IS NOT NULL OR session_token IS NOT NULL)
);
CREATE INDEX idx_carts_session ON carts(session_token);
-- Luá»“ng: khÃ¡ch chÆ°a login dÃ¹ng session_token; khi login thÃ¬ gÃ¡n user_id
-- vÃ o cart Ä‘Ã³ (hoáº·c merge vá»›i cart sáºµn cÃ³), rá»“i xÃ³a session_token.

-- @cart_items: Tá»«ng dÃ²ng trong giá» (1 variant + sá»‘ lÆ°á»£ng). Unique theo (cart, variant) Ä‘á»ƒ khÃ´ng trÃ¹ng dÃ²ng.
CREATE TABLE cart_items (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id     BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id  BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity    INT NOT NULL CHECK (quantity > 0),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (cart_id, variant_id)
);

-- =====================================================================
--  COUPONS / MÃƒ GIáº¢M GIÃ
-- =====================================================================
-- @coupons: MÃ£ giáº£m giÃ¡ (theo % hoáº·c sá»‘ tiá»n cá»‘ Ä‘á»‹nh), cÃ³ giá»›i háº¡n tá»•ng lÆ°á»£t vÃ  lÆ°á»£t/user.
CREATE TABLE coupons (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                 VARCHAR(50) NOT NULL UNIQUE,
    description          VARCHAR(255),
    discount_type        discount_type NOT NULL,
    discount_value       NUMERIC(12,2) NOT NULL CHECK (discount_value > 0),
    min_order_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    max_discount_amount  NUMERIC(12,2),       -- tráº§n giáº£m khi dÃ¹ng %
    usage_limit          INT,                 -- tá»•ng sá»‘ láº§n dÃ¹ng (NULL = vÃ´ háº¡n)
    usage_limit_per_user INT DEFAULT 1,       -- má»—i user dÃ¹ng tá»‘i Ä‘a (NULL = vÃ´ háº¡n)
    used_count           INT NOT NULL DEFAULT 0,
    valid_from           TIMESTAMPTZ NOT NULL DEFAULT now(),
    valid_until          TIMESTAMPTZ,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE
);

-- Giá»›i háº¡n mÃ£ chá»‰ Ã¡p cho 1 sá»‘ Sáº¢N PHáº¨M cá»¥ thá»ƒ.
-- QUY Æ¯á»šC: náº¿u coupon KHÃ”NG cÃ³ dÃ²ng nÃ o á»Ÿ Ä‘Ã¢y (vÃ  coupon_categories) -> Ã¡p cho cáº£ Ä‘Æ¡n.
-- @coupon_products: Giá»›i háº¡n mÃ£ chá»‰ Ã¡p cho 1 sá»‘ sáº£n pháº©m; rá»—ng = Ã¡p cáº£ Ä‘Æ¡n.
CREATE TABLE coupon_products (
    coupon_id   BIGINT NOT NULL REFERENCES coupons(id)  ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (coupon_id, product_id)
);

-- Giá»›i háº¡n mÃ£ chá»‰ Ã¡p cho 1 sá»‘ DANH Má»¤C cá»¥ thá»ƒ.
-- @coupon_categories: Giá»›i háº¡n mÃ£ chá»‰ Ã¡p cho 1 sá»‘ danh má»¥c; rá»—ng = Ã¡p cáº£ Ä‘Æ¡n.
CREATE TABLE coupon_categories (
    coupon_id   BIGINT NOT NULL REFERENCES coupons(id)    ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (coupon_id, category_id)
);

-- =====================================================================
--  ORDERS / ÄÆ N HÃ€NG
-- =====================================================================
-- @orders: ÄÆ¡n hÃ ng. LÆ°u snapshot ngÆ°á»i nháº­n + tiá»n; cho phÃ©p khÃ¡ch khÃ´ng login Ä‘áº·t (user_id nullable).
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

-- Chi tiáº¿t Ä‘Æ¡n: snapshot tÃªn + giÃ¡ táº¡i thá»i Ä‘iá»ƒm mua
-- @order_items: Chi tiáº¿t Ä‘Æ¡n, lÆ°u SNAPSHOT tÃªn+giÃ¡ lÃºc mua Ä‘á»ƒ Ä‘Æ¡n cÅ© khÃ´ng Ä‘á»•i khi giÃ¡ SP thay Ä‘á»•i.
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

-- Lá»ŠCH Sá»¬ TRáº NG THÃI ÄÆ N HÃ€NG
-- Má»—i láº§n Ä‘Æ¡n Ä‘á»•i tráº¡ng thÃ¡i thÃ¬ thÃªm 1 dÃ²ng. GiÃºp tra "Ä‘Æ¡n Ä‘ang á»Ÿ Ä‘Ã¢u".
-- @order_status_history: Lá»‹ch sá»­ Ä‘á»•i tráº¡ng thÃ¡i Ä‘Æ¡n (má»—i láº§n Ä‘á»•i thÃªm 1 dÃ²ng) Ä‘á»ƒ tra cá»©u/Ä‘á»‘i soÃ¡t.
CREATE TABLE order_status_history (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    old_status  order_status,             -- NULL á»Ÿ láº§n Ä‘áº§u táº¡o Ä‘Æ¡n
    new_status  order_status NOT NULL,
    changed_by  BIGINT REFERENCES users(id) ON DELETE SET NULL, -- admin thao tÃ¡c (NULL = há»‡ thá»‘ng)
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_order_status_history_order ON order_status_history(order_id);

-- Lá»‹ch sá»­ giao dá»‹ch thanh toÃ¡n
-- @payment_transactions: Lá»‹ch sá»­ giao dá»‹ch thanh toÃ¡n cá»§a Ä‘Æ¡n; gateway_response lÆ°u raw JSON tá»« MoMo/VNPay.
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
--  SHIPMENTS / Váº¬N CHUYá»‚N
--  TÃ¡ch thÃ´ng tin giao hÃ ng ra khá»i orders. 1 Ä‘Æ¡n cÃ³ thá»ƒ cÃ³ nhiá»u kiá»‡n.
-- =====================================================================
-- @shipments: ThÃ´ng tin váº­n chuyá»ƒn cá»§a Ä‘Æ¡n (Ä‘Æ¡n vá»‹ giao, mÃ£ váº­n Ä‘Æ¡n, tráº¡ng thÃ¡i). Há»£p khi ná»‘i GHN/GHTK.
CREATE TABLE shipments (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    carrier             VARCHAR(80),       -- GHN, GHTK, Viettel Post, J&T...
    tracking_number     VARCHAR(100),      -- mÃ£ váº­n Ä‘Æ¡n
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
--  INVENTORY MOVEMENTS / Sá»” KHO
--  Ghi má»i thay Ä‘á»•i tá»“n kho (+/-). stock_quantity á»Ÿ variant lÃ  sá»‘ HIá»†N Táº I;
--  báº£ng nÃ y lÃ  Lá»ŠCH Sá»¬ Ä‘á»ƒ truy "vÃ¬ sao tá»“n kho lá»‡ch".
-- =====================================================================
-- @inventory_movements: Sá»• kho ghi má»i thay Ä‘á»•i tá»“n (+/-). stock_quantity lÃ  sá»‘ hiá»‡n táº¡i, báº£ng nÃ y lÃ  lá»‹ch sá»­.
CREATE TABLE inventory_movements (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    variant_id  BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    change_qty  INT NOT NULL,             -- + nháº­p/hoÃ n, - bÃ¡n (KHÃ”NG Ä‘Æ°á»£c 0)
    reason      inventory_reason NOT NULL,
    order_id    BIGINT REFERENCES orders(id) ON DELETE SET NULL, -- náº¿u phÃ¡t sinh tá»« Ä‘Æ¡n
    note        TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_change_qty_nonzero CHECK (change_qty <> 0)
);
CREATE INDEX idx_inventory_movements_variant ON inventory_movements(variant_id);

-- =====================================================================
--  COUPON USAGES / Lá»ŠCH Sá»¬ DÃ™NG MÃƒ (Ä‘á»ƒ cháº·n dÃ¹ng quÃ¡ giá»›i háº¡n theo user)
-- =====================================================================
-- @coupon_usages: Ghi láº¡i ai-dÃ¹ng-mÃ£-nÃ o-á»Ÿ-Ä‘Æ¡n-nÃ o, dÃ¹ng Ä‘á»ƒ Ä‘áº¿m vÃ  cháº·n vÆ°á»£t giá»›i háº¡n lÆ°á»£t/user.
CREATE TABLE coupon_usages (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    coupon_id   BIGINT NOT NULL REFERENCES coupons(id)  ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    order_id    BIGINT NOT NULL REFERENCES orders(id)   ON DELETE CASCADE,
    used_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_coupon_usages_coupon_user ON coupon_usages(coupon_id, user_id);

-- =====================================================================
--  REVIEWS / ÄÃNH GIÃ
-- =====================================================================
-- @reviews: ÄÃ¡nh giÃ¡ sáº£n pháº©m (1-5 sao). Má»—i user chá»‰ review 1 láº§n/SP; order_id Ä‘á»ƒ xÃ¡c thá»±c "Ä‘Ã£ mua".
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
--  WISHLIST / DANH SÃCH YÃŠU THÃCH
-- =====================================================================
-- @wishlists: Danh sÃ¡ch sáº£n pháº©m yÃªu thÃ­ch cá»§a user; unique (user, product) Ä‘á»ƒ khÃ´ng lÆ°u trÃ¹ng.
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

-- @user_measurements: Sá»‘ Ä‘o cÆ¡ thá»ƒ cá»§a user, dÃ¹ng lÃ m Ä‘áº§u vÃ o cho AI gá»£i Ã½ size. Má»—i user 1 dÃ²ng (cáº­p nháº­t Ä‘Ã¨).
CREATE TABLE user_measurements (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    height_cm       NUMERIC(5,1) CHECK (height_cm > 0),  -- chiá»u cao (cm)
    weight_kg       NUMERIC(5,1) CHECK (weight_kg > 0),  -- cÃ¢n náº·ng (kg)
    chest_cm        NUMERIC(5,1),        -- vÃ²ng ngá»±c
    waist_cm        NUMERIC(5,1),        -- vÃ²ng eo
    hip_cm          NUMERIC(5,1),        -- vÃ²ng hÃ´ng
    shoulder_cm     NUMERIC(5,1),        -- vai
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- @ai_size_recommendations: Cache káº¿t quáº£ AI gá»£i Ã½ size cho (user, product) Ä‘á»ƒ khá»i gá»i API láº¡i.
CREATE TABLE ai_size_recommendations (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id       BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    recommended_size VARCHAR(20) NOT NULL,                   -- 'M', 'L', '42'...
    confidence       NUMERIC(4,3) CHECK (confidence BETWEEN 0 AND 1), -- 0.000 â†’ 1.000
    reasoning        TEXT,                                   -- giáº£i thÃ­ch cá»§a AI
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);
CREATE INDEX idx_ai_size_user ON ai_size_recommendations(user_id);
-- LÆ¯U Ã: gá»£i Ã½ phá»¥ thuá»™c sá»‘ Ä‘o. Khi user cáº­p nháº­t user_measurements, nÃªn XÃ“A
-- cÃ¡c dÃ²ng cache á»Ÿ Ä‘Ã¢y Ä‘á»ƒ láº§n sau tÃ­nh láº¡i (hoáº·c kÃ¨m cá»™t tham chiáº¿u version sá»‘ Ä‘o).

-- =====================================================================
--  TRIGGER: tá»± Ä‘á»™ng cáº­p nháº­t updated_at
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

-- Khi sá»‘ Ä‘o cá»§a user thay Ä‘á»•i -> xÃ³a cache gá»£i Ã½ size cá»§a user Ä‘Ã³
-- Ä‘á»ƒ láº§n sau há»‡ thá»‘ng tÃ­nh láº¡i theo sá»‘ Ä‘o má»›i (trÃ¡nh gá»£i Ã½ "káº¹t" theo sá»‘ Ä‘o cÅ©).
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


-- =====================================================================
--  SEED DATA - Dá»¯ liá»‡u máº«u
-- =====================================================================

INSERT INTO brands (name, slug) VALUES
    ('Nike', 'nike'), ('Adidas', 'adidas'), ('Local Brand VN', 'local-brand-vn');

INSERT INTO categories (parent_id, name, slug) VALUES
    (NULL, 'Thá»i trang Nam', 'thoi-trang-nam'),
    (NULL, 'Thá»i trang Ná»¯', 'thoi-trang-nu'),
    (NULL, 'GiÃ y dÃ©p', 'giay-dep'),
    (NULL, 'HÃ ng má»›i vá»', 'hang-moi-ve'),
    (NULL, 'Sale', 'sale');
INSERT INTO categories (parent_id, name, slug) VALUES
    (1, 'Ão thun Nam', 'ao-thun-nam'),
    (3, 'GiÃ y sneaker', 'giay-sneaker');

INSERT INTO products (brand_id, category_id, name, slug, description, gender, base_price)
VALUES
    (3, 6, 'Ão thun Basic Cotton', 'ao-thun-basic-cotton',
     'Ão thun cotton 100%, form regular fit', 'unisex', 199000),
    (1, 7, 'Nike Air Force 1', 'nike-air-force-1',
     'GiÃ y sneaker cá»• Ä‘iá»ƒn mÃ u tráº¯ng', 'unisex', 2890000);

-- 1 SP thuá»™c nhiá»u danh má»¥c: Ã¡o thun cÅ©ng náº±m trong "HÃ ng má»›i vá»" vÃ  "Sale"
INSERT INTO product_categories (product_id, category_id) VALUES
    (1, 4), (1, 5), (2, 4);

INSERT INTO product_variants (product_id, sku, size, color, price, stock_quantity) VALUES
    (1, 'AT-BASIC-M-DEN',   'M',  'Äen',   199000, 50),
    (1, 'AT-BASIC-L-DEN',   'L',  'Äen',   199000, 30),
    (1, 'AT-BASIC-M-TRANG', 'M',  'Tráº¯ng', 199000, 40),
    (1, 'AT-BASIC-L-TRANG', 'L',  'Tráº¯ng', 199000, 25);
INSERT INTO product_variants (product_id, sku, size, color, price, stock_quantity) VALUES
    (2, 'NK-AF1-40-TRANG', '40', 'Tráº¯ng', 2890000, 10),
    (2, 'NK-AF1-41-TRANG', '41', 'Tráº¯ng', 2890000, 8),
    (2, 'NK-AF1-42-TRANG', '42', 'Tráº¯ng', 2890000, 5);

INSERT INTO coupons (code, description, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit_per_user, valid_until)
VALUES
    ('WELCOME10', 'Giáº£m 10% cho Ä‘Æ¡n Ä‘áº§u tiÃªn', 'percentage', 10, 200000, 100000, 1, now() + interval '30 days'),
    ('FREESHIP50', 'Giáº£m 50k phÃ­ ship', 'fixed_amount', 50000, 500000, NULL, 3, now() + interval '30 days');


-- =====================================================================
--  VÃ Dá»¤ TRUY Váº¤N
-- =====================================================================

-- 1) Táº¥t cáº£ sáº£n pháº©m trong danh má»¥c "Sale" (gá»“m cáº£ danh má»¥c chÃ­nh láº«n phá»¥)
-- SELECT DISTINCT p.id, p.name
-- FROM products p
-- LEFT JOIN product_categories pc ON pc.product_id = p.id
-- WHERE p.category_id = (SELECT id FROM categories WHERE slug='sale')
--    OR pc.category_id = (SELECT id FROM categories WHERE slug='sale');

-- 2) Kiá»ƒm tra user cÃ²n Ä‘Æ°á»£c dÃ¹ng mÃ£ khÃ´ng (trÆ°á»›c khi Ã¡p)
-- SELECT (count(*) < c.usage_limit_per_user) AS con_duoc_dung
-- FROM coupons c
-- LEFT JOIN coupon_usages cu ON cu.coupon_id = c.id AND cu.user_id = :user_id
-- WHERE c.code = :code
-- GROUP BY c.usage_limit_per_user;

-- 3) Lá»‹ch sá»­ tráº¡ng thÃ¡i cá»§a 1 Ä‘Æ¡n
-- SELECT old_status, new_status, created_at
-- FROM order_status_history WHERE order_id = :order_id ORDER BY created_at;

-- 4) Äá»‘i chiáº¿u tá»“n kho tá»« sá»• kho (tá»•ng pháº£i khá»›p stock_quantity)
-- SELECT v.sku, v.stock_quantity, COALESCE(SUM(im.change_qty),0) AS tong_so_kho
-- FROM product_variants v
-- LEFT JOIN inventory_movements im ON im.variant_id = v.id
-- GROUP BY v.sku, v.stock_quantity;
