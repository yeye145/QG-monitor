package com.qg.service.impl;

import com.qg.domain.Code;
import com.qg.domain.Performance;
import com.qg.domain.Result;
import com.qg.mapper.PerformanceMapper;
import com.qg.service.PerformanceService;
import com.qg.websocket.UnifiedWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PerformanceServiceImpl implements PerformanceService {
    @Autowired
    private PerformanceMapper performanceMapper;

    @Autowired
    private UnifiedWebSocketHandler webSocketHandler;

    public Result addPerformance(List<Performance> performance) {
        log.debug("添加性能信息: {}", performance);
        if (performance == null || performance.isEmpty()) {
            log.error("添加性能信息失败，性能信息为空");
            return new Result(Code.BAD_REQUEST, "添加性能信息失败，性能信息为空");
        }
        try {
            log.debug("开始批量保存，数据量: {}", performance.size());
            int result = 0;
            for(Performance performance1 : performance)
            {
                performanceMapper.insert(performance1);
                result++;
            }
            log.info("添加性能信息成功，共处理 {} 条记录", result);
            if(result != performance.size())
            {
                log.warn("添加性能信息成功，但有 {} 条记录处理失败", performance.size() - result);
                return new Result(Code.INTERNAL_ERROR, "有 " + (performance.size() - result) + " 条记录处理失败");
            }
            return new Result(Code.SUCCESS, "添加性能信息成功");
        } catch (Exception e) {
            log.error("添加性能信息失败", e);
            return new Result(Code.INTERNAL_ERROR, "添加性能信息失败");
        }
    }

    /**
     * 广播新性能给WebSocket客户端
     */
    private void broadcastNewPerformances(List<Performance> performances) {
        try {
            // 创建 Result 对象
            Result result = new Result(Code.SUCCESS, performances, "新增错误信息");

            // 使用统一的WebSocket处理器发送错误信息
            webSocketHandler.sendMessageByType("performance", result);
        } catch (Exception e) {
            log.error("广播错误信息失败", e);
        }
    }
}
