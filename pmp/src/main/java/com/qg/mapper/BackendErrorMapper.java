package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.BackendError;
import com.qg.domain.BackendPerformance;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 后端错误mapper  // 类说明
 * @ClassName: BackendErrorMapper    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:23   // 时间
 * @Version: 1.0     // 版本
 */
@Mapper
public interface BackendErrorMapper extends BaseMapper<BackendError> {

    @Select("select * from backend_error where backend_error.project_id = '1'")
    List<BackendError> getBy1();



}
