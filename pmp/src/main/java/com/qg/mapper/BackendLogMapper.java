package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.BackendLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: 后端日志mapper  // 类说明
 * @ClassName: BackendLogMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:24   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface BackendLogMapper extends BaseMapper<BackendLog> {
}
