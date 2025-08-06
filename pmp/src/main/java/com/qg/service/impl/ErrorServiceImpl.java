package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qg.domain.Code;
import com.qg.domain.Error;
import com.qg.domain.Project;
import com.qg.domain.Result;
import com.qg.mapper.ErrorMapper;
import com.qg.mapper.ProjectMapper;
import com.qg.service.ErrorService;
import com.qg.websocket.UnifiedWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorMapper errorMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UnifiedWebSocketHandler webSocketHandler;

    @Override
    @Transactional
    public Result addError(List<Error> errorList) {
        log.debug("添加错误信息: {}", errorList);
        if (errorList == null || errorList.isEmpty()) {
            log.error("添加错误信息失败，错误信息为空");
            return new Result(Code.BAD_REQUEST, "添加错误信息失败，错误信息为空");
        }

        try {
            log.debug("开始处理错误信息，数据量: {}", errorList.size());
            List<Error> updatedErrors = new ArrayList<>();  // 已存在的错误（更新次数）
            List<Error> newErrors = new ArrayList<>();      // 新的错误（插入）
            List<Error> broadcastErrors = new ArrayList<>(); // 需要广播的错误

            for (Error error : errorList) {
                if (error == null) continue;

                // 根据项目ID、错误类型、环境、平台查找是否已存在相同错误
                LambdaQueryWrapper<Error> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Error::getProjectId, error.getProjectId())
                        .eq(Error::getType, error.getType())
                        .eq(Error::getEnv, error.getEnv())
                        .eq(Error::getPlatform, error.getPlatform())
                        .orderByDesc(Error::getTimestamp)
                        .last("LIMIT 1");

                Error existingError = errorMapper.selectOne(queryWrapper);

                if (existingError != null) {
                    // 错误已存在，更新发生次数和时间戳
                    LambdaUpdateWrapper<Error> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Error::getId, existingError.getId())
                            .set(Error::getEvent, existingError.getEvent() + 1)
                            .set(Error::getTimestamp, LocalDateTime.now())
                            .set(Error::getMessage, error.getMessage())
                            .set(Error::getStack, error.getStack())
                            .set(Error::getBreadcrumbs, error.getBreadcrumbs())
                            .set(Error::getUrl, error.getUrl())
                            .set(Error::getUserAgent, error.getUserAgent()); // 更新错误信息

                    errorMapper.update(null, updateWrapper);

                    // 更新后的错误信息用于广播
                    existingError.setEvent(existingError.getEvent() + 1);
                    existingError.setTimestamp(LocalDateTime.now());
                    existingError.setMessage(error.getMessage());
                    existingError.setStack(error.getStack());
                    existingError.setBreadcrumbs(error.getBreadcrumbs());
                    existingError.setUrl(error.getUrl());
                    existingError.setUserAgent(error.getUserAgent());

                    updatedErrors.add(existingError);
                    broadcastErrors.add(existingError);

                    log.debug("更新错误次数，错误ID: {}, 新次数: {}", existingError.getId(), existingError.getEvent());
                } else {
                    // 错误不存在，插入
                    errorMapper.insert(error);
                    newErrors.add(error);
                    broadcastErrors.add(error);

                    log.debug("插入新错误: {}", error);
                }
            }

            // 向WebSocket客户端广播错误信息
            if (!broadcastErrors.isEmpty()) {
                broadcastNewErrors(broadcastErrors);
            }

            log.info("处理错误信息完成，总数量: {}，新增: {} 条，更新: {} 条",
                    errorList.size(), newErrors.size(), updatedErrors.size());
            return new Result(Code.SUCCESS, "处理错误信息成功");
        } catch (Exception e) {
            log.error("添加错误信息失败，错误信息: {}", errorList, e);
            return new Result(Code.INTERNAL_ERROR, "添加错误信息失败: " + e.getMessage());
        }
    }


    /**
     * 生成错误的唯一键值
     */
    private String generateErrorKey(Error error) {
        return String.format("%s:%s:%s:%s",
                error.getProjectId(),
                error.getType(),
                error.getEnv(),
                error.getPlatform());
    }

    /**
     * 广播新错误给WebSocket客户端
     */
    private void broadcastNewErrors(List<Error> errors) {
        try {
            // 创建 Result 对象
            Result result = new Result(Code.SUCCESS, errors, "新增错误信息");

            // 使用统一的WebSocket处理器发送错误信息
            webSocketHandler.sendMessageByType("error", result);
        } catch (Exception e) {
            log.error("广播错误信息失败", e);
        }
    }


    @Override
    public Result selectByEnvProjectModule(String env, String projectId, Long moduleId) {
        if (env == null || env.isEmpty() || projectId == null || projectId.isEmpty()) {
            log.error("查询错误信息失败，参数为空");
            return new Result(Code.BAD_REQUEST, "查询错误信息失败，参数为空");
        }
        // 判断项目id是否存在
        Project project = projectMapper.selectOne(new LambdaQueryWrapper<Project>()
                .eq(Project::getUuid, projectId));
        if (project == null) {
            // 不存在
            log.error("查询错误信息失败，项目id不存在");
            return new Result(Code.NOT_FOUND, "查询错误信息失败，项目id不存在");
        }
        // 项目id存在
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
            log.error("查询错误信息失败，环境: {}, 项目id: {}, 模块id: {}", env, projectId, moduleId, e);
            return new Result(Code.INTERNAL_ERROR, "查询错误信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result selectById(Long id) {
        if (id == null) {
            log.error("查询错误信息失败，参数为空");
            return new Result(Code.BAD_REQUEST, "查询错误信息失败，参数为空");
        }
        try {
            Error error = errorMapper.selectById(id);
            log.info("成功查询错误: {}", error);
            return new Result(Code.SUCCESS, error, "查询成功");
        } catch (Exception e) {
            log.error("查询错误失败，参数: {}", id, e);
            return new Result(Code.INTERNAL_ERROR, "查询错误失败: " + e.getMessage());
        }
    }


}
