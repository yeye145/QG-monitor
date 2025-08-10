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

import java.util.List;
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
    public Result selectByCondition(String projectId, Long moduleId, String api, String environment) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }
        LambdaQueryWrapper<BackendPerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendPerformance::getProjectId, projectId);

        Module module = moduleMapper.selectById(moduleId);
        if (module != null) {
            String moduleName = module.getModuleName();
            queryWrapper.eq(BackendPerformance::getModule, moduleName);
        } else {
            return new Result(400, "模块不存在");
        }

        if (api != null && !api.isEmpty()) {
            queryWrapper.eq(BackendPerformance::getApi, api);
        }

        if (environment != null && !environment.isEmpty()) {
            queryWrapper.eq(BackendPerformance::getEnvironment, environment);
        }

        List<BackendPerformance> backendPerformances = backendPerformanceMapper.selectList(queryWrapper);


        return new Result(SUCCESS, backendPerformances, "查询成功" );
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
                    .collect(Collectors.toList());

            if (validPerformances.isEmpty()) {
                log.warn("没有有效的后端性能数据（projectId为空）");
                return new Result(BAD_REQUEST, "没有有效的后端性能数据");
            }

            int count = 0;
            for (BackendPerformance performance : validPerformances) {
                count += backendPerformanceMapper.insert(performance);
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

}
