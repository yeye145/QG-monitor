package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.FrontendError;
import com.qg.dto.ErrorStatsDTO;
import com.qg.vo.ErrorTrendVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 前端错误mapper  // 类说明
 * @ClassName: FrontendErrorMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:27   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface FrontendErrorMapper extends BaseMapper<FrontendError> {
    /**
     * 查询指定时间段内指定项目下的三端错误
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误统计趋势图所需数据
     */
    @Select("""
            SELECT
              date_trunc('hour', timestamp) AS time,
              SUM(event) AS value,
              'backend' AS category
            FROM pmp.backend_error
            WHERE timestamp >= #{startTime}
              AND timestamp <= #{endTime}
              AND project_id = #{projectId}
            GROUP BY time
            
            UNION ALL
            
            SELECT
              date_trunc('hour', timestamp) AS time,
              SUM(event) AS value,
              'frontend' AS category
            FROM pmp.frontend_error
            WHERE timestamp >= #{startTime}
              AND timestamp <= #{endTime}
              AND project_id = #{projectId}
            GROUP BY time
            
            UNION ALL
            
            SELECT
              date_trunc('hour', timestamp) AS time,
              SUM(event) AS value,
              'mobile' AS category
            FROM pmp.mobile_error
            WHERE timestamp >= #{startTime}
              AND timestamp <= #{endTime}
               AND project_id = #{projectId}
            GROUP BY time
            ORDER BY time, category;
            """)
    List<ErrorTrendVO> getErrorTrend(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询指定时间段内前端错误统计
     * @param projectId 项目ID
     * @return [[{错误统计数组}], [{错误占比数组}]]
     */
    @Select("""
        WITH frontend_stats AS (
          SELECT
            error_type AS errorType,
            SUM(event) AS count
          FROM pmp.frontend_error
          WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '7 days'
            AND timestamp <= CURRENT_TIMESTAMP
            AND project_id = #{projectId}
          GROUP BY error_type
          ORDER BY count DESC
          LIMIT 10
        ),
        total_errors AS (
          SELECT SUM(event) AS total
          FROM pmp.frontend_error
          WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '7 days'
            AND timestamp <= CURRENT_TIMESTAMP
            AND project_id = #{projectId}
        )
        SELECT
          errorType,
          count,
           CASE WHEN total = 0 THEN 0 ELSE (count::FLOAT / total) END AS ratio
        FROM frontend_stats, total_errors
        """)
    @Results({
            @Result(property = "errorType", column = "errorType"),
            @Result(property = "count", column = "count"),
            @Result(property = "ratio", column = "ratio")
    })
    List<ErrorStatsDTO> getFrontendErrorStats(@Param("projectId") String projectId);
}
