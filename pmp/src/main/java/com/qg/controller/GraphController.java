package com.qg.controller;

import cn.hutool.core.util.StrUtil;
import com.qg.domain.Code;
import com.qg.domain.Result;
import com.qg.service.FrontendBehaviorService;
import com.qg.vo.FrontendBehaviorVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequestMapping("/graph")
@RestController
@Tag(name = "前端图数据来源")
public class GraphController {

    @Autowired
    private FrontendBehaviorService frontendBehaviorService;

    @GetMapping("/pageStateAndEnterCount")
    public Result pageStateAndEnterCount(
            @RequestParam String projectId,
            @RequestParam(required = false) String route,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (StrUtil.isBlank(projectId) || startTime == null || endTime == null) {
            log.warn("参数为空" +
                     ",projectId:" + projectId +
                     ",startTime:" + startTime +
                     ",endTime:" + endTime);
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        // 时间需要有交集
        if (startTime.isAfter(endTime)) {
            log.warn("时间交集为空");
            return new Result(Code.BAD_REQUEST, "时间交集为空");
        }

        // 定义结果集合
        List<FrontendBehaviorVO> list;

        // 执行不同查询
        if (StrUtil.isBlank(route)) {
            list = frontendBehaviorService
                    .queryTimeDataByProjectIdAndTimeRange(projectId, startTime, endTime);
            log.info("""
                    
                    查询项目id:{}
                    起始时间:{}
                    终止时间:{}\
                    
                    内的页面停留时间数据成功""", projectId, startTime, endTime);
        } else {
            list = frontendBehaviorService
                    .queryTimeDataByProjectIdAndTimeRangeAndRoute(projectId, route, startTime, endTime);
            log.info("""
                    
                    查询项目id:{}
                    路由:{}
                    起始时间:{}
                    终止时间:{}\
                    
                    内的页面停留时间数据成功""", projectId, route, startTime, endTime);
        }

        // 至少返回空集合
        return new Result(Code.SUCCESS, !list.isEmpty() ? list : Collections.emptyList(), "查询成功");

    }

}
