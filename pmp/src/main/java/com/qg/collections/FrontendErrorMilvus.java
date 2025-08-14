package com.qg.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.domain.FrontendError;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.dml.InsertParam;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 前端错误存向量数据库  // 类说明
 * @ClassName: FrontendErrorMilvus    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 15:05   // 时间
 * @Version: 1.0     // 版本
 */
public class FrontendErrorMilvus {

    private final MilvusServiceClient client;
    private final EmbeddingService embeddingService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FrontendErrorMilvus(MilvusServiceClient client, EmbeddingService embeddingService) {
        this.client = client;
        this.embeddingService = embeddingService;
    }


    public void saveFrontendErrorToMilvus(FrontendError error) throws Exception {
        // 1. timestamp 转字符串
        String timestampStr = error.getTimestamp().format(dtf);

        // 2. stack 转向量
        List<Float> stackVector = embeddingService.getEmbedding(error.getStack());

        // 3.  转 JSON
        String request = objectMapper.writeValueAsString(error.getRequest());
        String response = objectMapper.writeValueAsString(error.getResponse());
        String breadcrumbs = objectMapper.writeValueAsString(error.getBreadcrumbs());
        String elementInfo = objectMapper.writeValueAsString(error.getElementInfo());
        String resource = objectMapper.writeValueAsString(error.getResource());
        String tag = objectMapper.writeValueAsString(error.getTags());


        // 4. 按 Milvus schema 准备数据
        List<Long> ids = Arrays.asList(error.getId());
        List<String> messages = Arrays.asList(error.getMessage());
        List<String> breadcrumbsList = Arrays.asList(breadcrumbs);
        List<String> elementInfoList = Arrays.asList(elementInfo);
        List<String> resourceList = Arrays.asList(resource);
        List<String> tagList = Arrays.asList(tag);
        List<String> requests = Arrays.asList(request);
        List<String> responses = Arrays.asList(response);
        List<String> timestamps = Arrays.asList(timestampStr);
        List<String> projectIds = Arrays.asList(error.getProjectId());
        List<String> errorTypes = Arrays.asList(error.getErrorType());
        List<List<Float>> stackVectors = Arrays.asList(stackVector);
        List<String> captureTypes = Arrays.asList(error.getCaptureType());
        List<Long> durations = Arrays.asList(error.getDuration());
        List<String> sessionIds = Arrays.asList(error.getSessionId());
        List<String> userAgents = Arrays.asList(error.getUserAgent());
        List<String> stack = Arrays.asList(error.getStack());


        List<Long> events = Arrays.asList(error.getEvent().longValue());

        // 5. 插入 Milvus
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("frontend_error")
                .withFields(Arrays.asList(
                        new InsertParam.Field("id", ids),
                        new InsertParam.Field("timestamp", timestamps),
                        new InsertParam.Field("message", messages),
                        new InsertParam.Field("projectId", projectIds),
                        new InsertParam.Field("request_info", requests),
                        new InsertParam.Field("errorType", errorTypes),
                        new InsertParam.Field("stack_vector", stackVectors),
                        new InsertParam.Field("response_info", responses),
                        new InsertParam.Field("breadcrumbs", breadcrumbsList),
                        new InsertParam.Field("elementInfo", elementInfoList),
                        new InsertParam.Field("resource_info", resourceList),
                        new InsertParam.Field("captureType", captureTypes),
                        new InsertParam.Field("duration", durations),
                        new InsertParam.Field("userAgent", userAgents),
                        new InsertParam.Field("sessionId", sessionIds),
                        new InsertParam.Field("tags", tagList),
                        new InsertParam.Field("event", events),
                        new InsertParam.Field("stack", stack)
                ))
                .build();

        client.insert(insertParam);
    }

}
