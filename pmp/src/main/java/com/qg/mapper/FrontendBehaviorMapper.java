package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.FrontendBehavior;
import com.qg.vo.FrontendBehaviorVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 前端行为mapper  // 类说明
 * @ClassName: FrontendBehaviorMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:26   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface FrontendBehaviorMapper extends BaseMapper<FrontendBehavior> {

    /**
     * 查询指定时间段内指定项目下的所有前端行为
     *
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("""
            SELECT
              (crumb->'data'->>'route') AS route,
              AVG((crumb->'data'->>'totalTime')::BIGINT) AS avg_total_time,
              AVG((crumb->'data'->>'visibleTime')::BIGINT) AS avg_visible_time,
              COUNT(*) AS samples
            FROM frontend_behavior,
                 jsonb_array_elements(breadcrumbs) AS crumb
            WHERE
              project_id = #{projectId}
              AND timestamp BETWEEN #{startTime} AND #{endTime}
              AND crumb->>'message' = 'Page stay time recorded'
            GROUP BY route
            """)
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);


    /**
     * 按路由和时间范围查
     *
     * @param projectId
     * @param route
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("""
            SELECT
              (crumb->'data'->>'route') AS route,
              AVG((crumb->'data'->>'totalTime')::BIGINT) AS avg_total_time,
              AVG((crumb->'data'->>'visibleTime')::BIGINT) AS avg_visible_time,
              COUNT(*) AS samples
            FROM frontend_behavior,
                 jsonb_array_elements(breadcrumbs) AS crumb
            WHERE
              project_id = #{projectId, jdbcType=VARCHAR}
              AND timestamp BETWEEN #{startTime, jdbcType=TIMESTAMP} AND #{endTime, jdbcType=TIMESTAMP}
              AND crumb->>'message' = 'Page stay time recorded'
              AND (CAST(#{route, jdbcType=VARCHAR} AS TEXT) IS NULL
                   OR crumb->'data'->>'route' = CAST(#{route, jdbcType=VARCHAR} AS TEXT))
            GROUP BY route
            """)
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute(
            @Param("projectId") String projectId,
            @Param("route") String route,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);


}
