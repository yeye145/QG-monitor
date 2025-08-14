package com.qg.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.BackendError;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.InsertParam;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 后端错误存向量数据库  // 类说明
 * @ClassName: BackendErrorMilus    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 11:49   // 时间
 * @Version: 1.0     // 版本
 */
public class BackendErrorMilvus {
    private final MilvusServiceClient client;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BackendErrorMilvus(MilvusServiceClient client, EmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }

    public void saveBackendErrorToMilvus(BackendError error) throws Exception {

        // 1. timestamp 转字符串
        String timestampStr = error.getTimestamp().format(dtf);

        // 2. stack 转向量
        List<Float> stackVector = embeddingService.getEmbedding(error.getStack());

        // 3. environmentSnapshot 转 JSON
        String envSnapshotJson = objectMapper.writeValueAsString(error.getEnvironmentSnapshot());

        // 4. 按 Milvus schema 准备数据
        List<Long> ids = Arrays.asList(error.getId());
        List<String> timestamps = Arrays.asList(timestampStr);
        List<String> modules = Arrays.asList(error.getModule());
        List<String> projectIds = Arrays.asList(error.getProjectId());
        List<String> environments = Arrays.asList(error.getEnvironment());
        List<String> errorTypes = Arrays.asList(error.getErrorType());
        List<List<Float>> stackVectors = Arrays.asList(stackVector);
        List<String> envSnapshots = Arrays.asList(envSnapshotJson);
        List<Long> events = Arrays.asList(error.getEvent().longValue());
        List<String> stack = Arrays.asList(error.getStack());

        // 5. 插入 Milvus
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("backend_error")
                .withFields(Arrays.asList(
                        new InsertParam.Field("id", ids),
                        new InsertParam.Field("timestamp", timestamps),
                        new InsertParam.Field("module", modules),
                        new InsertParam.Field("projectId", projectIds),
                        new InsertParam.Field("environment", environments),
                        new InsertParam.Field("errorType", errorTypes),
                        new InsertParam.Field("stack_vector", stackVectors),
                        new InsertParam.Field("stack", stack),
                        new InsertParam.Field("environmentSnapshot", envSnapshots),
                        new InsertParam.Field("event", events)
                ))
                .build();

        client.insert(insertParam);
    }
}
