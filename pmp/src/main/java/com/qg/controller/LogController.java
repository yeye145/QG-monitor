package com.qg.controller;

import com.qg.domain.BackendLog;
import com.qg.domain.Result;
import com.qg.service.BackendLogService;
import com.qg.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.qg.domain.Code.SUCCESS;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService logService;


    @Autowired
    private BackendLogService backendLogService;


    @GetMapping("/getAllLogs/{projectId}")
    public Result getAllLogs(@PathVariable String projectId) {
        List<BackendLog> logs = backendLogService.getAllLogs(projectId);
        if (logs != null && !logs.isEmpty()) {
            return new Result(SUCCESS,logs,"查询成功");
        } else {
            return new Result(500, "没有日志数据");
        }
    }
}
