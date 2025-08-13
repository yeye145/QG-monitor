package com.qg.parse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SourceMapService {
    
    /**
     * 通过 source map 将构建后 JS 文件的行列号解析为原始源码
     * @param sourceMapPath source map 文件路径
     * @param generatedLine 构建后 JS 文件中的行号（从1开始）
     * @param generatedColumn 构建后 JS 文件中的列号（从0开始）
     * @return 原始源码位置信息
     */
    public OriginalSourcePosition resolveSourcePosition(String sourceMapPath,
                                                        int generatedLine,
                                                        int generatedColumn) {
        try {
            // 读取并解析 source map
            String sourceMapContent = readFileContent(sourceMapPath);
            SourceMapParser parser = new SourceMapParser(sourceMapContent);

            // 调试信息
            System.out.println("=== 调试信息 ===");
            parser.debugSourceMapInfo();
            parser.debugSourceMapping();

            // 验证 source map 是否有效
            if (parser.getMappings() == null || parser.getMappings().isEmpty()) {
                System.err.println("Source map 中没有映射信息");
                return null;
            }

            System.out.println("正在解析位置: 构建后 JS 文件第 " + generatedLine + " 行, 第 " + generatedColumn + " 列");

            // 查找映射关系
            SourceMapParser.OriginalPosition position = parser.findOriginalPosition(
                    generatedLine, generatedColumn);

            if (position != null) {
                OriginalSourcePosition result = new OriginalSourcePosition();
                result.setSourceFile(position.source);
                result.setLineNumber(position.line);
                result.setColumnNumber(position.column);

                // 改进源码获取逻辑
                String sourceCode = getSourceCode(parser, position);
                result.setSourceCode(sourceCode);

                return result;
            } else {
                System.out.println("未找到精确映射，尝试查找最近的映射...");
                return findNearestPosition(parser, generatedLine, generatedColumn);
            }

        } catch (Exception e) {
            System.err.println("解析 source map 时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 改进的源码获取方法
     */
    private String getSourceCode(SourceMapParser parser, SourceMapParser.OriginalPosition position) {
        try {
            // 方法1: 从 sourcesContent 中获取（修复索引匹配问题）
            if (parser.getSourcesContent() != null && parser.getSources() != null) {
                // 正确的匹配方式：通过源文件名在 sources 数组中找到索引
                String targetSource = position.source;
                int sourceIndex = -1;

                // 在 sources 数组中查找目标源文件的索引
                for (int i = 0; i < parser.getSources().size(); i++) {
                    if (targetSource.equals(parser.getSources().get(i))) {
                        sourceIndex = i;
                        break;
                    }
                }

                System.out.println("目标源文件: " + targetSource);
                System.out.println("在 sources 中的索引: " + sourceIndex);

                // 检查索引是否有效且在 sourcesContent 范围内
                if (sourceIndex != -1 && sourceIndex < parser.getSourcesContent().size()) {
                    String content = parser.getSourcesContent().get(sourceIndex);
                    System.out.println("获取到的源文件内容长度: " + (content != null ? content.length() : "null"));

                    if (content != null) {
                        String[] lines = content.split("\n");
                        System.out.println("源文件总行数: " + lines.length);
                        System.out.println("请求的行号: " + position.line);

                        if (position.line > 0 && position.line <= lines.length) {
                            String sourceLine = lines[position.line - 1]; // 行号是1-based
                            System.out.println("成功获取源码行: " + sourceLine);
                            return sourceLine;
                        } else {
                            System.out.println("行号超出范围");
                            // 打印附近几行的内容用于调试
                            for (int i = Math.max(0, position.line - 3);
                                 i < Math.min(lines.length, position.line + 2); i++) {
                                System.out.println("  行 " + (i + 1) + ": " + lines[i]);
                            }
                        }
                    }
                } else {
                    System.out.println("索引无效或超出 sourcesContent 范围");
                    System.out.println("sourcesContent 数组大小: " + parser.getSourcesContent().size());
                }
            }

            // 方法2: 如果 sourcesContent 为空或不匹配，尝试从 sources 中获取文件路径并读取文件
            if (position.source != null && !position.source.startsWith("webpack://")) {
                System.out.println("尝试从文件系统读取源文件: " + position.source);
                // 这里可以根据你的项目结构调整路径
                String sourceCode = readSourceFileFromFileSystem(position.source);
                if (sourceCode != null) {
                    String[] lines = sourceCode.split("\n");
                    if (position.line > 0 && position.line <= lines.length) {
                        return lines[position.line - 1];
                    }
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("获取源码内容时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从文件系统读取源文件内容
     */
    private String readSourceFileFromFileSystem(String sourcePath) {
        try {
            // 根据你的项目结构调整路径
            // 假设 source map 文件在 uploads/maps/ 目录下
            // 源文件可能需要去除相对路径前缀 ../../

            if (sourcePath.startsWith("../../")) {
                // 去除相对路径前缀
                String relativePath = sourcePath.substring(6); // 去除 "../../"
                System.out.println("相对路径: " + relativePath);

                // 尝试在不同目录下查找源文件
                String[] possiblePaths = {
                        "uploads/src/" + relativePath,
                        "src/" + relativePath,
                        relativePath
                };

                for (String path : possiblePaths) {
                    try {
                        if (Files.exists(Paths.get(path))) {
                            System.out.println("找到源文件: " + path);
                            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
                        }
                    } catch (IOException e) {
                        System.out.println("尝试路径 " + path + " 失败: " + e.getMessage());
                    }
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("从文件系统读取源文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 查找最近的映射位置（当精确位置没有映射时）
     */
    private OriginalSourcePosition findNearestPosition(SourceMapParser parser,
                                                       int generatedLine,
                                                       int generatedColumn) {
        try {
            List<SourceMapParser.Mapping> allMappings = parser.parseMappings();
            if (allMappings.isEmpty()) {
                return null;
            }

            // 查找最接近的映射点
            SourceMapParser.Mapping nearest = null;
            long minDistance = Long.MAX_VALUE;

            for (SourceMapParser.Mapping mapping : allMappings) {
                // 计算与目标位置的距离
                long lineDiff = Math.abs(mapping.generatedLine - generatedLine);
                long distance;

                if (lineDiff == 0) {
                    // 同一行，计算列距离
                    distance = Math.abs(mapping.generatedColumn - generatedColumn);
                } else {
                    // 不同行，优先考虑行距离
                    distance = lineDiff * 100000L + Math.abs(mapping.generatedColumn - generatedColumn);
                }

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = mapping;
                }
            }

            if (nearest != null && minDistance < 100000) { // 设置一个合理的阈值
                if (nearest.sourceIndex < parser.getSources().size()) {
                    OriginalSourcePosition result = new OriginalSourcePosition();
                    String sourceFile = parser.getSources().get(nearest.sourceIndex);
                    result.setSourceFile(sourceFile);
                    result.setLineNumber(nearest.originalLine + 1);
                    result.setColumnNumber(nearest.originalColumn + 1);

                    // 改进的源码获取逻辑
                    SourceMapParser.OriginalPosition tempPosition = new SourceMapParser.OriginalPosition(
                            sourceFile, nearest.originalLine + 1, nearest.originalColumn + 1, null);
                    String sourceCode = getSourceCode(parser, tempPosition);
                    result.setSourceCode(sourceCode);

                    System.out.println("找到最近的映射位置: 距离 " + minDistance);
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("查找最近位置时出错: " + e.getMessage());
        }

        return null;
    }
    /**
     * 为映射点获取源码
     */
    private String getSourceCodeForMapping(SourceMapParser parser, SourceMapParser.Mapping mapping) {
        try {
            if (mapping.sourceIndex < parser.getSourcesContent().size()) {
                String sourceContent = parser.getSourcesContent().get(mapping.sourceIndex);
                if (sourceContent != null) {
                    String[] lines = sourceContent.split("\n");
                    if (mapping.originalLine >= 0 && mapping.originalLine < lines.length) {
                        return lines[mapping.originalLine];
                    }
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("获取映射点源码时出错: " + e.getMessage());
            return null;
        }
    }

    // 原始源码位置信息类
    public static class OriginalSourcePosition {
        private String sourceFile;    // 原始源文件路径
        private int lineNumber;       // 原始行号
        private int columnNumber;     // 原始列号
        private String sourceCode;    // 对应的源码行
        
        // getters and setters
        public String getSourceFile() { return sourceFile; }
        public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
        
        public int getLineNumber() { return lineNumber; }
        public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
        
        public int getColumnNumber() { return columnNumber; }
        public void setColumnNumber(int columnNumber) { this.columnNumber = columnNumber; }
        
        public String getSourceCode() { return sourceCode; }
        public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
        
        @Override
        public String toString() {
            return String.format("OriginalSourcePosition{sourceFile='%s', lineNumber=%d, columnNumber=%d, sourceCode='%s'}", 
                               sourceFile, lineNumber, columnNumber, sourceCode);
        }
    }
    
    private String readFileContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    }




}
