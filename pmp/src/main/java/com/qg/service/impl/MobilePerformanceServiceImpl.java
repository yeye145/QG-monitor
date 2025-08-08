package com.qg.service.impl;

import com.qg.domain.MobilePerformance;
import com.qg.mapper.MobilePerformanceMapper;
import com.qg.service.MobilePerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
