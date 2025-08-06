package com.qg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.qg.utils.RedisConstants.ERROR_REPEAT_KEY;
import static com.qg.utils.RedisConstants.MAX_ERROR_TIME;


@Slf4j
@Service
public class ErrorServiceImpl implements ErrorService {

    @Autowired
    private ErrorMapper errorMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
            log.debug("开始批量保存，数据量: {}", errorList.size());
            List<Error> nonDuplicateErrors = new ArrayList<>();
            List<Error> broadcastErrors = new ArrayList<>(); // 用于广播的新错误

            for (Error error : errorList) {
                if (error == null) continue;
                String errorKey = generateErrorKey(error);
                String redisKey = ERROR_REPEAT_KEY + errorKey;

                // 检查 Redis 中是否存在（10分钟过期）
                Boolean hasKey = stringRedisTemplate.hasKey(redisKey);
                if (Boolean.TRUE.equals(hasKey)) {
                    log.debug("跳过重复错误: {}", error);
                    continue;
                }
                // 记录到 Redis（10分钟过期）
                stringRedisTemplate.opsForValue().set(
                        redisKey, error.getMessage(), Duration.ofMinutes(MAX_ERROR_TIME));
                nonDuplicateErrors.add(error);
                broadcastErrors.add(error); // 记录需要广播的错误
            }

            // 批量插入非重复错误
            int successCount = 0;
            for (Error error : nonDuplicateErrors) {
                successCount += errorMapper.insert(error);
            }

            // 向WebSocket客户端广播新错误
            if (!broadcastErrors.isEmpty()) {
                broadcastNewErrors(broadcastErrors);
            }

            log.info("添加错误信息完成，总数量: {}，新增: {} 条，重复: {} 条",
                    errorList.size(), successCount, errorList.size() - successCount);
            return new Result(Code.SUCCESS, "添加错误信息成功");
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
