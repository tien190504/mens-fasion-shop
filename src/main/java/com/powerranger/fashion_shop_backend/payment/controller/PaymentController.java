package com.powerranger.fashion_shop_backend.payment.controller;

import com.powerranger.fashion_shop_backend.common.ApiResponse;
import com.powerranger.fashion_shop_backend.payment.dto.PaymentRequest;
import com.powerranger.fashion_shop_backend.payment.dto.PaymentResponse;
import com.powerranger.fashion_shop_backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment link created", response));
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<String>> callback(@RequestParam Map<String, String> params) {
        paymentService.processCallback(params);
        String status = params.getOrDefault("status", "failed");
        return ResponseEntity.ok(ApiResponse.ok("Payment " + status, "Transaction status: " + status));
    }

    @PostMapping("/ipn")
    public ResponseEntity<ApiResponse<String>> ipn(@RequestParam Map<String, String> params) {
        paymentService.processIpn(params);
        return ResponseEntity.ok(ApiResponse.ok("IPN processed successfully", null));
    }

    @GetMapping("/mock-checkout")
    public ResponseEntity<String> mockCheckout(
            @RequestParam Long orderId,
            @RequestParam String ref,
            @RequestParam BigDecimal amount,
            @RequestParam String method) {
        String html = """
                <html>
                <head>
                    <title>Mock Payment Gateway</title>
                    <style>
                        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f7f9fc; margin: 0; }
                        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); text-align: center; max-width: 400px; width: 100%; }
                        h2 { color: #333; }
                        p { color: #666; margin-bottom: 25px; line-height: 1.5; }
                        .btn { display: inline-block; padding: 12px 24px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; text-decoration: none; width: 100%%; box-sizing: border-box; margin-bottom: 10px; text-align: center; }
                        .btn-success { background-color: #28a745; color: white; }
                        .btn-danger { background-color: #dc3545; color: white; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h2>Cổng thanh toán giả lập</h2>
                        <p>Phương thức: <strong>%s</strong><br>Mã đơn hàng ID: <strong>%d</strong><br>Số tiền: <strong>%,.0f VND</strong></p>
                        <a href="/api/v1/payments/callback?ref=%s&status=success" class="btn btn-success">Thanh toán Thành công</a>
                        <a href="/api/v1/payments/callback?ref=%s&status=failed" class="btn btn-danger">Thanh toán Thất bại</a>
                    </div>
                </body>
                </html>
                """.formatted(method.toUpperCase(), orderId, amount, ref, ref);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
}
