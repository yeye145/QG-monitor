package com.qg.service;

import com.qg.vo.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface GraphService {
    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute
    (String projectId, String route, LocalDateTime startTime, LocalDateTime endTime);

    List<ErrorTrendVO> getErrorTrend
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<ManualTrackingVO> getManualTrackingStats
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    Object[] getErrorStats(String projectId);

    FrontendPerformanceAverageVO getAverageFrontendPerformanceTime
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<MethodInvocationVO> getMethodInvocationStats
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);

    List<IllegalAttackVO> getIpInterceptionCount
            (String projectId, LocalDateTime startTime, LocalDateTime endTime);
}
