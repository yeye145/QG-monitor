package com.qg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.domain.BackendError;
import com.qg.domain.FrontendError;
import com.qg.mapper.BackendErrorMapper;
import com.qg.mapper.FrontendErrorMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: // 类说明
 * @ClassName: BackendErrorSqlTest    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/11 15:36   // 时间
 * @Version: 1.0     // 版本
 */
@SpringBootTest
public class BackendErrorSqlTest {
    @Autowired
    private BackendErrorMapper backendErrorMapper;

    @Autowired
    private FrontendErrorMapper frontendErrorMapper;

    @Test
    public void testSql() {
        LambdaQueryWrapper<BackendError> queryWrapper = new LambdaQueryWrapper<>();
        List<BackendError> backendError = backendErrorMapper.selectList(queryWrapper);
        for (BackendError error : backendError) {
            if (error != null){
                System.out.println(error);
                System.out.println(error.getEnvironmentSnapshot());
            }else {
                System.out.println("error空");
            }
        }
    }

    @Test
    public void testSaveAndRead() {
        // 构造测试数据
        BackendError error = new BackendError();
        error.setProjectId("1");
        error.setEnvironment("test");
        // 设置 environmentSnapshot
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("os", "Windows");
        snapshot.put("jdk666", "21");
        error.setEnvironmentSnapshot(snapshot);
        // 保存
        backendErrorMapper.insert(error);
        System.out.println("保存的ID: " + error.getId()); // 输出保存后的ID
        // 读取
        BackendError saved = backendErrorMapper.selectById(error.getId());
        System.out.println("快照内容: " + saved.getEnvironmentSnapshot()); // 应输出 {os=Windows, jdk=21}
    }



}
