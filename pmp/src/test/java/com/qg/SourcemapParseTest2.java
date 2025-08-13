package com.qg;

import com.qg.parse.SourceMapParser;
import com.qg.parse.SourceMapService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
public class SourcemapParseTest2 {
    // 在你的服务中使用
    public void processFrontendError(String mapFilePath, int lineno, int colno) {
        try {
            System.out.println("=== 开始解析 Source Map ===");
            System.out.println("Map 文件路径: " + mapFilePath);
            System.out.println("查找位置: 行 " + lineno + ", 列 " + colno);

            // 2. 解析原始源码位置
            SourceMapService service = new SourceMapService();
            SourceMapService.OriginalSourcePosition position = service.resolveSourcePosition(
                    mapFilePath, lineno, colno);

            if (position != null) {
                System.out.println("=== 成功解析原始源码位置 ===");
                System.out.println("  源文件: " + position.getSourceFile());
                System.out.println("  行号: " + position.getLineNumber());
                System.out.println("  列号: " + position.getColumnNumber());
                System.out.println("  源码: " + position.getSourceCode());

                // 如果源码为null，提供更多信息
                if (position.getSourceCode() == null) {
                    System.out.println("  注意: 源码内容为 null，可能原因:");
                    System.out.println("    1. Source map 中未包含 sourcesContent");
                    System.out.println("    2. 源文件内容为空");
                    System.out.println("    3. 行号超出源文件范围");
                }

                // 这里可以将信息保存到数据库或进行其他处理
            } else {
                System.out.println("无法解析原始源码位置");
            }
        } catch (Exception e) {
            System.err.println("处理前端错误时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testParse() {
        // 使用示例
        processFrontendError("uploads/maps/4617c291-97a7-490e-b2ff-e07a94c3db82.map", 9, 1065);
    }

    // 在你的测试类中添加这个方法
    @Test
    public void manualParseTest() {
        try {
            String mapFilePath = "uploads/maps/ab2b658e-437c-41b3-8de5-a013a037b21e.map";
            int lineno = 9;
            int colno = 1065;

            System.out.println("=== 手动解析测试 ===");
            System.out.println("文件: " + mapFilePath);
            System.out.println("位置: 行 " + lineno + ", 列 " + colno);

            // 读取 source map 内容
            String sourceMapContent = new String(Files.readAllBytes(Paths.get(mapFilePath)), StandardCharsets.UTF_8);

            // 使用你的 SourceMapParser
            SourceMapParser parser = new SourceMapParser(sourceMapContent);

            // 查找映射关系
            SourceMapParser.OriginalPosition position = parser.findOriginalPosition(lineno, colno);

            if (position != null) {
                System.out.println("找到映射位置:");
                System.out.println("  源文件: " + position.source);
                System.out.println("  行号: " + position.line);
                System.out.println("  列号: " + position.column);
                System.out.println("  源码: " + position.getSourceLine());
            } else {
                System.out.println("未找到精确映射");

                // 尝试查找附近的映射
                List<SourceMapParser.Mapping> mappings = parser.parseMappings();
                System.out.println("总映射点数: " + mappings.size());

                // 查找第9行的映射点
                System.out.println("第9行的映射点:");
                for (SourceMapParser.Mapping mapping : mappings) {
                    if (mapping.generatedLine == 9) {
                        System.out.println("  列 " + mapping.generatedColumn + " -> " +
                                (mapping.sourceIndex < parser.getSources().size() ?
                                        parser.getSources().get(mapping.sourceIndex) : "unknown") +
                                ":" + (mapping.originalLine + 1) + ":" + (mapping.originalColumn + 1));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
