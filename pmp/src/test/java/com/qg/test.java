package com.qg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.debugging.sourcemap.FilePosition;
import com.google.debugging.sourcemap.SourceMapConsumer;
import com.google.debugging.sourcemap.SourceMapConsumerV3;
import com.google.debugging.sourcemap.SourceMapParseException;
import com.google.debugging.sourcemap.proto.Mapping;
import com.qg.domain.BackendError;
import com.qg.mapper.BackendErrorMapper;
import com.qg.mapper.FrontendBehaviorMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class test {
    @Autowired
    private BackendErrorMapper backendErrorMapper;
    @Autowired
    private FrontendBehaviorMapper frontendBehaviorMapper;

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

    @Test
    public void test() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // 读取 Source Map 文件为 Map（value 类型为 Object）
        Map<String, Object> sourceMap = mapper.readValue(
                new File("uploads/documents/5ce99897-596c-4201-83b0-6ca1cb1ad784.map"),
                Map.class
        );

        // 1. 正确处理 version（整数类型）
        // Integer version = (Integer) sourceMap.get("version"); // 直接作为 Integer 读取
        // 或转换为字符串
        String versionStr = String.valueOf(sourceMap.get("version"));

        // 2. 其他字段处理（file 可能为 null 或字符串）
        String file = (String) sourceMap.get("file");
        // 处理可能的 null（避免空指针）
        if (file == null) {
            file = "";
        }

        // 3.  sources 和 sourcesContent 为列表（通常为字符串列表）
        List<String> sources = (List<String>) sourceMap.get("sources");
        Object sourceObj = sourceMap.get("sources");
        List<String> sourcesContent = (List<String>) sourceMap.get("sourcesContent");


//        // 4. mappings 为字符串（Base64 VLQ 编码）
//        String mappings = (String) sourceMap.get("mappings");

        // 打印结果
        System.out.println("Source Map Version: " + versionStr);
        System.out.println("file: " + file);

        // System.out.println("Sources: " + sourceObj);
        System.out.println("Source Map Version (String): " + versionStr);
        //  sources.forEach(System.out::println);
        sourcesContent.forEach(System.out::println);
//       System.err.println("Source Map Mappings: " + mappings);
    }

    @Test
    public void googleTest() throws SourceMapParseException, IOException {
        // 1. 读取 .map 文件内容
        String mapContent = Files.readString(Paths.get("uploads/documents/5ce99897-596c-4201-83b0-6ca1cb1ad784.map"));

        // 2. 创建 SourceMapConsumer
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        consumer.parse(mapContent);

        // 3. 查询映射关系 - 使用正确的方法名
        // 注意：行号和列号都是从1开始
        Mapping.OriginalMapping mapping = consumer.getMappingForLine(1, 10);

        if (mapping != null) {
            System.out.println("Original File: " + mapping.getOriginalFile());
            System.out.println("Original Line: " + mapping.getLineNumber());
            System.out.println("Original Column: " + mapping.getColumnPosition());
            System.out.println("Original Name: " + mapping.getIdentifier());
        }

        // 4. 遍历所有 mappings（如果需要）
        consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor() {
            @Override
            public void visit(String sourceName, String symbolName,
                              FilePosition sourceStartPosition,
                              FilePosition startPosition,
                              FilePosition endPosition) {
                System.out.printf(
                        "Generated [%d:%d-%d:%d] -> Source %s [%d:%d] (Name: %s)%n",
                        startPosition.getLine(), startPosition.getColumn(),
                        endPosition.getLine(), endPosition.getColumn(),
                        sourceName,
                        sourceStartPosition.getLine(), sourceStartPosition.getColumn(),
                        symbolName);
            }
        });

    }

    @Test
    public void printDecodedMappings() throws IOException, SourceMapParseException {
        // 1. 读取 .map 文件
        String mapContent = Files.readString(Paths.get("uploads/documents/5ce99897-596c-4201-83b0-6ca1cb1ad784.map"));

        // 2. 解析 SourceMap
        SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
        consumer.parse(mapContent);

        // 3. 遍历并打印解码后的 mappings
        System.out.println("Decoded Mappings [Generated → Source]:");
        System.out.println("Generated Line:Column → Source File:Line:Column (Symbol)");
        System.out.println("----------------------------------------");

        consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor() {
            @Override
            public void visit(
                    String sourceName,
                    String symbolName,
                    FilePosition sourceStartPosition,
                    FilePosition startPosition,
                    FilePosition endPosition
            ) {
                // 生成代码的位置 (行:列)
                String generatedPos = String.format("%d:%d", startPosition.getLine(), startPosition.getColumn());

                // 源码的位置 (文件:行:列)
                String sourcePos = String.format(
                        "%s:%d:%d",
                        sourceName,
                        sourceStartPosition.getLine(),
                        sourceStartPosition.getColumn()
                );

                // 打印映射关系
                System.out.printf(
                        "%s → %s (Symbol: %s)%n",
                        generatedPos,
                        sourcePos,
                        symbolName != null ? symbolName : "N/A"
                );
            }
        });
    }


}

