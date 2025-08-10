package com.qg.repository;

import com.qg.domain.MethodInvocation;
import com.qg.mapper.MethodInvocationMapper;
import com.qg.vo.MethodInvocationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static com.qg.repository.RepositoryConstants.*;


@Repository
@Slf4j
public class MethodInvocationRepository extends StatisticsDataRepository<MethodInvocation> {

    @Autowired
    private MethodInvocationMapper methodInvocationMapper;

    /**
     * 统计方法调用
     * @param methodMap
     */
    public void statisticsMethod(Map<String, Integer> methodMap) {
        // 批量更新Redis
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            methodMap.forEach((fullKey, count) -> {
                String redisKey = REDIS_KEY_PREFIX.getAsString() + fullKey;
                connection.stringCommands().incrBy(redisKey.getBytes(), count);
                // 设置过期时间
                connection.keyCommands().expire(redisKey.getBytes(), Duration.ofMinutes(getTtlMinutes()));
            });
            return null;
        });

        // 批量更新内存缓存
        methodMap.forEach((fullKey, count) -> {
            String[] parts = fullKey.split(":", 2);
            String projectId = parts[0];
            String methodName = parts[1];

            cacheMap.compute(fullKey, (k, existing) -> {
                if (existing == null) {
                    MethodInvocation newEntity = new MethodInvocation();
                    newEntity.setMethodName(methodName);
                    newEntity.setProjectId(projectId);
                    newEntity.incrementEvent(count);
                    return newEntity;
                }
                existing.incrementEvent(count);
                return existing;
            });
        });
    }

    @Override
    protected long getTtlMinutes() {
        return TTL_MINUTES.getAsLong();
    }

    @Override
    protected void saveToDatabase(MethodInvocation entity) {
        try {
            MethodInvocationVO methodInvocationVO = convertToVO(entity);
            methodInvocationMapper.insert(methodInvocationVO);
        } catch (Exception e) {
            log.error("方法调用统计失败,项目ID: {}: {}", entity.getProjectId(), e.getMessage());
        }
    }

    @Override
    protected String generateUniqueKey(MethodInvocation entity) {
        return REDIS_KEY_PREFIX.getAsString() + entity.getProjectId() + ":" + entity.getMethodName();
    }

    @Override
    protected void incrementEvent(MethodInvocation entity) {
    }

    /**
     * 重写父类逻辑
     */
    @Override
    @Scheduled(fixedRate = FIXED_RATE_METHOD)
    public void scheduleSaveToDatabase() {
        cacheMap.forEach((fullKey, entity) -> {
            String redisKey = REDIS_KEY_PREFIX.getAsString() + fullKey;
            if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
                // Redis已过期，保存到数据库并清理缓存
                saveToDatabase(entity);
                cacheMap.remove(fullKey);
            }
        });
    }

    /**
     * 将实体转换为VO
     *
     * @param entity
     * @return
     */
    private MethodInvocationVO convertToVO(MethodInvocation entity) {
        MethodInvocationVO vo = new MethodInvocationVO();
        vo.setProjectId(entity.getProjectId());
        vo.setMethodName(entity.getMethodName());
        vo.setEvent(entity.getEventCount());
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }
}