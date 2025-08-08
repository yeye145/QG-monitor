package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendLog;
import com.qg.mapper.BackendLogMapper;
import com.qg.service.BackendLogService;
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
    private BackendLogMapper backendLogMapper;


    @Override
    public Integer saveBackendLogs(List<BackendLog> backendLogs) {
        if (backendLogs == null || backendLogs.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;

        for (BackendLog backendLog : backendLogs) {
            // 假设有一个方法来保存单个日志条目
            count += backendLogMapper.insert(backendLog);
        }

        return backendLogs.size() == count ? count : 0; // 返回保存的记录数
    }

    @Override
    public List<BackendLog> getAllLogs(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return List.of(); // 返回空列表表示没有日志数据
        }
        LambdaQueryWrapper <BackendLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BackendLog::getProjectId, projectId);

        return backendLogMapper.selectList(queryWrapper);
    }
}
