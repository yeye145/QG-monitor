package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.FrontendPerformance;
import com.qg.domain.Result;
import com.qg.mapper.FrontendPerformanceMapper;
import com.qg.service.FrontendPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 前端性能应用  // 类说明
 * @ClassName: FrontendPerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:35   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class FrontendPerformanceServiceImpl implements FrontendPerformanceService {

    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;

    @Override
    public Integer saveFrontendPerformance(List<FrontendPerformance> frontendPerformance) {
        if (frontendPerformance == null || frontendPerformance.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;

        for (FrontendPerformance performance : frontendPerformance) {
            // 假设有一个方法来保存单个性能数据条目
            count += frontendPerformanceMapper.insert(performance);
        }

        return frontendPerformance.size() == count ? count : 0; // 返回保存的记录数

    }

    @Override
    public Result selectByCondition(String projectId, String capture) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }
        LambdaQueryWrapper<FrontendPerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FrontendPerformance::getProjectId, projectId);

        if (capture != null && !capture.isEmpty()) {
            queryWrapper.eq(FrontendPerformance::getCaptureType, capture);
        }

        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(queryWrapper);

        return new Result(200, frontendPerformances, "查询成功");
    }
}
