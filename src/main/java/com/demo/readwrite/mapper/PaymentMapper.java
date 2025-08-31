package com.demo.readwrite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.demo.readwrite.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("business")
public interface PaymentMapper extends BaseMapper<Payment> {
}
