package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendError;
import com.qg.domain.BackendPerformance;
import com.qg.domain.Module;
import com.qg.domain.Result;
import com.qg.mapper.BackendPerformanceMapper;
import com.qg.mapper.ModuleMapper;
import com.qg.service.BackendPerformanceService;
import com.qg.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 后端性能应用  // 类说明
 * @ClassName: BackendPerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:34   // 时间
 * @Version: 1.0     // 版本
 */
@Service
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


        return new Result(200, backendPerformances, "查询成功" );
    }
}
