package com.qg.parse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceMapParser {
    
    private List<String> sources;
    private List<String> sourcesContent;
    private List<String> names;
    private String mappings;
    private int version;
    
    public SourceMapParser(String sourceMapContent) throws IllegalArgumentException {
        if (sourceMapContent == null || sourceMapContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Source map content is null or empty");
        }
        parseSourceMap(sourceMapContent);
    }
    
    private void parseSourceMap(String content) throws IllegalArgumentException {
        try {
            // 使用正则表达式提取关键字段，避免复杂的JSON解析
            mappings = extractField(content, "mappings");
            if (mappings == null) {
                // 尝试检查是否是格式化过的JSON
                mappings = extractField(content.replaceAll("\\s+", ""), "mappings");
            }
            
            if (mappings == null) {
                System.err.println("Source map content: " + content.substring(0, Math.min(200, content.length())) + "...");
                throw new IllegalArgumentException("Missing 'mappings' field in source map");
            }
            
            // 提取其他字段
            String versionStr = extractField(content, "version");
            version = versionStr != null ? Integer.parseInt(versionStr) : 3;
            
            sources = extractStringArray(content, "sources");
            if (sources == null) sources = new ArrayList<>();
            
            sourcesContent = extractStringArray(content, "sourcesContent");
            if (sourcesContent == null) sourcesContent = new ArrayList<>();
            
            names = extractStringArray(content, "names");
            if (names == null) names = new ArrayList<>();
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse source map: " + e.getMessage(), e);
        }
    }
    
    // 使用正则表达式提取字段值
    private String extractField(String json, String fieldName) {
        // 匹配 "fieldName": value 格式
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    // 提取数字字段
    private String extractNumberField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // 替换现有的 extractStringArray 方法
    private List<String> extractStringArray(String json, String fieldName) {
        List<String> result = new ArrayList<>();

        // 更精确的匹配模式
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\\[");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return result;
        }

        int startIndex = matcher.end();
        int bracketCount = 1;
        int currentIndex = startIndex;

        // 找到完整的数组内容
        while (currentIndex < json.length() && bracketCount > 0) {
            char c = json.charAt(currentIndex);
            if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
            }
            currentIndex++;
        }

        if (bracketCount == 0) {
            String arrayContent = json.substring(startIndex - 1, currentIndex);
            return parseJsonArray(arrayContent);
        }

        return result;
    }

    // 解析 JSON 数组
    private List<String> parseJsonArray(String arrayContent) {
        List<String> result = new ArrayList<>();

        // 移除外层括号
        if (arrayContent.startsWith("[") && arrayContent.endsWith("]")) {
            arrayContent = arrayContent.substring(1, arrayContent.length() - 1);
        }

        // 简单的数组元素分割（处理引号内的逗号）
        List<String> elements = splitJsonArrayElements(arrayContent);

        for (String element : elements) {
            element = element.trim();
            if (element.startsWith("\"") && element.endsWith("\"")) {
                // 处理字符串值
                element = element.substring(1, element.length() - 1);
                // 处理转义字符
                element = element.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
            }
            result.add(element);
        }

        return result;
    }

    // 分割 JSON 数组元素
    private List<String> splitJsonArrayElements(String content) {
        List<String> elements = new ArrayList<>();
        int start = 0;
        int bracketCount = 0;
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (escapeNext) {
                escapeNext = false;
                continue;
            }

            if (c == '\\' && inString) {
                escapeNext = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
            }

            if (!inString) {
                if (c == '[' || c == '{') {
                    bracketCount++;
                } else if (c == ']' || c == '}') {
                    bracketCount--;
                } else if (c == ',' && bracketCount == 0) {
                    elements.add(content.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }

        // 添加最后一个元素
        if (start < content.length()) {
            elements.add(content.substring(start).trim());
        }

        return elements;
    }
    
    // VLQ 解码器
    private static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    
    private int decodeBase64(char c) {
        return BASE64_CHARS.indexOf(c);
    }
    
    private List<Integer> decodeVLQ(String vlq) {
        List<Integer> result = new ArrayList<>();
        if (vlq == null || vlq.isEmpty()) {
            return result;
        }
        
        int value = 0;
        int shift = 0;
        
        for (int i = 0; i < vlq.length(); i++) {
            int digit = decodeBase64(vlq.charAt(i));
            if (digit == -1) {
                // 无效字符
                break;
            }
            
            boolean continuation = (digit & 32) != 0;
            digit &= 31;
            value += digit << shift;
            
            if (continuation) {
                shift += 5;
            } else {
                // 处理符号位
                result.add((value & 1) != 0 ? -(value >> 1) : value >> 1);
                value = 0;
                shift = 0;
            }
        }
        
        return result;
    }
    
    // Mapping 记录类
    public static class Mapping {
        public int generatedLine;
        public int generatedColumn;
        public int sourceIndex;
        public int originalLine;
        public int originalColumn;
        public int nameIndex;
        
        public Mapping(int genLine, int genCol, int srcIdx, int origLine, int origCol, int nameIdx) {
            this.generatedLine = genLine;
            this.generatedColumn = genCol;
            this.sourceIndex = srcIdx;
            this.originalLine = origLine;
            this.originalColumn = origCol;
            this.nameIndex = nameIdx;
        }
    }
    
    // 解析 mappings 字段
    public List<Mapping> parseMappings() {
        List<Mapping> mappingsList = new ArrayList<>();
        
        // 添加空值检查
        if (mappings == null || mappings.isEmpty()) {
            System.err.println("Mappings is null or empty");
            return mappingsList;
        }
        
        String[] lines = mappings.split(";");
        
        int previousGeneratedLine = 0;
        int previousGeneratedColumn = 0;
        int previousSourceIndex = 0;
        int previousOriginalLine = 0;
        int previousOriginalColumn = 0;
        int previousNameIndex = 0;
        
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            if (line == null || line.isEmpty()) {
                continue;
            }
            
            int generatedLine = lineIndex + 1;
            int generatedColumn = 0;
            
            String[] segments = line.split(",");
            for (String segment : segments) {
                if (segment == null || segment.isEmpty()) continue;
                
                List<Integer> values = decodeVLQ(segment);
                
                if (!values.isEmpty()) {
                    generatedColumn += values.get(0);
                    
                    if (values.size() > 1) {
                        int sourceIndex = previousSourceIndex + (values.size() > 1 ? values.get(1) : 0);
                        int originalLine = previousOriginalLine + (values.size() > 2 ? values.get(2) : 0);
                        int originalColumn = previousOriginalColumn + (values.size() > 3 ? values.get(3) : 0);
                        
                        int nameIndex = 0;
                        if (values.size() > 4) {
                            nameIndex = previousNameIndex + values.get(4);
                        }
                        
                        mappingsList.add(new Mapping(
                            generatedLine,
                            generatedColumn,
                            sourceIndex,
                            originalLine,
                            originalColumn,
                            nameIndex
                        ));
                        
                        previousSourceIndex = sourceIndex;
                        previousOriginalLine = originalLine;
                        previousOriginalColumn = originalColumn;
                        previousNameIndex = nameIndex;
                    }
                }
                
                previousGeneratedColumn = generatedColumn;
            }
        }
        
        return mappingsList;
    }
    
    // 根据行列号查找原始位置
    public OriginalPosition findOriginalPosition(int line, int column) {
        // 添加空值检查
        if (mappings == null) {
            System.err.println("Mappings is null");
            return null;
        }
        
        List<Mapping> mappingsList = parseMappings();
        
        Mapping bestMatch = null;
        for (Mapping mapping : mappingsList) {
            if (mapping.generatedLine == line && mapping.generatedColumn <= column) {
                if (bestMatch == null || mapping.generatedColumn > bestMatch.generatedColumn) {
                    bestMatch = mapping;
                }
            }
        }
        
        if (bestMatch != null && bestMatch.sourceIndex < sources.size()) {
            String sourceContent = null;
            if (sourcesContent != null && bestMatch.sourceIndex < sourcesContent.size()) {
                sourceContent = sourcesContent.get(bestMatch.sourceIndex);
            }
            
            return new OriginalPosition(
                sources.get(bestMatch.sourceIndex),
                bestMatch.originalLine + 1, // 转为 1-based
                bestMatch.originalColumn + 1, // 转为 1-based
                sourceContent
            );
        }
        
        return null;
    }
    
    // 原始位置信息类
    public static class OriginalPosition {
        public String source;
        public int line;
        public int column;
        public String sourceContent;
        
        public OriginalPosition(String source, int line, int column, String sourceContent) {
            this.source = source;
            this.line = line;
            this.column = column;
            this.sourceContent = sourceContent;
        }
        
        // 获取指定行的内容
        public String getSourceLine() {
            if (sourceContent == null) return null;
            
            String[] lines = sourceContent.split("\n");
            if (line > 0 && line <= lines.length) {
                return lines[line - 1]; // line 是 1-based，数组是 0-based
            }
            return null;
        }
    }
    
    // 获取 source map 数据的 getter 方法
    public List<String> getSources() {
        return sources;
    }
    
    public List<String> getSourcesContent() {
        return sourcesContent;
    }
    
    public String getMappings() {
        return mappings;
    }
    
    public int getVersion() {
        return version;
    }
    
    public List<String> getNames() {
        return names;
    }

    // 在 SourceMapParser 类中添加调试方法
    public void debugSourceMapInfo() {
        System.out.println("=== Source Map 信息 ===");
        System.out.println("Sources 数量: " + (sources != null ? sources.size() : 0));
        System.out.println("SourcesContent 数量: " + (sourcesContent != null ? sourcesContent.size() : 0));
        System.out.println("Names 数量: " + (names != null ? names.size() : 0));

        if (sources != null) {
            System.out.println("Sources 列表:");
            for (int i = 0; i < sources.size(); i++) {
                System.out.println("  [" + i + "] " + sources.get(i));
            }
        }

        if (sourcesContent != null) {
            System.out.println("SourcesContent 状态:");
            for (int i = 0; i < sourcesContent.size(); i++) {
                String content = sourcesContent.get(i);
                System.out.println("  [" + i + "] " + (content != null ?
                        "长度=" + content.length() + ", 预览=" + content.substring(0, Math.min(50, content.length())) :
                        "null"));
            }
        }
    }

    // 在 SourceMapParser 中添加方法
    public void debugSourceMapping() {
        System.out.println("=== Sources 和 SourcesContent 对应关系 ===");
        int maxLen = Math.max(sources.size(), sourcesContent.size());

        for (int i = 0; i < maxLen; i++) {
            String source = i < sources.size() ? sources.get(i) : "<无>";
            String content = i < sourcesContent.size() ?
                    (sourcesContent.get(i) != null ?
                            "长度=" + sourcesContent.get(i).length() :
                            "null") :
                    "<无>";

            System.out.println("[" + i + "] source: " + source);
            System.out.println("    content: " + content);
            System.out.println();
        }
    }
}
