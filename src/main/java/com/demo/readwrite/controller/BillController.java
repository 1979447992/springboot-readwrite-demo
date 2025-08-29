package com.demo.readwrite.controller;

import com.demo.readwrite.entity.Bill;
import com.demo.readwrite.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {

    @Autowired
    private BillService billService;

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestParam Long userId,
                                         @RequestParam String title,
                                         @RequestParam BigDecimal amount,
                                         @RequestParam Integer type,
                                         @RequestParam(required = false) String remark) {
        Bill bill = billService.createBill(userId, title, amount, type, remark);
        return ResponseEntity.ok(bill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable Long id,
                                         @RequestBody Bill bill) {
        bill.setId(id);
        Bill updatedBill = billService.updateBill(bill);
        return ResponseEntity.ok(updatedBill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteBill(@PathVariable Long id) {
        boolean result = billService.deleteBill(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        Bill bill = billService.getBillById(id);
        return bill != null ? ResponseEntity.ok(bill) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> bills = billService.getAllBills();
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/billno/{billNo}")
    public ResponseEntity<Bill> getBillByBillNo(@PathVariable String billNo) {
        Bill bill = billService.findByBillNo(billNo);
        return bill != null ? ResponseEntity.ok(bill) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bill>> getBillsByUserId(@PathVariable Long userId) {
        List<Bill> bills = billService.getBillsByUserId(userId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Bill>> getBillsByType(@PathVariable Integer type) {
        List<Bill> bills = billService.getBillsByType(type);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Bill>> getUserBillsByType(@PathVariable Long userId,
                                                       @PathVariable Integer type) {
        List<Bill> bills = billService.getUserBillsByType(userId, type);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<BigDecimal> getUserTotalAmount(@PathVariable Long userId) {
        BigDecimal total = billService.calculateUserTotalAmount(userId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/type/{type}/total")
    public ResponseEntity<BigDecimal> getTotalAmountByType(@PathVariable Integer type) {
        BigDecimal total = billService.calculateTotalAmountByType(type);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Bill>> searchBills(@RequestParam String keyword) {
        List<Bill> bills = billService.searchBills(keyword);
        return ResponseEntity.ok(bills);
    }
}