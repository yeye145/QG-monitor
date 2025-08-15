package com.qg.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.FrontendPerformance;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.InsertParam;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 前端性能存向量库  // 类说明
 * @ClassName: FrontendPerformanceMilvus    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 15:35   // 时间
 * @Version: 1.0     // 版本
 */
public class FrontendPerformanceMilvus {

    private final MilvusServiceClient client;
    private final EmbeddingService embeddingService;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FrontendPerformanceMilvus(MilvusServiceClient client, EmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }

    public void saveFrontendPerformanceToMilvus(FrontendPerformance performance) throws Exception {
        // 1. timestamp 转字符串
        String timestampStr = performance.getTimestamp().format(dtf);

        // 2.  转向量
        List<Float> metrics = embeddingService.getEmbedding(performance.getMetrics().toString());

        // 4. 按 Milvus schema 准备数据
        List<Long> ids = Arrays.asList(performance.getId());
        List<String> timestamps = Arrays.asList(timestampStr);
        List<List<Float>> metricsList = Arrays.asList(metrics);
        List<Long> events = Arrays.asList(performance.getEvent().longValue());
        List<String> sessionIds = Arrays.asList(performance.getSessionId());
        List<String> userAgent= Arrays.asList(performance.getUserAgent());
        List<String> captureType = Arrays.asList(performance.getCaptureType());
        List<String> projectIds = Arrays.asList(performance.getProjectId());
        List<String> metric = Arrays.asList(performance.getMetrics().toString());





        // 5. 插入 Milvus
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("frontend_performance")
                .withFields(Arrays.asList(
                        new InsertParam.Field("id", ids),
                        new InsertParam.Field("timestamp", timestamps),
                        new InsertParam.Field("projectId", projectIds),
                        new InsertParam.Field("sessionId", sessionIds),
                        new InsertParam.Field("userAgent", userAgent),
                        new InsertParam.Field("metrics_vector", metricsList),
                        new InsertParam.Field("metrics", metric),
                        new InsertParam.Field("captureType", captureType),
                        new InsertParam.Field("event", events)
                ))
                .build();

        client.insert(insertParam);
    }
}
