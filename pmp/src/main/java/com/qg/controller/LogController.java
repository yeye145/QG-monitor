package com.qg.controller;

import com.qg.domain.BackendLog;
import com.qg.domain.Result;
import com.qg.service.BackendLogService;
import com.qg.service.LogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.qg.domain.Code.SUCCESS;

@RestController
@RequestMapping("/logs")
@Tag(name = "日志信息")
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

    @GetMapping("/selectByCondition")
    public Result getLogsByCondition (@RequestParam(required = false) String evn,
                                      @RequestParam(required = false) String logLevel, @RequestParam String projectId) {
        List<BackendLog> logs = backendLogService.getLogsByCondition(evn, logLevel, projectId);
        if (logs != null && !logs.isEmpty()) {
            return new Result(SUCCESS, logs, "查询成功");
        } else {
            return new Result(500, "没有符合条件的日志数据");
        }
    }
}
