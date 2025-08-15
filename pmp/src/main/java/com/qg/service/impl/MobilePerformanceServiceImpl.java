package com.qg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendPerformance;
import com.qg.domain.MobilePerformance;
import com.qg.domain.Result;
import com.qg.mapper.MobilePerformanceMapper;
import com.qg.service.MobilePerformanceService;
import com.qg.vo.MobileOperationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qg.domain.Code.*;

/**
 * @Description: 移动性能应用  // 类说明
 * @ClassName: MobilePerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:36   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class MobilePerformanceServiceImpl implements MobilePerformanceService {

    @Autowired
    private MobilePerformanceMapper mobilePerformanceMapper;

    @Override
    public Integer saveMobilePerformance(MobilePerformance mobilePerformance) {
        if (mobilePerformance == null) {
            return 0; // 返回0表示没有数据需要保存
        }
        return mobilePerformanceMapper.insert(mobilePerformance);
    }

    @Override
    public Result selectByCondition(String projectId, String deviceModel, String osVersion) {

        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }

        LambdaQueryWrapper<MobilePerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobilePerformance::getProjectId, projectId);

        if (deviceModel != null && !deviceModel.isEmpty()) {
            queryWrapper.eq(MobilePerformance::getDeviceModel, deviceModel);
        }

        if (osVersion != null && !osVersion.isEmpty()) {
            queryWrapper.eq(MobilePerformance::getOsVersion, osVersion);
        }

        List<MobilePerformance> mobilePerformances = mobilePerformanceMapper.selectList(queryWrapper);

        return new Result(200, List.of(new ArrayList<>(),new ArrayList<>(),mobilePerformances), "查询成功");
    }

    @Override
    public Result getAverageTime(String projectId, String timeType) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(BAD_REQUEST, "项目ID不能为空");
        }

        if (timeType == null || timeType.isEmpty()) {
            return new Result(BAD_REQUEST, "时间类型不能为空");
        }

        LambdaQueryWrapper<MobilePerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobilePerformance::getProjectId, projectId);

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
        queryWrapper.ge(MobilePerformance::getTimestamp, passTime);

        List<MobilePerformance> mobilePerformances = mobilePerformanceMapper.selectList(queryWrapper);

        // 计算加权平均响应时间
        Map<String, Double> averageTimeMap = mobilePerformances.stream()
                .filter(bp -> bp.getApiName() != null && bp.getApiTime() != null && bp.getEvent() != null)
                .collect(Collectors.groupingBy(
                        MobilePerformance::getApiName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    double totalTime = list.stream()
                                            .mapToDouble(bp -> bp.getApiTime() * bp.getEvent())
                                            .sum();
                                    int totalEvents = list.stream()
                                            .mapToInt(MobilePerformance::getEvent)
                                            .sum();
                                    return totalEvents > 0 ? totalTime / totalEvents : 0.0;
                                }
                        )
                ));


        if (mobilePerformances.isEmpty()) {
            return new Result(NOT_FOUND, "没有找到相关数据");
        }

        return new Result(SUCCESS, averageTimeMap, "查询成功");
    }

    @Override
    public Result getMobileOperation(String projectId, String timeType) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(BAD_REQUEST, "项目ID不能为空");
        }

        if (timeType == null || timeType.isEmpty()) {
            return new Result(BAD_REQUEST, "时间类型不能为空");
        }

        LambdaQueryWrapper<MobilePerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobilePerformance::getProjectId, projectId);

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

        queryWrapper.ge(MobilePerformance::getTimestamp, passTime);
        List<MobileOperationVO> mobileOperationVOList = new ArrayList<>();
        List<MobilePerformance> mobilePerformanceList = mobilePerformanceMapper.selectList(queryWrapper);
        for (MobilePerformance mobilePerformance : mobilePerformanceList) {
            MobileOperationVO mobileOperationVO = BeanUtil.copyProperties(mobilePerformance,MobileOperationVO.class);
            mobileOperationVOList.add(mobileOperationVO);
        }

        return new Result(SUCCESS, mobileOperationVOList,"查询成功");
    }
}
