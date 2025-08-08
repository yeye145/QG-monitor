package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.qg.domain.Result;
import com.qg.repository.LogINFORepository;
import com.qg.service.BackendLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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
    private LogINFORepository logINFORepository;

    @Override
    public String getLog(@RequestBody String logJSON) {
        logINFORepository.checkLogRepeat(logJSON);
        return JSONUtil.toJsonStr(new Result(200, "已接收日志信息"));
    }
}
