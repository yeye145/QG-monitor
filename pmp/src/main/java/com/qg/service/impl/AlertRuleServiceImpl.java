package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qg.domain.AlertRule;
import com.qg.domain.Code;
import com.qg.domain.Result;
import com.qg.mapper.AlertRuleMapper;
import com.qg.service.AlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AlertRuleServiceImpl implements AlertRuleService {

    @Autowired
    private AlertRuleMapper alertRuleMapper;


    @Override
    public Result selectByType(String errorType, String env) {
        // 参数校验
        if (errorType == null || errorType.trim().isEmpty() || env == null || env.trim().isEmpty()) {
            return new Result(Code.BAD_REQUEST, "错误类型不能为空");
        }
        try {
            LambdaQueryWrapper<AlertRule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AlertRule::getErrorType, errorType.trim())
                    .eq(AlertRule::getEnv, env.trim());
            AlertRule alertRule = alertRuleMapper.selectOne(queryWrapper);
            if (alertRule == null) {
                log.info("未找到错误类型对应的规则: {}", errorType);
                return new Result(Code.NOT_FOUND, "未找到该错误类型");
            }
            log.debug("成功查询错误类型规则: {}", errorType);
            return new Result(Code.SUCCESS, alertRule, "查询成功");
        } catch (Exception e) {
            log.error("查询错误类型规则失败，errorType: {}", errorType, e);
            return new Result(Code.INTERNAL_ERROR, "查询失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result updateThreshold(AlertRule alertRule) {
        // 参数校验
        if (alertRule == null || alertRule.getErrorType() == null || alertRule.getErrorType().trim().isEmpty()) {
            return new Result(Code.BAD_REQUEST, "错误类型不能为空");
        }
        try {
            boolean result = alertRuleMapper.selectCount(
                    new LambdaQueryWrapper<AlertRule>()
                            .eq(AlertRule::getErrorType, alertRule.getErrorType())
                            .eq(AlertRule::getEnv, alertRule.getEnv())
            ) > 0;
            if (result) {
                // 只更新 threshold 字段
                LambdaUpdateWrapper<AlertRule> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(AlertRule::getErrorType, alertRule.getErrorType())
                        .eq(AlertRule::getEnv, alertRule.getEnv())
                        .set(AlertRule::getThreshold, alertRule.getThreshold());
                alertRuleMapper.update(null, updateWrapper);
                return new Result(Code.SUCCESS, "更新成功");
            } else {
                // 插入
                alertRuleMapper.insert(alertRule);
                return new Result(Code.SUCCESS, "创建成功");
            }
        } catch (Exception e) {
            log.error("操作阈值失败，errorType: {}", alertRule.getErrorType(), e);
            return new Result(Code.INTERNAL_ERROR, "操作失败: " + e.getMessage());
        }
    }

}
