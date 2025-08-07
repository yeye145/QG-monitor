package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.MobilePerformance;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: 移动性能mapper  // 类说明
 * @ClassName: MobilePerformanceMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:29   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface MobilePerformanceMapper extends BaseMapper<MobilePerformance> {
    // 这里可以添加特定于移动性能的查询方法
}
