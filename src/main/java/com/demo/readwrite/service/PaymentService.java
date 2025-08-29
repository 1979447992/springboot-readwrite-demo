package com.demo.readwrite.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.readwrite.annotation.MasterDB;
import com.demo.readwrite.entity.Payment;
import com.demo.readwrite.mapper.PaymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@MasterDB("支付服务所有操作强制使用主库")
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    @Transactional
    public Payment createPayment(Long userId, BigDecimal amount, String paymentMethod, String description) {
        String orderNo = generateOrderNo();
        Payment payment = new Payment(userId, orderNo, amount, paymentMethod, description);
        save(payment);
        return payment;
    }

    @Transactional
    public Payment updatePaymentStatus(Long id, Integer status) {
        Payment payment = getById(id);
        if (payment != null) {
            payment.setStatus(status);
            updateById(payment);
        }
        return payment;
    }

    @MasterDB("支付状态查询必须强制主库")
    public Payment getPaymentById(Long id) {
        return getById(id);
    }

    @MasterDB("订单号查询必须强制主库")
    public Payment findByOrderNo(String orderNo) {
        return baseMapper.findByOrderNo(orderNo);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return baseMapper.findByUserId(userId);
    }

    @MasterDB("支付状态统计必须强制主库")
    public List<Payment> getPaymentsByStatus(Integer status) {
        return baseMapper.findByStatus(status);
    }

    public List<Payment> getUserPaymentsByStatus(Long userId, Integer status) {
        return baseMapper.findByUserIdAndStatus(userId, status);
    }

    @Transactional
    @MasterDB("处理支付必须强制主库")
    public boolean processPayment(String orderNo) {
        Payment payment = findByOrderNo(orderNo);
        if (payment != null && payment.getStatus() == 0) {
            payment.setStatus(1);
            updateById(payment);
            return true;
        }
        return false;
    }

    @Transactional
    @MasterDB("退款处理必须强制主库")
    public boolean refundPayment(String orderNo) {
        Payment payment = findByOrderNo(orderNo);
        if (payment != null && payment.getStatus() == 1) {
            payment.setStatus(3);
            updateById(payment);
            return true;
        }
        return false;
    }

    private String generateOrderNo() {
        return "ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}