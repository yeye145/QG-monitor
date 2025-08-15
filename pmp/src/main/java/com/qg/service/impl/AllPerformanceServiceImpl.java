package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.domain.Module;
import com.qg.mapper.BackendPerformanceMapper;
import com.qg.mapper.FrontendPerformanceMapper;
import com.qg.mapper.MobilePerformanceMapper;
import com.qg.mapper.ModuleMapper;
import com.qg.service.AllPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: AllPerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 11:28   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class AllPerformanceServiceImpl implements AllPerformanceService {

    @Autowired
    private BackendPerformanceMapper backendPerformanceMapper;


    @Autowired
    private ModuleMapper moduleMapper;

    @Autowired
    private MobilePerformanceMapper mobilePerformanceMapper;

    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;

    @Override
    public Result selectByCondition(String projectId, String api, String capture, String osVersion) {
        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }


        LambdaQueryWrapper<BackendPerformance> BackendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<FrontendPerformance> FrontendQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<MobilePerformance> MobileQueryWrapper = new LambdaQueryWrapper<>();


        BackendQueryWrapper.eq(BackendPerformance::getProjectId, projectId);
        FrontendQueryWrapper.eq(FrontendPerformance::getProjectId, projectId);
        MobileQueryWrapper.eq(MobilePerformance::getProjectId, projectId);


        if (api != null && !api.isEmpty()) {
            BackendQueryWrapper.like(BackendPerformance::getApi, api);
        }

        List<BackendPerformance> backendPerformances = backendPerformanceMapper.selectList(BackendQueryWrapper);



        if (capture != null && !capture.isEmpty()) {
            FrontendQueryWrapper.eq(FrontendPerformance::getCaptureType, capture);
        }

        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(FrontendQueryWrapper);






        if (osVersion != null && !osVersion.isEmpty()) {
            MobileQueryWrapper.like(MobilePerformance::getOsVersion, osVersion);
        }

        List<MobilePerformance> mobilePerformances = mobilePerformanceMapper.selectList(MobileQueryWrapper);


        return new Result(200,
                List.of(backendPerformances, frontendPerformances, mobilePerformances),
                "查询成功");
    }
}
