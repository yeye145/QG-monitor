package com.qg.repository;

import com.qg.domain.BackendError;
import com.qg.domain.FrontendError;
import com.qg.domain.MobileError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.qg.repository.RepositoryConstants.FIXED_RATE_DEFAULT;

@Repository
@Slf4j
public abstract class StatisticsDataRepository<T> {

    // 统计数据脚本
    private static final String STATISTICS_SCRIPT = """
            local current = redis.call('GET', KEYS[1])
            if current then
                return redis.call('INCR', KEYS[1])
            else
                redis.call('SET', KEYS[1], 1, 'EX', ARGV[1])
                return 1
            end
            """;

    // 批量检查脚本
    private static final DefaultRedisScript<List> BATCH_CHECK_SCRIPT = new DefaultRedisScript<>(
            """
                    local result = {}
                    for i, key in ipairs(KEYS) do
                        result[i] = redis.call('EXISTS', key) == 0 and 1 or 0
                    end
                    return result
                    """,
            List.class
    );

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    protected final ConcurrentHashMap<String, T> cacheMap = new ConcurrentHashMap<>();

    protected abstract long getTtlMinutes();

    protected abstract void saveToDatabase(T entity);

    protected abstract String generateUniqueKey(T entity);

    protected abstract void incrementEvent(T entity);

    /**
     * 统计并缓存数据
     */
    public void statistics(T entity) {
        // 获取前缀
        String key = generateUniqueKey(entity);

        // 执行lua脚本
        stringRedisTemplate.execute(
                new DefaultRedisScript<>(STATISTICS_SCRIPT, Long.class),
                Collections.singletonList(key),
                String.valueOf(TimeUnit.MINUTES.toSeconds(getTtlMinutes()))
        );

        // 更新内存缓存
        T cached = cacheMap.computeIfAbsent(key, k -> entity);
        incrementEvent(cached);

        if (entity instanceof MobileError) {
            MobileErrorFatherRepository repository = (MobileErrorFatherRepository) this;
            repository.sendWechatAlert((MobileError) cached);
        }
        if (entity instanceof FrontendError) {
            FrontendErrorFatherRepository repository = (FrontendErrorFatherRepository) this;
            repository.sendWechatAlert((FrontendError) cached);
        }
        if (entity instanceof BackendError) {
            BackendErrorFatherRepository repository = (BackendErrorFatherRepository) this;
            repository.sendWechatAlert((BackendError) cached);
        }

    }


    /**
     * 定时将缓存中的数据批量存入数据库
     */
    @Scheduled(fixedRate = FIXED_RATE_DEFAULT)
    public void scheduleSaveToDatabase() {

        if (cacheMap.isEmpty()) return;

        List<String> keys = new ArrayList<>(cacheMap.keySet());
        List<T> entities = new ArrayList<>(cacheMap.values());

        // 批量检查哪些key已过期
        List<Long> checkResults = stringRedisTemplate.execute(
                BATCH_CHECK_SCRIPT,
                keys
        );

        if (checkResults.isEmpty()) {
            return;
        }

        // 处理需要保存的数据
        for (int i = 0; i < checkResults.size(); i++) {
            if (checkResults.get(i) == 1) {
                try {
                    saveToDatabase(entities.get(i));
                    cacheMap.remove(keys.get(i));
                } catch (Exception e) {
                    log.error("批量插入数据到数据库失败：{}", e.getMessage());
                }
            }
        }
    }

}