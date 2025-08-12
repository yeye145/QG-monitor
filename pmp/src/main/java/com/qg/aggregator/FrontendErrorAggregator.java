package com.qg.aggregator;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.FrontendError;
import com.qg.domain.Project;
import com.qg.mapper.FrontendErrorMapper;
import com.qg.mapper.ProjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Service
@Slf4j
public class FrontendErrorAggregator {

    // 使用 Redis 的 key 前缀
    private static final String ERROR_CACHE_KEY_PREFIX = "frontend_error:";
    private static final String BATCH_COUNTER_KEY = "frontend_error_batch_counter";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private ProjectMapper projectMapper;

    // 添加线程池用于延迟处理
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    // 用于跟踪每个key的调度任务
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 添加错误信息到 Redis 缓存中，并触发延迟处理
     */
    public void addErrorToCache(FrontendError frontendError) {
        // 构建 Redis key，不包含 module 字段
        String key = generateRedisKey(frontendError.getProjectId(),
                frontendError.getSessionId());

        // 使用错误类型作为 field
        String field = frontendError.getErrorType();

        // 将整个 FrontendError 对象序列化为 JSON 字符串
        String errorJson;

        // 使用 Redis 的 Hash 数据结构
        // 如果该类型已存在，则更新计数并保留最早的时间戳
        String existingErrorJson = (String) stringRedisTemplate.opsForHash().get(key, field);

        if (existingErrorJson != null) {
            // 如果已存在该类型的错误，合并处理
            FrontendError existingError = JSONUtil.toBean(existingErrorJson, FrontendError.class);
            existingError.setEvent(existingError.getEvent() + 1); // 增加计数

            // 保留最早的时间戳（这里使用 timestamp 字段）
            if (frontendError.getTimestamp().isBefore(existingError.getTimestamp())) {
                existingError.setTimestamp(frontendError.getTimestamp());
                existingError.setStack(frontendError.getStack());
                // 可以根据需要更新其他字段
            }

            errorJson = JSONUtil.toJsonStr(existingError);
        } else {
            // 第一次出现该类型错误，初始化计数为1
            frontendError.setEvent(1);
            errorJson = JSONUtil.toJsonStr(frontendError);

            // 如果是新类型错误，且当前没有调度任务，则启动新的调度任务
            scheduleAggregation(key);
        }

        // 存储到 Redis
        stringRedisTemplate.opsForHash().put(key, field, errorJson);

        // 设置过期时间，防止内存泄漏
        stringRedisTemplate.expire(key, 60, TimeUnit.MINUTES);
    }

    /**
     * 为指定key安排聚合任务
     * @param key Redis key
     */
    private void scheduleAggregation(String key) {
        // 取消已存在的任务（如果有的话）
        ScheduledFuture<?> existingTask = scheduledTasks.get(key);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
        }

        // 安排新的任务
        ScheduledFuture<?> newTask = scheduler.schedule(() -> {
            try {
                processAndSaveErrorsForKey(key);
            } catch (Exception e) {
                log.error("处理前端错误信息时发生异常，key: {}", key, e);
            } finally {
                // 任务完成后从映射中移除
                scheduledTasks.remove(key);
            }
        }, 10, TimeUnit.MINUTES);

        // 记录新任务
        scheduledTasks.put(key, newTask);
    }

    /**
     * 处理并保存指定key的错误信息
     * @param key Redis key
     */
    private void processAndSaveErrorsForKey(String key) {
        log.info("开始处理并保存前端错误信息，key: {}", key);

        try {
            // 从key中提取projectId
            String projectId = extractProjectIdFromKey(key);

            // 检查项目是否存在
            if (!isProjectExists(projectId)) {
                log.warn("项目不存在，跳过处理并删除缓存数据，项目ID: {}", projectId);
                // 删除不存在项目的缓存数据
                stringRedisTemplate.delete(key);
                return;
            }

            // 获取 key 对应的所有 field 和 value
            Map<Object, Object> errorMap = stringRedisTemplate.opsForHash().entries(key);

            if (errorMap.isEmpty()) {
                return;
            }

            // 处理并保存数据
            List<FrontendError> errorsToSave = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : errorMap.entrySet()) {
                String errorJson = (String) entry.getValue();
                FrontendError error = JSONUtil.toBean(errorJson, FrontendError.class);
                errorsToSave.add(error);
            }

            // 批量保存到数据库
            if (!errorsToSave.isEmpty()) {
                // 获取批次号
                Long batchId = stringRedisTemplate.opsForValue().increment(BATCH_COUNTER_KEY, 1);

                for (FrontendError error : errorsToSave) {
                    frontendErrorMapper.insert(error);
                }
                log.info("保存了 {} 条前端错误信息，批次ID: {}，key: {}", errorsToSave.size(), batchId, key);
            }

            // 删除已处理的 key
            stringRedisTemplate.delete(key);

        } catch (Exception e) {
            log.error("处理前端错误信息时发生异常，key: {}", key, e);
        }

        log.info("前端错误信息处理并保存完成，key: {}", key);
    }

    private String generateRedisKey(String projectId, String sessionId) {
        return ERROR_CACHE_KEY_PREFIX + projectId + ":" + sessionId;
    }

    /**
     * 从Redis key中提取项目ID
     * @param key Redis key
     * @return 项目ID
     */
    private String extractProjectIdFromKey(String key) {
        // key格式: frontend_error:projectId:sessionId
        String[] parts = key.split(":");
        if (parts.length >= 3) {
            return parts[1]; // 第二个部分是projectId
        }
        return null;
    }

    /**
     * 检查项目是否存在
     * @param projectId 项目ID
     * @return 项目是否存在
     */
    private boolean isProjectExists(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            return false;
        }

        try {
            LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Project::getUuid, projectId).eq(Project::getIsDeleted, false);
            return projectMapper.selectCount(queryWrapper) > 0;
        } catch (Exception e) {
            log.error("检查项目存在性时发生异常，项目ID: {}", projectId, e);
            return false;
        }
    }

    /**
     * 关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
