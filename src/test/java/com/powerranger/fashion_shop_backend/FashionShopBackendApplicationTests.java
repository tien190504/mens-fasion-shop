package com.powerranger.fashion_shop_backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class FashionShopBackendApplicationTests {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fashion_shop_test")
            .withUsername("fashionshop")
            .withPassword("fashionshop");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.jwt.secret", () -> "test-secret-key-that-is-long-enough-for-hs256");
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void logoutRequiresAuthenticationAndReturnsSuccessForValidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());

        String token = loginAndReadToken("admin@shop.com", "admin123");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void authMeRequiresAuthenticationAndAllowsProfileUpdate() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        String token = registerAndReadToken("profile@example.com", "Profile User", "0903333333");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Profile User"))
                .andExpect(jsonPath("$.data.phone").value("0903333333"))
                .andExpect(jsonPath("$.data.admin").value(false));

        mockMvc.perform(patch("/api/v1/auth/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Updated Profile",
                                  "phone": "0903333344",
                                  "avatarUrl": "https://example.test/avatar.png",
                                  "dateOfBirth": "1999-12-31"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated"))
                .andExpect(jsonPath("$.data.fullName").value("Updated Profile"))
                .andExpect(jsonPath("$.data.phone").value("0903333344"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://example.test/avatar.png"))
                .andExpect(jsonPath("$.data.dateOfBirth").value("1999-12-31"));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Profile"))
                .andExpect(jsonPath("$.data.phone").value("0903333344"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://example.test/avatar.png"))
                .andExpect(jsonPath("$.data.dateOfBirth").value("1999-12-31"));
    }

    @Test
    void authMePasswordChangeRequiresCurrentPassword() throws Exception {
        String token = registerAndReadToken("password-change@example.com", "Password User", "0904444444");

        mockMvc.perform(patch("/api/v1/auth/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "wrong-password",
                                  "newPassword": "newsecret123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/auth/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "secret123",
                                  "newPassword": "newsecret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "password-change@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        loginAndReadToken("password-change@example.com", "newsecret123", false);
    }

    @Test
    void adminUserManagementEndpointsWork() throws Exception {
        String adminToken = loginAndReadToken("admin@shop.com", "admin123");
        String userToken = registerAndReadToken("admin-user-access@example.com", "Regular User", "0905555555");

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "managed-user@example.com",
                                  "password": "managed123",
                                  "fullName": "Managed User",
                                  "phone": "0987654321",
                                  "avatarUrl": "https://example.test/managed.png",
                                  "dateOfBirth": "2000-01-01",
                                  "active": true,
                                  "admin": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.data.email").value("managed-user@example.com"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.admin").value(false))
                .andReturn();
        long managedUserId = readLong(createResult, "$.data.id");

        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "managed-user@example.com",
                                  "password": "managed123",
                                  "phone": "0987654323"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "managed-user-copy@example.com",
                                  "password": "managed123",
                                  "phone": "0987654321"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(adminToken))
                        .param("keyword", "managed-user")
                        .param("active", "true")
                        .param("admin", "false")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value((int) managedUserId))
                .andExpect(jsonPath("$.data.content[0].email").value("managed-user@example.com"));

        mockMvc.perform(get("/api/v1/admin/users/{id}", managedUserId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("managed-user@example.com"))
                .andExpect(jsonPath("$.data.phone").value("0987654321"));

        mockMvc.perform(patch("/api/v1/admin/users/{id}", managedUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Managed Admin",
                                  "phone": "0987654322",
                                  "avatarUrl": "https://example.test/managed-admin.png",
                                  "dateOfBirth": "2001-02-03",
                                  "active": true,
                                  "admin": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated"))
                .andExpect(jsonPath("$.data.fullName").value("Managed Admin"))
                .andExpect(jsonPath("$.data.phone").value("0987654322"))
                .andExpect(jsonPath("$.data.admin").value(true));

        String promotedToken = loginAndReadToken("managed-user@example.com", "managed123", true);

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(promotedToken)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/admin/users/{id}", managedUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "admin": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.admin").value(false));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(promotedToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/admin/users/{id}/password", managedUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPassword": "reset1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "managed-user@example.com",
                                  "password": "managed123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        String resetToken = loginAndReadToken("managed-user@example.com", "reset1234", false);

        mockMvc.perform(patch("/api/v1/admin/users/{id}", managedUserId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", bearer(resetToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminUserManagementRejectsUnsafeAdminChanges() throws Exception {
        String adminToken = loginAndReadToken("admin@shop.com", "admin123");

        MvcResult meResult = mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn();
        long adminId = readLong(meResult, "$.data.id");

        mockMvc.perform(patch("/api/v1/admin/users/{id}", adminId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/admin/users/{id}", adminId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "admin": false
                                }
                                """))
                .andExpect(status().isBadRequest());

        MvcResult createAdminResult = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "second-admin@example.com",
                                  "password": "second123",
                                  "phone": "0987654330",
                                  "active": true,
                                  "admin": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.admin").value(true))
                .andReturn();
        long secondAdminId = readLong(createAdminResult, "$.data.id");

        mockMvc.perform(patch("/api/v1/admin/users/{id}", secondAdminId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "admin": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.admin").value(false));
    }

    @Test
    void implementedMvpApiFlowsWork() throws Exception {
        String adminToken = loginAndReadToken("admin@shop.com", "admin123");
        String userToken = registerAndReadToken("customer@example.com", "Customer User");

        mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Blocked Brand"
                                }
                                """))
                .andExpect(status().isForbidden());

        MvcResult brandResult = mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Power Denim",
                                  "logoUrl": "https://example.test/brand.png",
                                  "description": "Everyday menswear"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        long brandId = readLong(brandResult, "$.data.id");

        MvcResult categoryResult = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Shirts",
                                  "description": "Men shirts",
                                  "sortOrder": 1,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        long categoryId = readLong(categoryResult, "$.data.id");

        MvcResult productResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Oxford Shirt",
                                  "description": "Slim fit cotton shirt",
                                  "brandId": %d,
                                  "categoryId": %d,
                                  "basePrice": 450000,
                                  "gender": "men",
                                  "published": true,
                                  "variants": [
                                    {
                                      "sku": "OXF-WHT-M",
                                      "size": "M",
                                      "color": "White",
                                      "price": 450000,
                                      "stockQuantity": 10,
                                      "imageUrl": "https://example.test/oxford-m.jpg",
                                      "active": true
                                    }
                                  ],
                                  "imageUrls": [
                                    "https://example.test/oxford-1.jpg",
                                    "https://example.test/oxford-2.jpg"
                                  ]
                                }
                                """.formatted(brandId, categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.variants", hasSize(1)))
                .andExpect(jsonPath("$.data.images", hasSize(2)))
                .andReturn();
        long variantId = readLong(productResult, "$.data.variants[0].id");
        String productSlug = readString(productResult, "$.data.slug");

        mockMvc.perform(get("/api/v1/products")
                        .param("keyword", "Oxford")
                        .param("categoryId", Long.toString(categoryId))
                        .param("brandId", Long.toString(brandId))
                        .param("gender", "men"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].slug").value(productSlug));

        mockMvc.perform(get("/api/v1/products/{slug}", productSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.variants[0].stockQuantity").value(10))
                .andExpect(jsonPath("$.data.images[?(@.primary == true)]", hasSize(1)));

        MvcResult cartResult = mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "variantId": %d,
                                  "quantity": 2
                                }
                                """.formatted(variantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andReturn();
        long cartItemId = readLong(cartResult, "$.data.items[0].id");

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", cartItemId)
                        .header("Authorization", bearer(userToken))
                        .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].quantity").value(3));

        MvcResult addressResult = mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientName": "Customer User",
                                  "phone": "0900000000",
                                  "province": "Ho Chi Minh",
                                  "district": "District 1",
                                  "ward": "Ben Nghe",
                                  "streetAddress": "1 Nguyen Hue",
                                  "isDefault": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.province").value("Ho Chi Minh"))
                .andReturn();
        long addressId = readLong(addressResult, "$.data.id");

        mockMvc.perform(put("/api/v1/addresses/{id}", addressId)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientName": "Customer User",
                                  "phone": "0911111111",
                                  "province": "Ho Chi Minh",
                                  "district": "District 3",
                                  "ward": "Ward 6",
                                  "streetAddress": "20 Vo Van Tan",
                                  "isDefault": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("0911111111"));

        mockMvc.perform(get("/api/v1/addresses")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipientName": "Customer User",
                                  "recipientPhone": "0911111111",
                                  "shippingAddress": "20 Vo Van Tan, District 3, Ho Chi Minh",
                                  "paymentMethod": "cod",
                                  "note": "Leave at reception"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.paymentMethod").value("cod"))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andReturn();
        long orderId = readLong(orderResult, "$.data.id");

        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) orderId));

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/admin/orders/{id}/status", orderId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "confirmed",
                                  "note": "Stock confirmed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("confirmed"));

        MvcResult shipmentResult = mockMvc.perform(post("/api/v1/admin/shipments")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "carrier": "GHN",
                                  "trackingNumber": "GHN123456",
                                  "shippingFee": 30000
                                }
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andReturn();
        long shipmentId = readLong(shipmentResult, "$.data.id");

        mockMvc.perform(patch("/api/v1/admin/shipments/{id}/status", shipmentId)
                        .header("Authorization", bearer(adminToken))
                        .param("status", "in_transit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("in_transit"));

        mockMvc.perform(get("/api/v1/admin/shipments/order/{orderId}", orderId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trackingNumber").value("GHN123456"));

        mockMvc.perform(post("/api/v1/admin/inventory/movements")
                        .header("Authorization", bearer(adminToken))
                        .param("variantId", Long.toString(variantId))
                        .param("quantity", "5")
                        .param("reason", "adjustment")
                        .param("note", "Cycle count adjustment"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products/{slug}", productSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.variants[0].stockQuantity").value(12));

        mockMvc.perform(post("/api/v1/admin/inventory/movements")
                        .header("Authorization", bearer(adminToken))
                        .param("variantId", Long.toString(variantId))
                        .param("quantity", "1")
                        .param("reason", "return")
                        .param("note", "Customer return"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products/{slug}", productSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.variants[0].stockQuantity").value(13));

        mockMvc.perform(post("/api/v1/coupons")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "SAVE10",
                                  "description": "Ten percent off",
                                  "discountType": "percentage",
                                  "discountValue": 10,
                                  "minOrderAmount": 1000,
                                  "usageLimit": 100,
                                  "usageLimitPerUser": 1,
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("SAVE10"));

        mockMvc.perform(get("/api/v1/coupons/validate")
                        .header("Authorization", bearer(userToken))
                        .param("code", "SAVE10")
                        .param("orderAmount", "200000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("SAVE10"));
    }

    private String registerAndReadToken(String email, String fullName) throws Exception {
        return registerAndReadToken(email, fullName, phoneFromEmail(email));
    }

    private String registerAndReadToken(String email, String fullName, String phone) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret123",
                                  "fullName": "%s",
                                  "phone": "%s"
                                }
                                """.formatted(email, fullName, phone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.isAdmin").value(false))
                .andReturn();
        return readString(result, "$.data.token");
    }

    private String loginAndReadToken(String email, String password) throws Exception {
        return loginAndReadToken(email, password, true);
    }

    private String loginAndReadToken(String email, String password, boolean expectedAdmin) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.isAdmin").value(expectedAdmin))
                .andReturn();
        return readString(result, "$.data.token");
    }

    private static String phoneFromEmail(String email) {
        return email.startsWith("admin") ? "0900000001" : "0900000002";
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String readString(MvcResult result, String path) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), path);
    }

    private static long readLong(MvcResult result, String path) throws Exception {
        Number value = JsonPath.read(result.getResponse().getContentAsString(), path);
        return value.longValue();
    }
}
