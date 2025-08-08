package com.qg.service;

import com.qg.domain.Result;

/**
 * @Description: 所有错误service  // 类说明
 * @ClassName: AllErrorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/8 11:00   // 时间
 * @Version: 1.0     // 版本
 */
public interface AllErrorService {

    Result selectByCondition(String projectId, Long moduleId, String type);

    Result selectById(Long id);
}
