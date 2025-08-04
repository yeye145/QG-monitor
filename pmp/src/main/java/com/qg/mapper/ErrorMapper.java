package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.domain.AlertRule;
import com.qg.domain.Error;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErrorMapper extends BaseMapper<Error> {
}
