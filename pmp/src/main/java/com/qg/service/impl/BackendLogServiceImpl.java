package com.qg.service.impl;


import cn.hutool.json.JSONUtil;
import com.qg.domain.BackendLog;

import com.qg.repository.BackendLogRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.mapper.BackendLogMapper;

import com.qg.service.BackendLogService;
import com.qg.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 后端日志应用  // 类说明
 * @ClassName: BackendLogServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:33   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class BackendLogServiceImpl implements BackendLogService {

    @Autowired
    private BackendLogRepository backendLogRepository;
    @Autowired
    private BackendLogMapper backendLogMapper;
    @Autowired
    private ModuleService moduleService;


    @Override
    public List<BackendLog> getAllLogs(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return List.of(); // 返回空列表表示没有日志数据
        }
        LambdaQueryWrapper<BackendLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendLog::getProjectId, projectId);

        return backendLogMapper.selectList(queryWrapper);

    }

    /**
     * 获取后端SDK发送的日志
     * @param logJSON
     * @return
     */
    @Override
    public String receiveLogFromSDK(String logJSON) {
        // 转换数据，进行缓存交互
        try {
            JSONUtil.toList(logJSON, BackendLog.class)
                    .forEach(log -> {
                        moduleService.putModuleIfAbsent(log.getModule(), log.getProjectId());
                        backendLogRepository.statistics(log);
                    });
            return "backend-info-log存入缓存成功";
        } catch (Exception e) {
            return "backend-info-log存入缓存失败";
        }
    }


    @Override
    public List<BackendLog> getLogsByCondition(String evn, String logLevel, String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return List.of(); // 返回空列表表示没有日志数据
        }
        LambdaQueryWrapper<BackendLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendLog::getProjectId, projectId);
        if (evn != null && !evn.isEmpty()) {
            queryWrapper.eq(BackendLog::getEnvironment, evn);
        }
        if (logLevel != null && !logLevel.isEmpty()) {
            queryWrapper.eq(BackendLog::getLevel, logLevel);
        }

        return backendLogMapper.selectList(queryWrapper);
    }
}
