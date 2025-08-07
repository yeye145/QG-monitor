package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.FrontendError;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: 前端错误mapper  // 类说明
 * @ClassName: FrontendErrorMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:27   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface FrontendErrorMapper extends BaseMapper<FrontendError> {
}
