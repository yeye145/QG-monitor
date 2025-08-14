package com.qg.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.MobileError;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.InsertParam;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 移动错误存入向量库  // 类说明
 * @ClassName: MobileErrorMilvus    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 15:53   // 时间
 * @Version: 1.0     // 版本
 */
public class MobileErrorMilvus {

    private final MilvusServiceClient client;
    private final EmbeddingService embeddingService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MobileErrorMilvus(MilvusServiceClient client, EmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }

    public void saveMobileErrorToMilvus(MobileError error) throws Exception {
        // 1. timestamp 转字符串
        String timestampStr = error.getTimestamp().format(dtf);

        // 2. stack 转向量
        List<Float> stackVector = embeddingService.getEmbedding(error.getStack());


        // 4. 按 Milvus schema 准备数据
        List<Long> ids = Arrays.asList(error.getId());
        List<String> messages = Arrays.asList(error.getMessage());
        List<String> className = Arrays.asList(error.getClassName());
        List<String> timestamps = Arrays.asList(timestampStr);
        List<String> projectIds = Arrays.asList(error.getProjectId());
        List<String> errorTypes = Arrays.asList(error.getErrorType());
        List<List<Float>> stackVectors = Arrays.asList(stackVector);
        List<Long> events = Arrays.asList((long) error.getEvent());
        List<String> stack = Arrays.asList(error.getStack());


         // 5. 插入 Milvus
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("mobile_error")
                .withFields(Arrays.asList(
                        new InsertParam.Field("id", ids),
                        new InsertParam.Field("timestamp", timestamps),
                        new InsertParam.Field("className", className),
                        new InsertParam.Field("event", events),
                        new InsertParam.Field("projectId", projectIds),
                        new InsertParam.Field("message", messages),
                        new InsertParam.Field("errorType", errorTypes),
                        new InsertParam.Field("stack_vector", stackVectors),
                        new InsertParam.Field("stack", stack)
                ))
                .build();

        client.insert(insertParam);
    }
}
