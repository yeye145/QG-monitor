package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.AlertRule;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {


    @Select("SELECT " +
            "CONCAT('mobile:error:', project_id, ':', error_type, ':', #{className}) AS redis_key, " +
            "ar.threshold " +
            "FROM alert_rule ar " +
            "WHERE project_id = #{projectId} " +
            "AND error_type = #{errorType}")
    @MapKey("redis_key")
        // 以redis_key为键
    HashMap<String, Integer> selectByMobileRedisKeyToMap(
            @Param("projectId") String projectId,
            @Param("errorType") String errorType,
            @Param("className") String className);
}
