package com.qg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.qg.dto.MethodInvocationDTO;
import com.qg.vo.MethodInvocationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MethodInvocationMapper extends BaseMapper<MethodInvocationDTO> {

    /**
     * 查询指定时间段内指定项目下的方法调用统计
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 方法调用统计列表(方法名和项目ID)
     */
    @Select("""
        SELECT
          method_name AS methodName,
          SUM(event) AS event 
        FROM pmp.method_invocation
        WHERE create_time >= #{startTime}
          AND create_time <= #{endTime}
          AND project_id = #{projectId}
        GROUP BY method_name;
        """)
    List<MethodInvocationVO> queryMethodInvocationStats(
            @Param("projectId") String projectId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

}
