package com.demo.readwrite.service;

import com.demo.readwrite.entity.Payment;
import com.demo.readwrite.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceTest {

    @MockBean
    private PaymentMapper paymentMapper;

    private PaymentService paymentService;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        
        testPayment = new Payment(1001L, "ORDER_TEST_001", new BigDecimal("99.99"), "微信支付", "测试支付");
        testPayment.setId(2001L);
    }

    @Test
    void createPayment_ShouldReturnPayment_WhenValidData() {
        when(paymentMapper.insert(any(Payment.class))).thenReturn(1);
        
        Payment result = paymentService.createPayment(1001L, new BigDecimal("99.99"), "微信支付", "测试支付");
        
        assertNotNull(result);
        assertEquals(1001L, result.getUserId());
        assertEquals(new BigDecimal("99.99"), result.getAmount());
        assertEquals("微信支付", result.getPaymentMethod());
        assertEquals("测试支付", result.getDescription());
        assertTrue(result.getOrderNo().startsWith("ORDER_"));
        verify(paymentMapper, times(1)).insert(any(Payment.class));
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenPaymentExists() {
        when(paymentMapper.selectById(2001L)).thenReturn(testPayment);
        
        Payment result = paymentService.getPaymentById(2001L);
        
        assertNotNull(result);
        assertEquals(2001L, result.getId());
        assertEquals("ORDER_TEST_001", result.getOrderNo());
        verify(paymentMapper, times(1)).selectById(2001L);
    }

    @Test
    void findByOrderNo_ShouldReturnPayment_WhenOrderExists() {
        when(paymentMapper.findByOrderNo("ORDER_TEST_001")).thenReturn(testPayment);
        
        Payment result = paymentService.findByOrderNo("ORDER_TEST_001");
        
        assertNotNull(result);
        assertEquals("ORDER_TEST_001", result.getOrderNo());
        verify(paymentMapper, times(1)).findByOrderNo("ORDER_TEST_001");
    }

    @Test
    void updatePaymentStatus_ShouldReturnUpdatedPayment_WhenPaymentExists() {
        when(paymentMapper.selectById(2001L)).thenReturn(testPayment);
        when(paymentMapper.updateById(any(Payment.class))).thenReturn(1);
        
        Payment result = paymentService.updatePaymentStatus(2001L, 1);
        
        assertNotNull(result);
        assertEquals(1, result.getStatus());
        verify(paymentMapper, times(1)).selectById(2001L);
        verify(paymentMapper, times(1)).updateById(any(Payment.class));
    }

    @Test
    void processPayment_ShouldReturnTrue_WhenPaymentPending() {
        testPayment.setStatus(0); // 待支付状态
        when(paymentMapper.findByOrderNo("ORDER_TEST_001")).thenReturn(testPayment);
        when(paymentMapper.updateById(any(Payment.class))).thenReturn(1);
        
        boolean result = paymentService.processPayment("ORDER_TEST_001");
        
        assertTrue(result);
        assertEquals(1, testPayment.getStatus()); // 应该变为已支付
        verify(paymentMapper, times(1)).findByOrderNo("ORDER_TEST_001");
        verify(paymentMapper, times(1)).updateById(testPayment);
    }

    @Test
    void processPayment_ShouldReturnFalse_WhenPaymentAlreadyProcessed() {
        testPayment.setStatus(1); // 已支付状态
        when(paymentMapper.findByOrderNo("ORDER_TEST_001")).thenReturn(testPayment);
        
        boolean result = paymentService.processPayment("ORDER_TEST_001");
        
        assertFalse(result);
        verify(paymentMapper, times(1)).findByOrderNo("ORDER_TEST_001");
        verify(paymentMapper, times(0)).updateById(any(Payment.class));
    }

    @Test
    void refundPayment_ShouldReturnTrue_WhenPaymentPaid() {
        testPayment.setStatus(1); // 已支付状态
        when(paymentMapper.findByOrderNo("ORDER_TEST_001")).thenReturn(testPayment);
        when(paymentMapper.updateById(any(Payment.class))).thenReturn(1);
        
        boolean result = paymentService.refundPayment("ORDER_TEST_001");
        
        assertTrue(result);
        assertEquals(3, testPayment.getStatus()); // 应该变为已退款
        verify(paymentMapper, times(1)).findByOrderNo("ORDER_TEST_001");
        verify(paymentMapper, times(1)).updateById(testPayment);
    }

    @Test
    void getPaymentsByStatus_ShouldReturnPayments_WhenPaymentsExist() {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentMapper.findByStatus(1)).thenReturn(payments);
        
        List<Payment> result = paymentService.getPaymentsByStatus(1);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getOrderNo(), result.get(0).getOrderNo());
        verify(paymentMapper, times(1)).findByStatus(1);
    }
}