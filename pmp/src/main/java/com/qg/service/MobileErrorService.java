package com.qg.service;

import com.qg.domain.MobileError;
import com.qg.domain.Result;

/**
 * @Description: // 类说明
 * @ClassName: MobileErrorService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:32   // 时间
 * @Version: 1.0     // 版本
 */
public interface MobileErrorService {
    Result selectByCondition(String projectId, String type);

    String receiveErrorFromSDK(String mobileErrorJSON);
}
