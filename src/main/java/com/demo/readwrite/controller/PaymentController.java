package com.demo.readwrite.controller;

import com.demo.readwrite.entity.Payment;
import com.demo.readwrite.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestParam Long userId,
                                               @RequestParam BigDecimal amount,
                                               @RequestParam String paymentMethod,
                                               @RequestParam(required = false) String description) {
        Payment payment = paymentService.createPayment(userId, amount, paymentMethod, description);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Long id,
                                                      @RequestParam Integer status) {
        Payment payment = paymentService.updatePaymentStatus(id, status);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/order/{orderNo}")
    public ResponseEntity<Payment> getPaymentByOrderNo(@PathVariable String orderNo) {
        Payment payment = paymentService.findByOrderNo(orderNo);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable Integer status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Payment>> getUserPaymentsByStatus(@PathVariable Long userId,
                                                               @PathVariable Integer status) {
        List<Payment> payments = paymentService.getUserPaymentsByStatus(userId, status);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestParam String orderNo) {
        boolean success = paymentService.processPayment(orderNo);
        String message = success ? "支付处理成功" : "支付处理失败";
        return ResponseEntity.ok(message);
    }

    @PostMapping("/refund")
    public ResponseEntity<String> refundPayment(@RequestParam String orderNo) {
        boolean success = paymentService.refundPayment(orderNo);
        String message = success ? "退款处理成功" : "退款处理失败";
        return ResponseEntity.ok(message);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.list();
        return ResponseEntity.ok(payments);
    }
}