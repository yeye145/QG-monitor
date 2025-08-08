package com.qg.service;

import com.qg.domain.Result;

/**
 * @Description: // 类说明
 * @ClassName: FrontendErrorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:31   // 时间
 * @Version: 1.0     // 版本
 */
public interface FrontendErrorService {
    Result selectByCondition(String projectId, String type);
}
