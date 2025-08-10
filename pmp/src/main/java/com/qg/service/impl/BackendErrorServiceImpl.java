package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.aggregator.BackendErrorAggregator;
import com.qg.domain.BackendError;
import com.qg.domain.BackendPerformance;
import com.qg.domain.Module;
import com.qg.domain.Result;
import com.qg.mapper.BackendErrorMapper;
import com.qg.mapper.ModuleMapper;
import com.qg.service.BackendErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.qg.domain.Code.*;

/**
 * @Description: 后端错误应用  // 类说明
 * @ClassName: BackendErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:32   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class BackendErrorServiceImpl implements BackendErrorService {

    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private BackendErrorAggregator backendErrorAggregator;

    @Override
    public Result selectByCondition(String projectId, Long moduleId, String type) {
        if (projectId == null) {
            return new Result(BAD_REQUEST, "参数不能为空");
        }

        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
        Module module = moduleMapper.selectById(moduleId);
        if (module != null) {
            String moduleName = module.getModuleName();
            queryWrapper.eq(BackendError::getModule, moduleName);
        } else {
            return new Result(BAD_REQUEST, "模块不存在");
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(BackendError::getErrorType, type);
        }
        queryWrapper.eq(BackendError::getProjectId, projectId);

        List<BackendError> backendErrors = backendErrorMapper.selectList(queryWrapper);

        return new Result(SUCCESS, backendErrors,"查询成功");

    }

    @Override
    public Integer saveBackendError(BackendError backendError) {
        String projectId = backendError.getProjectId();
        String moduleName = backendError.getModule();
        if (backendError == null|| projectId == null) {
            return 0; // 返回0表示没有数据需要保存
        }
        LambdaQueryWrapper<Module> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Module::getProjectId, projectId).
                eq(Module::getModuleName, moduleName);
        Module module = moduleMapper.selectOne(queryWrapper);
        if (module == null) {
            moduleMapper.insert(new Module(projectId, moduleName));
        }

        return backendErrorMapper.insert(backendError);
    }

    @Override
    public Result addBackendError(String errorData) {
        if (errorData == null) {
            log.error("参数为空");
            return new Result(BAD_REQUEST, "参数为空");
        }

        try {
            BackendError backendError = JSONUtil.toBean(errorData, BackendError.class);
            if (backendError.getProjectId() == null ||
                    backendError.getErrorType() == null ||
                    backendError.getEnvironment() == null) {
                log.error("参数错误");
                return new Result(BAD_REQUEST, "参数错误");
            }

            // 设置当前时间戳（如果未设置）
            if (backendError.getTimestamp() == null) {
                backendError.setTimestamp(LocalDateTime.now());
            }

            // 添加到 Redis 聚合器缓存中
            backendErrorAggregator.addErrorToCache(backendError);
            return new Result(SUCCESS, "添加错误信息成功");
        } catch (Exception e) {
            log.error("添加错误信息时出错，错误信息： {}", errorData, e);
            return new Result(INTERNAL_ERROR, "添加错误信息失败");
        }
    }
}
