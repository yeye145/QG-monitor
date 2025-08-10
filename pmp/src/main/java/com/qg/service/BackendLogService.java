package com.qg.service;

import org.springframework.web.bind.annotation.RequestBody;
import com.qg.domain.BackendLog;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: BackendLogService    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:30   // 时间
 * @Version: 1.0     // 版本
 */
public interface BackendLogService {

    List<BackendLog> getAllLogs(String projectId);

    List<BackendLog> getLogsByCondition(String evn, String logLevel, String projectId);

    void receiveLogFromSDK(String logJSON);

}
