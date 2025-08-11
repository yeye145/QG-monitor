package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.FrontendError;
import com.qg.vo.ErrorTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
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
}
