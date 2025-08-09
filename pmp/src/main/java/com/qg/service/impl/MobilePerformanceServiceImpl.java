package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.MobilePerformance;
import com.qg.domain.Result;
import com.qg.mapper.MobilePerformanceMapper;
import com.qg.service.MobilePerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 移动性能应用  // 类说明
 * @ClassName: MobilePerformanceServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:36   // 时间
 * @Version: 1.0     // 版本
 */
@Service
public class MobilePerformanceServiceImpl implements MobilePerformanceService {

    @Autowired
    private MobilePerformanceMapper mobilePerformanceMapper;

    @Override
    public Integer saveMobilePerformance(MobilePerformance mobilePerformance) {
        if (mobilePerformance == null) {
            return 0; // 返回0表示没有数据需要保存
        }
        return mobilePerformanceMapper.insert(mobilePerformance);
    }

    @Override
    public Result selectByCondition(String projectId, String deviceModel, String osVersion) {

        if (projectId == null || projectId.isEmpty()) {
            return new Result(400, "项目ID不能为空");
        }

        LambdaQueryWrapper<MobilePerformance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MobilePerformance::getProjectId, projectId);

        if (deviceModel != null && !deviceModel.isEmpty()) {
            queryWrapper.eq(MobilePerformance::getDeviceModel, deviceModel);
        }

        if (osVersion != null && !osVersion.isEmpty()) {
            queryWrapper.eq(MobilePerformance::getOsVersion, osVersion);
        }

        List<MobilePerformance> mobilePerformances = mobilePerformanceMapper.selectList(queryWrapper);

        return new Result(200, mobilePerformances, "查询成功");
    }
}
