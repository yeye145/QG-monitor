package com.qg.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.aggregator.FrontendErrorAggregator;
import com.qg.domain.FrontendError;
import com.qg.domain.FrontendPerformance;
import com.qg.domain.Result;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.service.FrontendErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.qg.domain.Code.*;

/**
 * @Description: 前端错误应用  // 类说明
 * @ClassName: FrontendErrorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:35   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class FrontendErrorServiceImpl implements FrontendErrorService {

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private FrontendErrorAggregator frontendErrorAggregator;

    @Override
    public Result selectByCondition(String projectId, String type) {
        if (projectId == null) {
            return new Result(400, "参数不能为空");
        }
        LambdaQueryWrapper<FrontendError> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(FrontendError::getProjectId, projectId);

        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(FrontendError::getErrorType, type);
        }

        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(queryWrapper);

        return new Result(200, frontendErrors, "查询成功");
    }

    @Override
    public Integer saveFrontendError(List<FrontendError> frontendErrors) {
        if (frontendErrors == null || frontendErrors.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;

        for (FrontendError error : frontendErrors) {
            count += frontendErrorMapper.insert(error);
        }

        return frontendErrors.size() == count ? count : 0; // 返回保存的记录数
    }

    @Override
    public Result addFrontendError(String errorData) {
        if (errorData == null) {
            log.error("参数为空");
            return new Result(BAD_REQUEST, "参数为空");
        }

        try {
            FrontendError frontendError = JSONUtil.toBean(errorData, FrontendError.class);
            if (frontendError.getProjectId() == null ||
                    frontendError.getErrorType() == null ||
                    frontendError.getSessionId() == null
            ) {
                log.error("参数错误");
                return new Result(BAD_REQUEST, "参数错误");
            }

            // 设置当前时间戳（如果未设置）
            if (frontendError.getTimestamp() == null) {
                frontendError.setTimestamp(LocalDateTime.now());
            }

            // 添加到 Redis 聚合器缓存中
            frontendErrorAggregator.addErrorToCache(frontendError);
            return new Result(SUCCESS, "添加错误信息成功");
        } catch (Exception e) {
            log.error("添加错误信息时出错，错误信息： {}", errorData, e);
            return new Result(INTERNAL_ERROR, "添加错误信息失败");
        }
    }
}
