package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.Module;
import com.qg.mapper.BackendPerformanceMapper;
import com.qg.mapper.ModuleMapper;
import com.qg.service.BackendPerformanceService;
import com.qg.service.ModuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qg.domain.Code.*;

/**
 * @Description: 后端性能应用  // 类说明
 * @ClassName: BackendPerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:34   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class BackendPerformanceServiceImpl implements BackendPerformanceService {

    @Autowired
    private BackendPerformanceMapper backendPerformanceMapper;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private ModuleService moduleService;

    @Override
    public int saveBackendPerformance(List<BackendPerformance> backendPerformances) {
        if (backendPerformances == null || backendPerformances.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;

        for (BackendPerformance backendPerformance : backendPerformances) {
            count += backendPerformanceMapper.insert(backendPerformance);
        }

        return backendPerformances.size()==count ? count : 0 ; // 返回保存的记录数
    }

    @Override
    public Result selectByCondition(String projectId, String api) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }
        LambdaQueryWrapper<BackendPerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendPerformance::getProjectId, projectId);


        if (api != null && !api.isEmpty()) {
            queryWrapper.like(BackendPerformance::getApi, api);
        }


        List<BackendPerformance> backendPerformances = backendPerformanceMapper.selectList(queryWrapper);


        return new Result(SUCCESS, List.of(backendPerformances,new ArrayList<>(),new ArrayList<>()), "查询成功" );
    }

    @Override
    public Result addPerformance(String performanceData) {
        // 添加参数校验
        if (performanceData == null || performanceData.trim().isEmpty()) {
            log.warn("后端性能数据为空");
            return new Result(BAD_REQUEST, "后端性能数据为空");
        }

        try {
            // 解析JSON数据
            List<BackendPerformance> backendPerformances = JSONUtil.toList(performanceData, BackendPerformance.class);

            if (backendPerformances == null || backendPerformances.isEmpty()) {
                log.warn("解析后端性能数据为空");
                return new Result(BAD_REQUEST, "解析后端性能数据为空");
            }

            // 过滤掉 projectId 为空的数据
            List<BackendPerformance> validPerformances = backendPerformances.stream()
                    .filter(performance -> performance.getProjectId() != null && !performance.getProjectId().isEmpty())
                    .toList();

            if (validPerformances.isEmpty()) {
                log.warn("没有有效的后端性能数据（projectId为空）");
                return new Result(BAD_REQUEST, "没有有效的后端性能数据");
            }

            int count = 0;
            for (BackendPerformance performance : validPerformances) {
                count += backendPerformanceMapper.insert(performance);
                moduleService.putModuleIfAbsent(performance.getModule(), performance.getProjectId());
            }
            boolean isSuccess = count > 0;

            if (isSuccess) {
                log.info("成功插入{}条后端性能数据", validPerformances.size());
                return new Result(SUCCESS, "成功插入" + validPerformances.size() + "条数据");
            } else {
                log.error("后端性能数据批量插入失败");
                return new Result(INTERNAL_ERROR, "后端性能数据保存失败");
            }

        } catch (Exception e) {
            log.error("后端性能数据保存失败: ", e);
            return new Result(INTERNAL_ERROR, "后端性能数据保存失败: " + e.getMessage());
        }
    }

    @Override
    public Result getAverageTime(String projectId, String timeType) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(BAD_REQUEST, "项目ID不能为空");
        }

        if (timeType == null || timeType.isEmpty()) {
            return new Result(BAD_REQUEST, "时间类型不能为空");
        }

        LambdaQueryWrapper<BackendPerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendPerformance::getProjectId, projectId);

        LocalDateTime passTime;

        switch (timeType) {
            case "day":
                passTime = LocalDateTime.now().minusDays(1);
                break;
            case "week":
                passTime = LocalDateTime.now().minusWeeks(1);
                break;
            case "month":
                passTime = LocalDateTime.now().minusMonths(1);
                break;
            default:
                return new Result(BAD_REQUEST, "不支持的时间类型");
        }
        queryWrapper.ge(BackendPerformance::getTimestamp, passTime);

        List<BackendPerformance> backendPerformances = backendPerformanceMapper.selectList(queryWrapper);

        // 计算加权平均响应时间
        Map<String, Double> averageTimeMap = backendPerformances.stream()
                .filter(bp -> bp.getApi() != null && bp.getDuration() != null && bp.getEvent() != null)
                .collect(Collectors.groupingBy(
                        BackendPerformance::getApi,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    double totalTime = list.stream()
                                            .mapToDouble(bp -> bp.getDuration() * bp.getEvent())
                                            .sum();
                                    int totalEvents = list.stream()
                                            .mapToInt(BackendPerformance::getEvent)
                                            .sum();
                                    return totalEvents > 0 ? totalTime / totalEvents : 0.0;
                                }
                        )
                ));


        if (backendPerformances.isEmpty()) {
            return new Result(NOT_FOUND, "没有找到相关数据");
        }

        return new Result(SUCCESS, averageTimeMap, "查询成功");
    }

}
