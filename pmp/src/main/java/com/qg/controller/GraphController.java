package com.qg.controller;

import cn.hutool.core.util.StrUtil;
import com.qg.domain.Code;
import com.qg.domain.Result;

import com.qg.mapper.FrontendPerformanceMapper;
import com.qg.service.FrontendPerformanceService;
import com.qg.service.*;

import com.qg.service.GraphService;
import com.qg.vo.ErrorTrendVO;

import com.qg.vo.FrontendBehaviorVO;
import com.qg.vo.ManualTrackingVO;
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
    private GraphService graphService;
    @Autowired
    private FrontendErrorService frontendErrorService;


    @Autowired
    private FrontendPerformanceService frontendPerformanceService;
    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;

    @Autowired
    private BackendPerformanceService backendPerformanceService;

    @Autowired
    private MobilePerformanceService mobilePerformanceService;

    @Autowired
    private BackendErrorService backendErrorService;

    @Autowired
    private MobileErrorService mobileErrorService;


    /**
     * 页面停留时间，页面进入次数
     *
     * @param projectId
     * @param route
     * @param startTime
     * @param endTime
     * @return
     */

    @GetMapping("/pageStateAndEnterCount")
    public Result pageStateAndEnterCount(
            @RequestParam String projectId,
            @RequestParam(required = false) String route,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        // 时间需要有交集
        if (startTime.isAfter(endTime)) {
            log.warn("时间交集为空");
            return new Result(Code.BAD_REQUEST, "时间交集为空");
        }

        try {
            // 定义结果集合
            List<FrontendBehaviorVO> list;

            // 执行不同查询
            if (StrUtil.isBlank(route)) {
                list = graphService
                        .queryTimeDataByProjectIdAndTimeRange(projectId, startTime, endTime);
                log.info("""
                        
                        查询项目id:{}
                        起始时间:{}
                        终止时间:{}\
                        
                        内的页面停留时间数据成功""", projectId, startTime, endTime);
            } else {
                list = graphService
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

        } catch (Exception e) {
            log.error("查询页面停留时间，页面进入次数失败，项目id: {}:{}", projectId, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询页面停留时间，页面进入次数失败");
        }

    }


    /**
     * @Author lrt
     * @Description //访问量
     * @Date 17:23 2025/8/12
     * @Param
 * @param projectId
 * @param timeType
     * @return com.qg.domain.Result
     **/
    @GetMapping("/getVisits")
    public Result getVisits(@RequestParam String projectId, @RequestParam String timeType) {
        return frontendPerformanceService.getVisits(projectId, timeType);
    }


    /**
     * 按时间（允许按照时间筛选）以及错误类别（前端/后端/移动）展示错误量
     *
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/getErrorTrend")
    public Result getErrorTrend(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        try {
            List<ErrorTrendVO> list = graphService.getErrorTrend(projectId, startTime, endTime);
            // 至少返回空集合
            return new Result(Code.SUCCESS, !list.isEmpty() ? list : Collections.emptyList(), "查询成功");
        } catch (Exception e) {
            log.error("查询错误趋势时发生异常: projectId={}, startTime={}, endTime={}:{}", projectId, startTime, endTime, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询错误趋势失败 ");
        }
    }


    /**
     * 获取前端错误周报
     *
     * @param projectId
     * @return
     */
    @GetMapping("/getFrontendErrorStats")
    public Result getFrontendErrorStats(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            return new Result(Code.SUCCESS,
                    graphService.getErrorStats(projectId), "查询近一周前端错误统计成功");
        } catch (Exception e) {
            log.error("查询错误统计时发生异常: projectId={}:{}", projectId, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询近一周前端错误统计失败");
        }
    }


    @GetMapping("/getManualTrackingStats")
    public Result getManualTrackingStats(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        // 时间需要有交集
        if (startTime.isAfter(endTime)) {
            log.warn("时间交集为空");
            return new Result(Code.BAD_REQUEST, "时间交集为空");
        }

        try {
            List<ManualTrackingVO> list = graphService.getManualTrackingStats(projectId, startTime, endTime);
            return new Result(
                    Code.SUCCESS,
                    list.isEmpty() ? Collections.emptyList() : list,
                    "查询前端埋点统计错误成功");
        } catch (Exception e) {
            log.error("查询前端埋点统计错误时发生异常: projectId={}:{}", projectId, e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询近一周前端错误统计失败");
        }
    }


    /**
     * @Author lrt
     * @Description // 查询近一周后端错误
     * @Date 17:25 2025/8/12
     * @Param
 * @param projectId
     * @return com.qg.domain.Result
     **/
    @GetMapping("/getBackendErrorStats")
    public Result getBackendErrorStats(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            return new Result(Code.SUCCESS,
                    backendErrorService.getBackendErrorStats(projectId), "查询近一周后端错误统计成功");
        } catch (Exception e) {
            log.error("查询后端错误统计时发生异常: projectId={}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询近一周后端错误统计失败");
        }
    }


    /**
     * @Author lrt
     * @Description //TODO 近一周移动端错
     * @Date 17:25 2025/8/12
     * @Param
 * @param projectId
     * @return com.qg.domain.Result
     **/
    @GetMapping("/getMobileErrorStats")
    public Result getMobileErrorStats(@RequestParam String projectId) {
        if (StrUtil.isBlank(projectId)) {
            return new Result(Code.BAD_REQUEST, "项目id不能为空");
        }
        try {
            return new Result(Code.SUCCESS,
                    mobileErrorService.getMobileErrorStats(projectId), "查询近一周移动端错误统计成功");
        } catch (Exception e) {
            log.error("查询移动端错误统计时发生异常: projectId={}", projectId, e);
            return new Result(Code.INTERNAL_ERROR, "查询近一周移动端错误统计失败");
        }
    }


    /**
     * @Author lrt
     * @Description //TODO api平均响应时间
     * @Date 17:26 2025/8/12
     * @Param
 * @param projectId
 * @param platform
 * @param timeType
     * @return com.qg.domain.Result
     **/
    @GetMapping("/getAverageTime")
    public Result getAverageTime(@RequestParam String projectId, @RequestParam String platform,
                               @RequestParam String timeType){
        switch (platform) {
            case "frontend":
                return new Result(Code.SUCCESS, null, "功能尚未实现，敬请期待");
            case "backend":
                return backendPerformanceService.getAverageTime(projectId, timeType);
            case "mobile":
                return mobilePerformanceService.getAverageTime(projectId, timeType);
            default:
                return new Result(Code.BAD_REQUEST, "不支持的平台类型");
        }
    }



    /**
     * 查询查询前端性能数据-平均时间
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/getAverageFrontendPerformanceTime")
    public Result getAverageFrontendPerformanceTime(
            @RequestParam("projectId") String projectId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 参数合法性校验
        if (isProjectIdAndTimeNull(projectId, startTime, endTime)) {
            return new Result(Code.BAD_REQUEST, "必需参数存在空");
        }

        // 时间需要有交集
        if (startTime.isAfter(endTime)) {
            log.warn("时间交集为空");
            return new Result(Code.BAD_REQUEST, "时间交集为空");
        }

        try {
            return new Result(Code.SUCCESS
                    , frontendPerformanceMapper
                    .queryAverageFrontendPerformanceTime(projectId, startTime, endTime)
                    , "查询前端性能数据-平均时间,成功");
        } catch (Exception e) {
            log.error("查询查询前端性能数据-平均时间,失败:{}", e.getMessage());
            return new Result(Code.INTERNAL_ERROR, "查询查询前端性能数据-平均时间,失败");
        }

    }

    /**
     * 判断项目id、时间是否为空
     *
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
    private boolean isProjectIdAndTimeNull(String projectId, LocalDateTime startTime, LocalDateTime endTime) {
        if (StrUtil.isBlank(projectId) || startTime == null || endTime == null) {
            log.warn("参数为空" +
                     ",projectId:" + projectId +
                     ",startTime:" + startTime +
                     ",endTime:" + endTime);
            return true;
        }
        return false;
    }

}
