package com.qg.service.impl;

import com.qg.mapper.FrontendBehaviorMapper;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.mapper.FrontendPerformanceMapper;
import com.qg.service.GraphService;
import com.qg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GraphServiceImpl implements GraphService {

    @Autowired
    private FrontendBehaviorMapper frontendBehaviorMapper;
    @Autowired
    private FrontendErrorMapper frontendErrorMapper;
    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;

    /**
     * 查询指定时间段内某项目中，用户页面停留《所有路由下》时间数据
     *
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRange
    (String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return frontendBehaviorMapper
                .queryTimeDataByProjectIdAndTimeRange(projectId, startTime, endTime);
    }


    /**
     * 查询指定时间段内某项目中，用户页面停留《在某路由下》的时间数据
     *
     * @param projectId
     * @param route
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<FrontendBehaviorVO> queryTimeDataByProjectIdAndTimeRangeAndRoute
    (String projectId, String route, LocalDateTime startTime, LocalDateTime endTime) {
        return frontendBehaviorMapper
                .queryTimeDataByProjectIdAndTimeRangeAndRoute(projectId, route, startTime, endTime);
    }

    /**
     * 按时间（允许按照时间筛选）以及错误类别（前端/后端/移动）展示错误量
     *
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<ErrorTrendVO> getErrorTrend
    (String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return frontendErrorMapper.queryErrorTrend(projectId, startTime, endTime);
    }

    /**
     * 获取埋点错误统计
     *
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<ManualTrackingVO> getManualTrackingStats
    (String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return frontendErrorMapper.queryManualTrackingStats(projectId, startTime, endTime);
    }

    /**
     * 获取两种前端错误信息
     *
     * @param projectId
     * @return
     */
    @Override
    public Object[] getErrorStats(String projectId) {

        List<UvBillDataVO> uvBillDataVOList = new ArrayList<>();
        List<TransformDataVO> transformDataVOList = new ArrayList<>();
        frontendErrorMapper
                .queryFrontendErrorStats(projectId)
                .forEach(errorStat -> {
                    uvBillDataVOList.add(new UvBillDataVO(errorStat.getErrorType(), errorStat.getCount()));
                    transformDataVOList.add(new TransformDataVO(errorStat.getErrorType(), errorStat.getRatio()));
                });

        return new Object[]{uvBillDataVOList, transformDataVOList};
    }

    /**
     * 获取前端性能，加载时间数据
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public FrontendPerformanceAverageVO getAverageFrontendPerformanceTime
    (String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        return frontendPerformanceMapper.queryAverageFrontendPerformanceTime(projectId, startTime, endTime);
    }

}
