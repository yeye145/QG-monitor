package com.qg.collections;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.*;
import com.qg.mapper.*;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: 定时上传数据  // 类说明
 * @ClassName: MilvusUpload    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/14 16:24   // 时间
 * @Version: 1.0     // 版本
 */
@Component
public class MilvusUpload {

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

    @Scheduled(initialDelay = 1000 * 60 * 60, fixedRate = 1000 * 60 * 60) //启动1小时后开始执行，每小时执行一次
    public void uploadData()  {
        // 1. 初始化 Milvus 客户端
        MilvusServiceClient client = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withDatabaseName("PMPdb")
                        .withHost("47.113.224.195")
                        .withPort(34530)
                        .build()
        );

        // 2. 初始化向量化服务（示例用 Dummy）
        EmbeddingService embeddingService = new DummyEmbeddingService();

        // 3. 初始化各个插入服务
        BackendErrorMilvus backendErrorMilvus = new BackendErrorMilvus(client, embeddingService);
        BackendPerformanceMilvus backendPerformanceMilvus = new BackendPerformanceMilvus(client, embeddingService);
        FrontendErrorMilvus frontendErrorMilvus = new FrontendErrorMilvus(client, embeddingService);
        FrontendPerformanceMilvus frontendPerformanceMilvus = new FrontendPerformanceMilvus(client, embeddingService);
        MobileErrorMilvus mobileErrorMilvus = new MobileErrorMilvus(client, embeddingService);
        MobilePerformanceMilvus mobilePerformanceMilvus = new MobilePerformanceMilvus(client, embeddingService);

        LocalDateTime passHour = LocalDateTime.now().minusHours(1);

        // 4. 上传后端数据
        List<BackendError> backendErrors = backendErrorMapper.selectList(new LambdaQueryWrapper<BackendError>().ge(BackendError::getTimestamp, passHour));
        List<BackendPerformance> backendPerformances = backendPerformanceMapper.selectList(new LambdaQueryWrapper<BackendPerformance>().ge(BackendPerformance::getTimestamp, passHour));

        // 5. 上传前端数据
        List<FrontendError> frontendErrors = frontendErrorMapper.selectList(new LambdaQueryWrapper<FrontendError>().ge(FrontendError::getTimestamp, passHour));
        List<FrontendPerformance> frontendPerformances = frontendPerformanceMapper.selectList(new LambdaQueryWrapper<FrontendPerformance>().ge(FrontendPerformance::getTimestamp, passHour));

        // 6. 上传移动端数据
        List<MobileError> mobileErrors = mobileErrorMapper.selectList(new LambdaQueryWrapper<MobileError>().ge(MobileError::getTimestamp, passHour));
        List<MobilePerformance> mobilePerformances = mobilePerformanceMapper.selectList(new LambdaQueryWrapper<MobilePerformance>().ge(MobilePerformance::getTimestamp, passHour));

        // 7. 批量插入后端错误数据
        backendErrors.forEach(error -> {
            try {
                backendErrorMilvus.saveBackendErrorToMilvus(error);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 8. 批量插入后端性能数据
        backendPerformances.forEach(performance -> {
            try {
                backendPerformanceMilvus.saveBackendPerformanceToMilvus(performance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 9. 批量插入前端错误数据
        frontendErrors.forEach(error -> {
            try {
                frontendErrorMilvus.saveFrontendErrorToMilvus(error);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 10. 批量插入前端性能数据
        frontendPerformances.forEach(performance -> {
            try {
                frontendPerformanceMilvus.saveFrontendPerformanceToMilvus(performance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 11. 批量插入移动端错误数据
        mobileErrors.forEach(error -> {
            try {
                mobileErrorMilvus.saveMobileErrorToMilvus(error);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 12. 批量插入移动端性能数据
        mobilePerformances.forEach(performance -> {
            try {
                mobilePerformanceMilvus.saveMobilePerformanceToMilvus(performance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
