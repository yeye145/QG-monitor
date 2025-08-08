package com.qg.service.impl;

import com.qg.domain.BackendPerformance;
import com.qg.mapper.BackendPerformanceMapper;
import com.qg.service.BackendPerformanceService;
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
}
