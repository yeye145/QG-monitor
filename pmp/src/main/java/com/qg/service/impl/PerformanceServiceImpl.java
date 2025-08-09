package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qg.domain.Code;
import com.qg.domain.Performance;
import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.mapper.PerformanceMapper;
import com.qg.mapper.ProjectMapper;
import com.qg.service.PerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PerformanceServiceImpl implements PerformanceService {
    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private ProjectMapper projectMapper;

    public Result addPerformance(List<Performance> performance) {
        log.debug("添加性能信息: {}", performance);
        if (performance == null || performance.isEmpty()) {
            log.error("添加性能信息失败，性能信息为空");
            return new Result(Code.BAD_REQUEST, "添加性能信息失败，性能信息为空");
        }
        LambdaQueryWrapper<Project> findqueryWrapper = new LambdaQueryWrapper<>();
        findqueryWrapper.eq(Project::getUuid, performance.get(0).getProjectId());
        Project project = projectMapper.selectOne(findqueryWrapper);
        if (project == null) {
            log.error("添加性能信息失败，项目id不存在");
            return new Result(Code.NOT_FOUND, "添加性能信息失败，项目不存在");
        }

        try {
            log.debug("开始批量保存，数据量: {}", performance.size());
            int result = 0;
            for(Performance performance1 : performance)
            {
                LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Performance::getProjectId, performance1.getProjectId())
                            .eq(Performance::getMetricName, performance1.getMetricName())
                            .eq(Performance::getEnv, performance1.getEnv())
                            .eq(Performance::getMetricValue, performance1.getMetricValue())
                            .eq(Performance::getUnit, performance1.getUnit());

                Performance exsitingPerformance  = performanceMapper.selectOne(queryWrapper);
                if(exsitingPerformance == null) {
//                    if (performance1.getEvent() == null) {
//                        performance1.setEvent(1);
//                    }

                    performanceMapper.insert(performance1);
                    result++;
                }
                else{
                    LambdaUpdateWrapper<Performance> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Performance::getProjectId, performance1.getProjectId())
                            .eq(Performance::getMetricName, performance1.getMetricName())
                            .eq(Performance::getEnv, performance1.getEnv())
                            .eq(Performance::getMetricValue, performance1.getMetricValue())
                            .eq(Performance::getUnit, performance1.getUnit())
                            .set(Performance::getCreatedTime, new Date()) // 设置为当前时间
                            .set(Performance::getEvent,exsitingPerformance.getEvent() + 1 );
                    performanceMapper.update( updateWrapper);
                    result++;
                }

            }
            log.info("添加性能信息成功，共处理 {} 条记录", result);
            if(result != performance.size())
            {
                log.warn("添加性能信息成功，但有 {} 条记录处理失败", performance.size() - result);
                return new Result(Code.INTERNAL_ERROR, "有 " + (performance.size() - result) + " 条记录处理失败");
            }
            return new Result(Code.SUCCESS, "添加性能信息成功");
        } catch (Exception e) {
            log.error("添加性能信息失败", e);
            return new Result(Code.INTERNAL_ERROR, "添加性能信息失败");
        }
    }

    @Override
    public Result selectByProjectId(String projectId) {
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getProjectId, projectId.trim());
        List< Performance> list = performanceMapper.selectList(queryWrapper);
        if(list == null)
            return new Result(Code.NOT_FOUND, "该项目暂无性能信息！");
        return new Result(Code.SUCCESS, list, "查询成功");
    }

    @Override
    public Result selectByEnvProjectId(String env, String projectId) {
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getProjectId, projectId.trim())
                .eq(Performance::getEnv, env.trim());
        List< Performance> list = performanceMapper.selectList(queryWrapper);
        if(list == null)
            return new Result(Code.NOT_FOUND, "该项目暂无性能信息！");
        return new Result(Code.SUCCESS, list, "查询成功");
    }


}
