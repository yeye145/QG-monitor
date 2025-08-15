package com.qg;

import com.qg.collections.*;
import com.qg.domain.*;
import com.qg.mapper.*;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Description: // 类说明
 * @ClassName: MilvusTest    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 11:55   // 时间
 * @Version: 1.0     // 版本
 */
@SpringBootTest
public class MilvusTest {

    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private BackendPerformanceMapper backendPerformanceMapper;

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Autowired
    private FrontendPerformanceMapper frontendPerformanceMapper;

    @Autowired
    private MobileErrorMapper mobileErrorMapper;

    @Autowired
    private MobilePerformanceMapper mobilePerformanceMapper;

    @Test
    public void test1() throws Exception {
        // 1. 初始化 Milvus 客户端
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withDatabaseName("default")
                        .withHost("47.113.224.195")
                        .withPort(34530)
                        .build()
        );

        // 2. 初始化向量化服务（示例用 Dummy）
        EmbeddingService embeddingService = new DummyEmbeddingService();

        // 3. 初始化插入服务
        BackendErrorMilvus milvusService = new BackendErrorMilvus(client, embeddingService);


        // 6. 批量插入示例
        List<BackendError> errorList = backendErrorMapper.selectList(null);
        for (BackendError e : errorList) {
            milvusService.saveBackendErrorToMilvus(e);
        }
    }
    @Test
    public void test2() throws Exception {
        // 1. 初始化 Milvus 客户端
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withDatabaseName("default")
                        .withHost("47.113.224.195")
                        .withPort(34530)
                        .build()
        );

        // 2. 初始化向量化服务（示例用 Dummy）
        EmbeddingService embeddingService = new DummyEmbeddingService();

        // 3. 初始化插入服务
        BackendPerformanceMilvus milvusService = new BackendPerformanceMilvus(client, embeddingService);

        // 4. 批量插入示例
        List<BackendPerformance> performances = backendPerformanceMapper.selectList(null);
        for (BackendPerformance e : performances) {
            milvusService.saveBackendPerformanceToMilvus(e);
        }
    }
    @Test
    public void test3() throws Exception {
        // 1. 初始化 Milvus 客户端
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withDatabaseName("default")
                        .withHost("47.113.224.195")
                        .withPort(34530)
                        .build()
        );
        // 2. 初始化向量化服务（示例用 Dummy）
        EmbeddingService embeddingService = new DummyEmbeddingService();

        // 3. 初始化插入服务
        FrontendErrorMilvus milvusService = new FrontendErrorMilvus(client, embeddingService);

        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(null);
        for (FrontendError e : frontendErrors) {
            milvusService.saveFrontendErrorToMilvus(e);
        }

    }

    @Test
    public void test4() throws Exception {
        // 1. 初始化 Milvus 客户端
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withDatabaseName("default")
                        .withHost("47.113.224.195")
                        .withPort(34530)
                        .build()
        );
        // 2. 初始化向量化服务（示例用 Dummy）
        EmbeddingService embeddingService = new DummyEmbeddingService();

        // 3. 初始化插入服务
        FrontendPerformanceMilvus milvusService = new FrontendPerformanceMilvus(client, embeddingService);

        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(null);

        for (FrontendPerformance e : frontendPerformances) {
            milvusService.saveFrontendPerformanceToMilvus(e);
        }
    }

    @Test
    public void test5() throws Exception {
        // 1. 初始化 Milvus 客户端
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withDatabaseName("default")
                        .withHost("47.113.224.195")
                        .withPort(34530)
                        .build()
        );

        EmbeddingService embeddingService = new DummyEmbeddingService();

        List<MobileError> mobileErrors = mobileErrorMapper.selectList(null);

        MobileErrorMilvus mobileErrorMilvus = new MobileErrorMilvus(client, embeddingService);

        for (MobileError e : mobileErrors) {
            mobileErrorMilvus.saveMobileErrorToMilvus(e);
        }
    }

    @Test
    public void test6() throws Exception {
        // 1. 初始化 Milvus 客户端
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withDatabaseName("default")
                        .withHost("47.113.224.195")
                        .withPort(34530)
                        .build()
        );

        EmbeddingService embeddingService = new DummyEmbeddingService();

        List<MobilePerformance> mobileErrors = mobilePerformanceMapper.selectList(null);

        MobilePerformanceMilvus mobilePerformanceMilvus = new MobilePerformanceMilvus(client, embeddingService);

        for (MobilePerformance e : mobileErrors) {
            mobilePerformanceMilvus.saveMobilePerformanceToMilvus(e);
        }
    }

}
