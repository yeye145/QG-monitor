package com.qg.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.MobilePerformance;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.InsertParam;

import java.lang.reflect.Array;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 移动性能存入向量数据库  // 类说明
 * @ClassName: MobilePerformanceMilvus    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 16:11   // 时间
 * @Version: 1.0     // 版本
 */
public class MobilePerformanceMilvus {

    private final MilvusServiceClient client;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MobilePerformanceMilvus(MilvusServiceClient client, EmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }


    public void saveMobilePerformanceToMilvus(MobilePerformance performance) throws Exception {
        // 1. timestamp 转字符串
        String timestampStr = performance.getTimestamp().format(dtf);

        // 2.  转向量

        List<Float> memoryUsage = embeddingService.getEmbedding(performance.getMemoryUsage()==null ? "" : performance.getMemoryUsage().toString());

        // 3. 按 Milvus schema 准备数据
        List<Long> ids = Arrays.asList(performance.getId());
        List<String> timestamps = Arrays.asList(timestampStr);
        List<Long> events = Arrays.asList(performance.getEvent().longValue());
        List<String> projectIds = Arrays.asList(performance.getProjectId());
        List<String> deviceModel = Arrays.asList(performance.getDeviceModel());
        List<String> osVersion = Arrays.asList(performance.getOsVersion());
        List<String> batteryLevel = Arrays.asList(performance.getBatteryLevel());
        List<List<Float>> memoryUsageList = Arrays.asList(memoryUsage);
        List<Long>operationFps = Arrays.asList(performance.getOperationFps());
        List<Long> apiTime = Arrays.asList(performance.getApiTime()== null ? 0L : performance.getApiTime());
        List<String> apiName = Arrays.asList(performance.getApiName());
        List<String> operationIds = Arrays.asList(performance.getOperationId());
        List<String> memoryUsageStr = Arrays.asList(performance.getMemoryUsage() == null ? "" : performance.getMemoryUsage().toString());


        // 4. 插入 Milvus
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("mobile_performance")
                .withFields(Arrays.asList(
                        new InsertParam.Field("id", ids),
                        new InsertParam.Field("timestamp", timestamps),
                        new InsertParam.Field("projectId", projectIds),
                        new InsertParam.Field("event", events),
                        new InsertParam.Field("deviceModel", deviceModel),
                        new InsertParam.Field("osVersion", osVersion),
                        new InsertParam.Field("batteryLevel", batteryLevel),
                        new InsertParam.Field("memoryUsage_vector", memoryUsageList),
                        new InsertParam.Field("operationFps", operationFps),
                        new InsertParam.Field("apiTime", apiTime),
                        new InsertParam.Field("apiName", apiName),
                        new InsertParam.Field("operationId", operationIds),
                        new InsertParam.Field("memoryUsage", memoryUsageStr)

                ))
                .build();

        client.insert(insertParam);
    }
}
