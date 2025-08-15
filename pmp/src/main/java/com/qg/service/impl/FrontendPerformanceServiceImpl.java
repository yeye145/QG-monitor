package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.FrontendBehavior;
import com.qg.domain.FrontendPerformance;
import com.qg.domain.Result;
import com.qg.mapper.FrontendPerformanceMapper;
import com.qg.service.FrontendPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        // 参数校验
        if (data == null || data.trim().isEmpty()) {
            log.warn("前端性能数据为空");
            return new Result(BAD_REQUEST, "前端性能数据为空");
        }

        try {
            List<FrontendPerformance> frontendPerformanceList = JSONUtil.toList(data, FrontendPerformance.class);

            if (frontendPerformanceList == null || frontendPerformanceList.isEmpty()) {
                log.warn("解析后的前端性能数据为空");
                return new Result(BAD_REQUEST, "解析前端性能数据为空");
            }

            // 计数成功插入的记录数
            int successCount = 0;
            for (FrontendPerformance performance : frontendPerformanceList) {
                if (performance != null) { // 额外的空值检查
                    int result = frontendPerformanceMapper.insert(performance);
                    successCount += result;
                }
            }

            log.info("保存前端性能数据完成，总共{}条，成功{}条", frontendPerformanceList.size(), successCount);
            return new Result(SUCCESS, "保存前端性能数据成功，共处理" + frontendPerformanceList.size() + "条数据");

        } catch (cn.hutool.json.JSONException e) {
            log.error("前端性能数据JSON解析失败: ", e);
            return new Result(BAD_REQUEST, "数据格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("保存前端性能数据时发生异常: ", e);
            return new Result(INTERNAL_ERROR, "保存前端性能数据失败: " + e.getMessage());
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
            queryWrapper.like(FrontendPerformance::getCaptureType, capture);
        }

        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(queryWrapper);

        return new Result(SUCCESS, List.of(new ArrayList<>(),frontendPerformances,new ArrayList<>()), "查询成功");
    }

    @Override
    public Result getVisits(String projectId, String timeType) {
        // 参数校验
        if (projectId == null || projectId.trim().isEmpty()) {
            log.warn("项目ID不能为空");
            return new Result(BAD_REQUEST, "项目ID不能为空");
        }
        if (timeType == null || timeType.trim().isEmpty()) {
            log.warn("时间类型不能为空");
            return new Result(BAD_REQUEST, "时间类型不能为空");
        }

        Result count = new Result();
        switch (timeType) {
            case "day":
            case "week":
            case "month":
            case "year":
                count = getVisitCount(projectId, timeType);
                break;
            default:
                log.warn("不支持的时间类型: {}", timeType);
                return new Result(BAD_REQUEST, "不支持的时间类型: " + timeType);
        }



        if (count != null) return count;


        return new Result(BAD_GATEWAY,"查询失败");
    }

    private Result getVisitCount(String projectId, String timeType) {

        List<Integer> timeCount = new ArrayList<>();

        switch (timeType) {
            case "day":
                for (int i = 0; i < 24; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            case "week":
                for (int i = 0; i < 7; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            case "month":
                for (int i = 0; i < 30; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            case "year":
                for (int i = 0; i < 12; i++) {
                    getCount(projectId, i, timeCount, timeType);
                }
                break;
            default:
                return new Result(BAD_REQUEST, "不支持的时间类型");
        }
        return new Result(SUCCESS, timeCount,"查询成功");
    }

    private void getCount(String projectId, int i, List<Integer> timeCount, String timeType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start, end;
        switch (timeType) {
            case "day":
                start = now.minusHours(i + 1);
                end = now.minusHours(i);
                break;
            case "week":
                start = now.minusDays(i + 1);
                end = now.minusDays(i);
                break;
            case "month":
                start = now.minusWeeks(i + 1);
                end = now.minusWeeks(i);
                break;
            case "year":
                start = now.minusMonths(i + 1);
                end = now.minusMonths(i);
                break;
            default:
                return;
        }
        LambdaQueryWrapper<FrontendPerformance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FrontendPerformance::getProjectId, projectId);
        wrapper.between(FrontendPerformance::getTimestamp, start, end);
        List<FrontendPerformance> list = frontendPerformanceMapper.selectList(wrapper);
        int count = list.stream()
                .filter(p -> p.getEvent() != null)
                .mapToInt(FrontendPerformance::getEvent)
                .sum();
        timeCount.add(count);
    }

    @Override
    public Result getAverageTime(String projectId, String timeType) {

        return new Result();
    }
}
