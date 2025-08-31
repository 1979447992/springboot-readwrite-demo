package com.demo.readwrite.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.readwrite.entity.Payment;
import com.demo.readwrite.mapper.PaymentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    public List<Payment> getByUserId(Long userId) {
        return this.list(new QueryWrapper<Payment>().eq("user_id", userId));
    }
    
    public Payment getByOrderNo(String orderNo) {
        return this.getOne(new QueryWrapper<Payment>().eq("order_no", orderNo));
    }
}
