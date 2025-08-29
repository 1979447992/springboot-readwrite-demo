package com.demo.readwrite.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.readwrite.annotation.MasterDB;
import com.demo.readwrite.entity.Bill;
import com.demo.readwrite.mapper.BillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BillService extends ServiceImpl<BillMapper, Bill> {

    @Transactional
    public Bill createBill(Long userId, String title, BigDecimal amount, Integer type, String remark) {
        String billNo = generateBillNo();
        Bill bill = new Bill(userId, billNo, title, amount, type, remark);
        save(bill);
        return bill;
    }

    @Transactional
    public Bill updateBill(Bill bill) {
        updateById(bill);
        return bill;
    }

    public boolean deleteBill(Long id) {
        return removeById(id);
    }

    public Bill getBillById(Long id) {
        return getById(id);
    }

    public List<Bill> getAllBills() {
        return list();
    }

    public Bill findByBillNo(String billNo) {
        return baseMapper.findByBillNo(billNo);
    }

    public List<Bill> getBillsByUserId(Long userId) {
        return baseMapper.findByUserId(userId);
    }

    public List<Bill> getBillsByType(Integer type) {
        return baseMapper.findByType(type);
    }

    public List<Bill> getUserBillsByType(Long userId, Integer type) {
        return baseMapper.findByUserIdAndType(userId, type);
    }

    @MasterDB("账单统计需要强制主库查询")
    public BigDecimal calculateUserTotalAmount(Long userId) {
        QueryWrapper<Bill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("status", 1);
        List<Bill> bills = list(queryWrapper);
        return bills.stream()
                   .map(Bill::getAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @MasterDB("账单类型统计需要强制主库查询")
    public BigDecimal calculateTotalAmountByType(Integer type) {
        QueryWrapper<Bill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", type).eq("status", 1);
        List<Bill> bills = list(queryWrapper);
        return bills.stream()
                   .map(Bill::getAmount)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Bill> searchBills(String keyword) {
        QueryWrapper<Bill> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title", keyword)
                   .or()
                   .like("remark", keyword)
                   .or()
                   .like("bill_no", keyword);
        return list(queryWrapper);
    }

    private String generateBillNo() {
        return "BILL_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}