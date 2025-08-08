package com.qg.service;

import com.qg.domain.BackendError;
import com.qg.domain.BackendPerformance;
import com.qg.domain.Result;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: BackendErrorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:30   // 时间
 * @Version: 1.0     // 版本
 */
public interface BackendErrorService {
    Result selectByCondition(String projectId, Long moduleId, String type);

    Integer saveBackendError(BackendError backendError);
}
