package com.qg.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.opengauss.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 专门处理 Map<String, Object> 与 GaussDB JSONB 类型的转换
 */
@Component
// 明确泛型为 Map<String, Object>，与实体类字段类型一致
public class MapHandler extends BaseTypeHandler<Map<String, Object>> {

    private static ObjectMapper objectMapper = new ObjectMapper();
    // 定义 TypeReference 明确反序列化类型
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<>() {};

    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        MapHandler.objectMapper = objectMapper;
    }

    /**
     * 写入数据库：将 Map 转为 JSONB 类型
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // 用 PGobject 包装 JSON 字符串，指定类型为 jsonb（适配 GaussDB）
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(objectMapper.writeValueAsString(parameter));
            ps.setObject(i, jsonObject);
        } catch (JsonProcessingException e) {
            throw new SQLException("序列化 Map 到 JSON 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从结果集读取（按列名）
     */
    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJsonb(rs.getObject(columnName));
    }

    /**
     * 从结果集读取（按列索引）
     */
    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJsonb(rs.getObject(columnIndex));
    }

    /**
     * 从存储过程读取
     */
    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJsonb(cs.getObject(columnIndex));
    }

    /**
     * 解析 GaussDB 返回的 JSONB 对象
     */
    private Map<String, Object> parseJsonb(Object columnValue) throws SQLException {
        if (columnValue == null) {
            return null;
        }
        // GaussDB 的 JSONB 类型会被转为 PGobject
        if (columnValue instanceof PGobject pgObject) {
            String json = pgObject.getValue();
            if (json == null || json.isEmpty()) {
                return null;
            }
            try {
                // 反序列化为 Map<String, Object>
                return objectMapper.readValue(json, TYPE_REFERENCE);
            } catch (JsonProcessingException e) {
                throw new SQLException("解析 JSONB 失败: " + e.getMessage(), e);
            }
        } else {
            // 非 PGobject 类型（如字符串），直接尝试解析
            try {
                return objectMapper.readValue(columnValue.toString(), TYPE_REFERENCE);
            } catch (JsonProcessingException e) {
                throw new SQLException("解析 JSON 失败: " + e.getMessage(), e);
            }
        }
    }
}