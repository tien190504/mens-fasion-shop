package com.powerranger.mens_fashion_backend;

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
class MensFashionBackendApplicationTests {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("mens_fashion_test")
            .withUsername("mensfashion")
            .withPassword("mensfashion");

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
    void implementedMvpApiFlowsWork() throws Exception {
        String adminToken = registerAndReadToken("admin@example.com", "Admin User");
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
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "secret123",
                                  "fullName": "%s",
                                  "phone": "%s"
                                }
                                """.formatted(email, fullName, phoneFromEmail(email))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").exists())
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
