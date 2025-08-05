package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.Code;
import com.qg.domain.Error;
import com.qg.domain.Result;
import com.qg.mapper.ErrorMapper;
import com.qg.service.ErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorMapper errorMapper;

    @Override
    public Result addError(List<Error> errorList) {
        log.debug("添加错误信息: {}", errorList);
        if (errorList == null || errorList.isEmpty()) {
            log.error("添加错误信息失败，错误信息为空");
            return new Result(Code.BAD_REQUEST, "添加错误信息失败，错误信息为空");
        }

        try {
            log.debug("开始批量保存，数据量: {}", errorList.size());
            // 使用注入的 mapper
            for (Error error : errorList) {
                errorMapper.insert(error);
            }
            log.info("添加错误信息成功，共处理 {} 条记录", errorList.size());
            return new Result(Code.SUCCESS, "添加错误信息成功");
        } catch (Exception e) {
            log.error("添加错误信息失败，错误信息: {}", errorList, e);
            return new Result(Code.INTERNAL_ERROR, "添加错误信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result selectByEnv(String env, String projectId, Long moduleId) {
        if (env == null || env.isEmpty() || projectId == null || projectId.isEmpty()) {
            log.error("查询错误信息失败，参数为空");
            return new Result(Code.BAD_REQUEST, "查询错误信息失败，参数为空");
        }
        try {
            LambdaQueryWrapper<Error> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Error::getEnv, env.trim())
                    .eq(Error::getProjectId, projectId.trim())
                    .orderByDesc(Error::getTimestamp);

            // 只有当 moduleId 不为空时才添加 moduleId 条件
            if (moduleId != null) {
                queryWrapper.eq(Error::getModuleId, moduleId);
            }
            List<Error> errorList = errorMapper.selectList(queryWrapper);
            log.info("成功查询错误信息: {}", errorList);
            return new Result(Code.SUCCESS, errorList, "查询成功");
        } catch (Exception e) {
            log.error("查询错误信息失败，环境: {}", env, e);
            return new Result(Code.INTERNAL_ERROR, "查询错误信息失败: " + e.getMessage());
        }
    }
}
