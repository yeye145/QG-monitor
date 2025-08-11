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
            queryWrapper.eq(FrontendPerformance::getCaptureType, capture);
        }

        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(queryWrapper);

        return new Result(SUCCESS, frontendPerformances, "查询成功");
    }

    @Override
    public Result getVisits(String projectId, String timeType, Integer number) {
        // 参数校验
        if (projectId == null || projectId.trim().isEmpty()) {
            log.warn("项目ID不能为空");
            return new Result(BAD_REQUEST, "项目ID不能为空");
        }
        if (timeType == null || timeType.trim().isEmpty()) {
            log.warn("时间类型不能为空");
            return new Result(BAD_REQUEST, "时间类型不能为空");
        }



        if (number <= 0 || timeType.equals("day") && number > 24 || timeType.equals("week") && number > 7 || timeType.equals("year") && number > 12) {
            //参数不合法就默认
            switch (timeType) {
                case "day":
                    number = 24; // 默认过去24小时
                    break;
                case "week":
                    number = 7; // 默认过去7天
                    break;
                case "year":
                    number = 12; // 默认过去12个月
                    break;
                default:
                    log.warn("时间类型不合法");
                    return new Result(BAD_REQUEST, "时间类型不合法");
            }
        }
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;

        switch (timeType) {
            case "day":
                startTime = endTime.minusHours(number);
                break;
            case "week":
                startTime = endTime.minusDays(number);
                break;
            case "year":
                startTime = endTime.minusMonths(number);
                break;
            default:
                log.warn("时间类型不合法");
                return new Result(BAD_REQUEST, "时间类型不合法");
        }
        // 查询数据

        LambdaQueryWrapper<FrontendPerformance> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(FrontendPerformance::getProjectId, projectId)
                .between(FrontendPerformance::getTimestamp, startTime, endTime);

        List<FrontendPerformance> performances = frontendPerformanceMapper.selectList(queryWrapper);

        Integer count = 0 ;
        for (FrontendPerformance performance : performances) {
            count += performance.getEvent();
        }

        return new Result(SUCCESS, count, "查询成功");
    }
}
