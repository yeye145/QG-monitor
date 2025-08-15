package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.BackendLog;
import com.qg.vo.EarthVO;
import com.qg.vo.IllegalAttackVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 后端日志mapper  // 类说明
 * @ClassName: BackendLogMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:24   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface BackendLogMapper extends BaseMapper<BackendLog> {

    /**
     * 查询指定时间段内所有IP的拦截次数统计
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 拦截统计列表(IP和拦截次数)
     */
    @Select("""
            SELECT 
              SUBSTRING(log_message FROM '拦截ip:([0-9.:]+),') AS ip,
              SUM(event) AS event
            FROM pmp.backend_log
            WHERE timestamp BETWEEN #{startTime} AND #{endTime}
              AND project_id = #{projectId}
              AND log_message LIKE '拦截ip:%'
            GROUP BY ip
            ORDER BY event DESC
            """
    )
    List<IllegalAttackVO> queryIpInterceptionCount(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);


    /**
     * 查询指定时间段内所有境外访问的IP的拦截次数统计
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("""
    SELECT 
      SUBSTRING(log_message FROM '拦截ip:([0-9.:]+),') AS ip,
      SUBSTRING(log_message FROM 'country:([^,]+)') AS country,
      SUBSTRING(log_message FROM 'city:([^,]+)') AS city,
      CAST(SUBSTRING(log_message FROM 'latitude:([0-9.-]+)') AS double precision) AS latitude,
      CAST(SUBSTRING(log_message FROM 'longitude:([0-9.-]+)') AS double precision) AS longitude,
      SUM(event) AS event 
    FROM pmp.backend_log 
    WHERE timestamp BETWEEN #{startTime} AND #{endTime}
      AND project_id = #{projectId} 
      AND log_message LIKE '拦截ip:%' 
      AND log_message LIKE '%reason:ip为境外访问%' 
    GROUP BY ip, country, city, latitude, longitude 
    ORDER BY event DESC
    """)
    List<EarthVO> queryForeignIpInterceptions(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
