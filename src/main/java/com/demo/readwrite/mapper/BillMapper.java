package com.demo.readwrite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.readwrite.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    @Select("SELECT * FROM bills WHERE bill_no = #{billNo} AND deleted = 0")
    Bill findByBillNo(@Param("billNo") String billNo);

    @Select("SELECT * FROM bills WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Bill> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM bills WHERE type = #{type} AND deleted = 0 ORDER BY create_time DESC")
    List<Bill> findByType(@Param("type") Integer type);

    @Select("SELECT * FROM bills WHERE user_id = #{userId} AND type = #{type} AND deleted = 0 ORDER BY create_time DESC")
    List<Bill> findByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
}