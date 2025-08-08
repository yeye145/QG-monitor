package com.qg.repository;

import cn.hutool.json.JSONUtil;
import com.qg.domain.BackendLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class LogINFORepository {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOG_KEY = "log:backend:info";


    public void checkLogRepeat(String logJSON) {
        // TODO: 转换数据
        List<BackendLog> logs = JSONUtil.toList(logJSON, BackendLog.class);

        logs.forEach(System.err::println);

        for (BackendLog log : logs) {
            // TODO: 生成唯一键（log:backend + project-token + ip +context）
            String redisKey = String.format("%s:%s:%s:%s", LOG_KEY,
                    log.getProjectId(), log.getEnvironmentSnapshot().getIp(), log.getContext());

            System.err.println("===>redisKey: " + redisKey);
//            // 检查 Redis 中是否已存在该日志
//            Long firstLogId = stringRedisTemplate.opsForValue().increment(redisKey, 1);
//            if (firstLogId == 1) {
//                // 首次出现：插入数据库，event=1
//                BackendLog savedLog = logRepository.save(convertToEntity(log));
//                // 设置 Redis 键的 TTL 为 10分钟
//                stringRedisTemplate.expire(redisKey, 10, TimeUnit.MINUTES);
//                // 将 Value 替换为数据库ID（方便后续扩展）
//                stringRedisTemplate.opsForValue().set(redisKey, savedLog.getId().toString());
//            } else {
//                // 重复日志：仅递增 Redis 计数，不操作数据库
//                // event 字段保持为1（首次插入时的值）
//            }
        }
    }

}
