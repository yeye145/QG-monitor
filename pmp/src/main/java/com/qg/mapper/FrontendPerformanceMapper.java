package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.FrontendPerformance;
import com.qg.vo.FrontendPerformanceAverageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * @Description: 前端性能mapper  // 类说明
 * @ClassName: FrontendPerformanceMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:28   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface FrontendPerformanceMapper extends BaseMapper<FrontendPerformance> {

    /**
     * 获取前端性能，加载时间数据
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("""
    SELECT
      AVG(COALESCE(
        CAST(jsonb_extract_path_text(metrics, 'vitals', 'fcp') AS NUMERIC),
        CAST(jsonb_extract_path_text(metrics, 'firstContentfulPaint') AS NUMERIC),
        0.0
      )) AS fcp,
      
      AVG(COALESCE(
        CAST(jsonb_extract_path_text(metrics, 'navigation', 'domReady') AS NUMERIC),
        CAST(jsonb_extract_path_text(metrics, 'domReady') AS NUMERIC),
        0.0
      )) AS domReady,
      
      AVG(COALESCE(
        CAST(jsonb_extract_path_text(metrics, 'navigation', 'loadComplete') AS NUMERIC),
        CAST(jsonb_extract_path_text(metrics, 'loadTime') AS NUMERIC),
        0.0
      )) AS loadComplete
    FROM frontend_performance
    WHERE project_id = #{projectId}
      AND timestamp BETWEEN #{startTime} AND #{endTime}
    """)
    FrontendPerformanceAverageVO queryAverageFrontendPerformanceTime(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
