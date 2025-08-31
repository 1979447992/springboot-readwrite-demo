package com.demo.readwrite.controller;

import com.demo.readwrite.entity.Payment;
import com.demo.readwrite.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPayments() {
        log.info("【@DS业务数据库】查询所有支付记录");
        List<Payment> payments = paymentService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", payments);
        result.put("count", payments.size());
        log.info("【@DS业务数据库】查询完成，共找到 {} 条记录", payments.size());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable Long id) {
        log.info("【@DS业务数据库】查询支付记录，ID: {}", id);
        Payment payment = paymentService.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (payment != null) {
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("data", payment);
            log.info("【@DS业务数据库】查询成功，订单号: {}", payment.getOrderNo());
        } else {
            result.put("success", false);
            result.put("message", "支付记录不存在");
            result.put("data", null);
            log.warn("【@DS业务数据库】支付记录不存在，ID: {}", id);
        }
        return ResponseEntity.ok(result);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestParam Long userId,
                                @RequestParam String orderNo,
                                @RequestParam BigDecimal amount,
                                @RequestParam(defaultValue = "alipay") String paymentMethod,
                                @RequestParam(defaultValue = "pending") String status,
                                @RequestParam(required = false) String description) {
        log.info("【@DS业务数据库】创建支付记录，用户ID: {}, 订单号: {}, 金额: {}", userId, orderNo, amount);
        Map<String, Object> result = new HashMap<>();
        try {
            Payment payment = new Payment(userId, orderNo, amount, paymentMethod, status, description);
            boolean saved = paymentService.save(payment);
            if (saved) {
                result.put("success", true);
                result.put("message", "支付记录创建成功");
                result.put("data", payment);
                log.info("【@DS业务数据库】创建成功，支付ID: {}, 订单号: {}", payment.getId(), payment.getOrderNo());
            } else {
                result.put("success", false);
                result.put("message", "支付记录创建失败");
                result.put("data", null);
                log.error("【@DS业务数据库】创建失败，订单号: {}", orderNo);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
            result.put("data", null);
            log.error("【@DS业务数据库】创建异常，订单号: {}, 错误: {}", orderNo, e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePayment(@PathVariable Long id,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String description) {
        log.info("【@DS业务数据库】更新支付记录，ID: {}, 状态: {}", id, status);
        Map<String, Object> result = new HashMap<>();
        try {
            Payment payment = paymentService.getById(id);
            if (payment != null) {
                if (status != null) payment.setStatus(status);
                if (description != null) payment.setDescription(description);
                boolean updated = paymentService.updateById(payment);
                if (updated) {
                    result.put("success", true);
                    result.put("message", "支付记录更新成功");
                    result.put("data", payment);
                    log.info("【@DS业务数据库】更新成功，ID: {}, 新状态: {}", id, payment.getStatus());
                } else {
                    result.put("success", false);
                    result.put("message", "支付记录更新失败");
                    result.put("data", null);
                    log.error("【@DS业务数据库】更新失败，ID: {}", id);
                }
            } else {
                result.put("success", false);
                result.put("message", "支付记录不存在");
                result.put("data", null);
                log.warn("【@DS业务数据库】更新失败，记录不存在，ID: {}", id);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            result.put("data", null);
            log.error("【@DS业务数据库】更新异常，ID: {}, 错误: {}", id, e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePayment(@PathVariable Long id) {
        log.info("【@DS业务数据库】删除支付记录，ID: {}", id);
        Map<String, Object> result = new HashMap<>();
        try {
            Payment payment = paymentService.getById(id);
            if (payment != null) {
                boolean deleted = paymentService.removeById(id);
                if (deleted) {
                    result.put("success", true);
                    result.put("message", "支付记录删除成功");
                    result.put("data", payment);
                    log.info("【@DS业务数据库】删除成功，ID: {}, 订单号: {}", id, payment.getOrderNo());
                } else {
                    result.put("success", false);
                    result.put("message", "支付记录删除失败");
                    result.put("data", null);
                    log.error("【@DS业务数据库】删除失败，ID: {}", id);
                }
            } else {
                result.put("success", false);
                result.put("message", "支付记录不存在");
                result.put("data", null);
                log.warn("【@DS业务数据库】删除失败，记录不存在，ID: {}", id);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            result.put("data", null);
            log.error("【@DS业务数据库】删除异常，ID: {}, 错误: {}", id, e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
