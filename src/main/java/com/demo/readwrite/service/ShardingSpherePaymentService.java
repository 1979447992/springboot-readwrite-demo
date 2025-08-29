package com.demo.readwrite.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.readwrite.config.MasterRouteManager;
import com.demo.readwrite.entity.Payment;
import com.demo.readwrite.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 使用ShardingSphere的支付服务示例
 * 展示零侵入式读写分离的使用方法
 */
@Service
public class ShardingSpherePaymentService extends ServiceImpl<PaymentMapper, Payment> {

    @Autowired
    private MasterRouteManager masterRouteManager;

    // ========== 自动读写分离示例 ==========
    
    /**
     * 查询操作 - 自动路由到从库
     * 不需要任何额外配置，ShardingSphere会自动识别SELECT语句
     */
    public List<Payment> getAllPayments() {
        // 这个查询会自动路由到从库
        return list();
    }
    
    /**
     * 写操作 - 自动路由到主库  
     * 不需要任何额外配置，ShardingSphere会自动识别INSERT/UPDATE/DELETE语句
     */
    @Transactional
    public Payment createPayment(Long userId, BigDecimal amount, String method) {
        Payment payment = new Payment(userId, generateOrderNo(), amount, method, "ShardingSphere测试");
        // 这个插入操作会自动路由到主库
        save(payment);
        return payment;
    }

    // ========== 强制主库查询示例 ==========
    
    /**
     * 支付状态查询 - 强制主库查询
     * 适用于对数据一致性要求极高的场景（如支付状态查询）
     */
    public Payment getPaymentStatusFromMaster(Long id) {
        // 使用强制主库路由
        return masterRouteManager.executeOnMaster(() -> {
            // 这里的查询操作会强制路由到主库
            return getById(id);
        });
    }
    
    /**
     * 支付处理 - 强制主库操作
     * 确保读取最新状态，避免并发问题
     */
    @Transactional
    public boolean processPaymentWithMasterRead(String orderNo) {
        return masterRouteManager.executeOnMaster(() -> {
            // 查询和更新都在主库执行，确保数据一致性
            Payment payment = baseMapper.findByOrderNo(orderNo);
            if (payment != null && payment.getStatus() == 0) {
                payment.setStatus(1);
                updateById(payment);
                return true;
            }
            return false;
        });
    }

    // ========== 现有老代码无需修改 ==========
    
    /**
     * 老项目中的方法，无需任何修改
     * ShardingSphere会自动根据SQL类型进行路由
     */
    public Payment findPaymentByOrderNo(String orderNo) {
        // 即使方法名不规范，SELECT语句仍会路由到从库
        return baseMapper.findByOrderNo(orderNo);
    }
    
    /**
     * 批量更新操作，无需修改
     */
    @Transactional
    public void batchUpdatePaymentStatus(List<Long> ids, Integer status) {
        // UPDATE语句会自动路由到主库
        ids.forEach(id -> {
            Payment payment = new Payment();
            payment.setId(id);
            payment.setStatus(status);
            updateById(payment);
        });
    }

    private String generateOrderNo() {
        return "SS_ORDER_" + System.currentTimeMillis();
    }
}