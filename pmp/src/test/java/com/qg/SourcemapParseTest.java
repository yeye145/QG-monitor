package com.qg;

import com.qg.parse.SourceMapParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@Slf4j
public class SourcemapParseTest {

    // 在你的服务类中添加方法
    public String getSourceCodeFromMap(String mapFilePath, int lineNo, int colNo) {
        try {
            System.out.println("查找位置: 生成代码行 " + lineNo + ", 列 " + colNo);

            // 读取 source map 文件内容
            String mapContent = readFileContent(mapFilePath);

            // 解析 source map
            SourceMapParser parser = new SourceMapParser(mapContent);

            // 检查行号是否有效
            String mappings = parser.getMappings();
            String[] lines = mappings.split(";");
            System.out.println("Source map 总行数: " + lines.length);

            if (lineNo <= 0) {
                System.err.println("行号必须大于 0");
                return null;
            }

            if (lineNo > lines.length) {
                System.err.println("请求的行号 " + lineNo + " 超出了 source map 的范围 (" + lines.length + "行)");
                // 可以尝试查找最后一行
                lineNo = lines.length;
                System.out.println("调整为查找最后一行: " + lineNo);
            }

            // 查找原始位置
            SourceMapParser.OriginalPosition position = parser.findOriginalPosition(lineNo, colNo);

            if (position != null) {
                System.out.println("找到原始位置:");
                System.out.println("  源文件: " + position.source);
                System.out.println("  行: " + position.line);
                System.out.println("  列: " + position.column);

                // 获取源码行
                String sourceLine = position.getSourceLine();
                if (sourceLine != null) {
                    System.out.println("  源码: " + sourceLine);
                    return sourceLine;
                }
            } else {
                System.out.println("在指定位置未找到映射关系");
            }
        } catch (Exception e) {
            System.err.println("解析 source map 失败: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    // 查找附近位置的辅助方法
    private String searchNearbyPositions(SourceMapParser parser, int lineNo, int colNo) {
        System.out.println("尝试查找附近位置...");

        // 向前查找
        for (int col = colNo - 1; col >= 0; col--) {
            SourceMapParser.OriginalPosition pos = parser.findOriginalPosition(lineNo, col);
            if (pos != null) {
                System.out.println("在附近位置找到映射: 行 " + lineNo + ", 列 " + col);
                System.out.println("  源文件: " + pos.source);
                System.out.println("  行: " + pos.line);
                String sourceLine = pos.getSourceLine();
                if (sourceLine != null) {
                    System.out.println("  源码: " + sourceLine);
                    return sourceLine;
                }
                break;
            }
        }

        return null;
    }




    // 读取文件内容的辅助方法
    private String readFileContent(String filePath) throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        byte[] bytes = java.nio.file.Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    @Test
    public void testParse() {
        // 使用示例
        String sourceCodeLine = getSourceCodeFromMap("uploads/maps/3e9a1b29-dca2-4e39-b932-4c3622fb7017.map", 9, 1208);
        if (sourceCodeLine != null) {
            System.out.println("原始源码行: " + sourceCodeLine);
        } else {
            System.out.println("未能找到对应的源码");
        }
    }
}
