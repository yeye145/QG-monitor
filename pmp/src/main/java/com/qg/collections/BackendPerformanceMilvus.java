package com.qg.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.BackendPerformance;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.InsertParam;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 后端性能存向量数据库  // 类说明
 * @ClassName: BackendPerformanceMilvus    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 14:54   // 时间
 * @Version: 1.0     // 版本
 */
public class BackendPerformanceMilvus {

    private final MilvusServiceClient client;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BackendPerformanceMilvus(MilvusServiceClient client, EmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }

    public void saveBackendPerformanceToMilvus(BackendPerformance performance) throws Exception {
        // 1. timestamp 转字符串
        String timestampStr = performance.getTimestamp().format(dtf);

        // 2. duration 转向量
        List<Float> durationVector = embeddingService.getEmbedding(String.valueOf(performance.getDuration()));


        // 2. environmentSnapshot 转 JSON
        String envSnapshotJson = objectMapper.writeValueAsString(performance.getEnvironmentSnapshot());

        // 3. 按 Milvus schema 准备数据
        List<Long> ids = Arrays.asList(performance.getId());
        List<String> timestamps = Arrays.asList(timestampStr);
        List<String> modules = Arrays.asList(performance.getModule());
        List<String> projectIds = Arrays.asList(performance.getProjectId());
        List<String> environments = Arrays.asList(performance.getEnvironment());
        List<String> apis = Arrays.asList(performance.getApi());
        List<List<Float>> durations = Arrays.asList(durationVector);
        List<Boolean> slows = Arrays.asList(performance.getSlow());
        List<String> envSnapshots = Arrays.asList(envSnapshotJson);
        List<Long> events = Arrays.asList(performance.getEvent().longValue());
        List<Long> duration = Arrays.asList(performance.getDuration() == null ? 0L : performance.getDuration());

        // 4. 插入 Milvus
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("backend_performance")
                .withFields(Arrays.asList(
                        new InsertParam.Field("id", ids),
                        new InsertParam.Field("timestamp", timestamps),
                        new InsertParam.Field("module", modules),
                        new InsertParam.Field("projectId", projectIds),
                        new InsertParam.Field("environment", environments),
                        new InsertParam.Field("api", apis),
                        new InsertParam.Field("duration_vector", durations),
                        new InsertParam.Field("duration", duration),
                        new InsertParam.Field("slow", slows),
                        new InsertParam.Field("environmentSnapshot", envSnapshots),
                        new InsertParam.Field("event", events)
                ))
                .build();

        client.insert(insertParam);
    }
}
