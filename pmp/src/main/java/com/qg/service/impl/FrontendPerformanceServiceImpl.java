package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.FrontendPerformance;
import com.qg.domain.Result;
import com.qg.mapper.FrontendPerformanceMapper;
import com.qg.service.FrontendPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.qg.domain.Code.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * @Description: 前端性能应用  // 类说明
 * @ClassName: FrontendPerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:35   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class FrontendPerformanceServiceImpl implements FrontendPerformanceService {

    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;

    @Override
    public Result saveFrontendPerformance(String data) {
        try {
            List<FrontendPerformance> frontendPerformanceList = JSONUtil.toList(data, FrontendPerformance.class);

            if (frontendPerformanceList == null || frontendPerformanceList.isEmpty()) {
                log.error("前端性能数据为空");
                return new Result(BAD_REQUEST, "前端性能数据为空"); // 返回0表示没有数据需要保存
            }
            int count = 0;

            for (FrontendPerformance performance : frontendPerformanceList) {
                // 假设有一个方法来保存单个性能数据条目
                count += frontendPerformanceMapper.insert(performance);
            }
            log.info("保存前端性能数据成功，保存了" + count + "条数据");
            return new Result(SUCCESS, "保存前端性能数据成功"); // 返回保存的记录数
        } catch (Exception e) {
            log.error("保存前端性能数据出错：{}", e.getMessage());
            return new Result(INTERNAL_ERROR, "保存前端性能数据出错");
        }

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
