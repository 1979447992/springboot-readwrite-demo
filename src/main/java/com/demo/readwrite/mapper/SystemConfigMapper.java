package com.demo.readwrite.mapper;

import com.demo.readwrite.entity.SystemConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SystemConfigMapper {

    @Select("SELECT * FROM system_config WHERE id = #{id}")
    SystemConfig selectById(Long id);

    @Select("SELECT * FROM system_config ORDER BY id")
    List<SystemConfig> selectAll();

    @Select("SELECT * FROM system_config WHERE config_key = #{configKey}")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    @Insert("INSERT INTO system_config (config_key, config_value, description) VALUES (#{configKey}, #{configValue}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemConfig config);

    @Update("UPDATE system_config SET config_value = #{configValue}, description = #{description} WHERE config_key = #{configKey}")
    int updateByConfigKey(SystemConfig config);

    @Delete("DELETE FROM system_config WHERE id = #{id}")
    int deleteById(Long id);
}