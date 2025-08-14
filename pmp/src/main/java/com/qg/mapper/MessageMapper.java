package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: // 类说明
 * @ClassName: MessageMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/13 19:01   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
