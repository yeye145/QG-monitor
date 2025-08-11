package com.qg;

import com.qg.domain.BackendError;
import com.qg.mapper.BackendErrorMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class test {
    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Test
    public void testSaveAndRead() {
        // 构造测试数据
        BackendError error = new BackendError();
        error.setProjectId("1");
        error.setEnvironment("test");
        // 设置 environmentSnapshot
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("os", "Windows");
        snapshot.put("jdk777", "2771");
        error.setEnvironmentSnapshot(snapshot);
        // 保存
        backendErrorMapper.insert(error);
        // 读取
        BackendError saved = backendErrorMapper.selectById(error.getId());
        System.out.println("快照内容: " + saved.getEnvironmentSnapshot()); // 应输出 {os=Windows, jdk=21}
    }
}
