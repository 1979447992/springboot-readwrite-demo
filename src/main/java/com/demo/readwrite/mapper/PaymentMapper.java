package com.demo.readwrite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.readwrite.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM payments WHERE order_no = #{orderNo} AND deleted = 0")
    Payment findByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM payments WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM payments WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<Payment> findByStatus(@Param("status") Integer status);

    @Select("SELECT * FROM payments WHERE user_id = #{userId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
}