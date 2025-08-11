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
            "'mobile:error:' || project_id || ':' || error_type || ':' || #{className,jdbcType=VARCHAR} AS redis_key, " +
            "ar.threshold " +
            "FROM alert_rule ar " +
            "WHERE project_id = #{projectId,jdbcType=VARCHAR} " +
            "AND error_type = #{errorType,jdbcType=VARCHAR}")
    @MapKey("redis_key")
    HashMap<String, Integer> selectByMobileRedisKeyToMap(
            @Param("projectId") String projectId,
            @Param("errorType") String errorType,
            @Param("className") String className);

}
