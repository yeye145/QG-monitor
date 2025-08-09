package com.qg.service.impl;

import cn.hutool.json.JSONUtil;
import com.qg.aggregator.FrontendErrorAggregator;
import com.qg.domain.BackendError;
import com.qg.domain.FrontendBehavior;
import com.qg.domain.FrontendError;
import com.qg.domain.Result;
import com.qg.mapper.FrontendBehaviorMapper;
import com.qg.service.FrontendBehaviorService;
import com.qg.vo.FrontendErrorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.qg.domain.Code.*;

/**
 * @Description: 前端行为应用  // 类说明
 * @ClassName: FrontendBehaviorServiceImpl    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/7 21:34   // 时间
 * @Version: 1.0     // 版本
 */
@Service
@Slf4j
public class FrontendBehaviorServiceImpl implements FrontendBehaviorService {
    @Autowired
    private FrontendBehaviorMapper frontendBehaviorMapper;

    @Autowired
    private FrontendErrorAggregator frontendErrorAggregator;

    @Override
    public Integer saveFrontendBehavior(List<FrontendBehavior> behaviorList) {
        if (behaviorList == null || behaviorList.isEmpty()) {
            return 0; // 返回0表示没有数据需要保存
        }
        int count = 0;
        for (FrontendBehavior behavior : behaviorList) {
            // 假设有一个方法来保存单个行为数据条目
            count += frontendBehaviorMapper.insert(behavior);
        }
        return behaviorList.size() == count ? count : 0; // 返回保存的记录
    }

    @Override
    public Result addFrontendError(String errorData) {
        if (errorData == null) {
            log.error("参数为空");
            return new Result(BAD_REQUEST, "参数为空");
        }

        try {
            FrontendErrorVO frontendErrorVO = JSONUtil.toBean(errorData, FrontendErrorVO.class);
            log.debug("前端错误信息VO： {}", frontendErrorVO);
            List<FrontendError> frontendErrorList = frontendErrorVO.getData();
            log.debug("前端错误信息list长度： {}", frontendErrorList.size());
            FrontendError frontendError = frontendErrorList.getFirst();
            frontendError.setProjectId(frontendErrorVO.getProjectId());
            frontendError.setTimestamp(frontendErrorVO.getTimestamp());
            log.debug("前端错误信息： {}", frontendError);
            if (frontendError.getProjectId() == null ||
                    frontendError.getErrorType() == null ||
                    frontendError.getSessionId() == null) {
                log.error("参数错误");
                return new Result(BAD_REQUEST, "参数错误");
            }

            // 设置当前时间戳（如果未设置）
            if (frontendError.getTimestamp() != null) {
            } else {
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
