package com.qg.repository;

import com.qg.domain.MethodInvocation;
import com.qg.mapper.MethodInvocationMapper;
import com.qg.vo.MethodInvocationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;



@Repository
@Slf4j
public class MethodInvocationRepository extends StatisticsDataRepository<MethodInvocation> {

    private static final String REDIS_KEY_PREFIX = "backend:method:";

    @Autowired
    private MethodInvocationMapper methodInvocationMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void statisticsMethod(Map<String, Integer> methodMap) {
        // 批量更新Redis
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            methodMap.forEach((fullKey, count) -> {
                String redisKey = REDIS_KEY_PREFIX + fullKey;
                connection.stringCommands().incrBy(redisKey.getBytes(), count);
                // 设置过期时间
                connection.keyCommands().expire(redisKey.getBytes(), Duration.ofSeconds(60));
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
        return 1;
    }

    @Override
    protected void saveToDatabase(MethodInvocation entity) {
        try {
            MethodInvocationVO methodInvocationVO = convertToVO(entity);
            methodInvocationMapper.insert(methodInvocationVO);
        } catch (Exception e) {
            System.err.println("\n\n\n方法调用统计失败\n" + e + "\n\n\n");
            e.printStackTrace();
            System.out.println("\n\n\n");
//            log.error("方法调用统计失败,项目ID: {}: {}", entity.getProjectId(), e.getMessage());
        }
    }

    @Override
    protected String generateUniqueKey(MethodInvocation entity) {
        return REDIS_KEY_PREFIX + entity.getProjectId() + ":" + entity.getMethodName();
    }

    @Override
    protected void incrementEvent(MethodInvocation entity) {
    }

    @Override
    @Scheduled(fixedRate = 6000)
    public void scheduleSaveToDatabase() {
        cacheMap.forEach((fullKey, entity) -> {
            String redisKey = REDIS_KEY_PREFIX + fullKey;
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