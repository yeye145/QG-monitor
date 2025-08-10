package com.qg.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 转换  // 类说明
 * @ClassName: JsonbTypeHandler    // 类名
 * @Author: lrt          // 创建者
 * @Date: 2025/8/9 10:16   // 时间
 * @Version: 1.0     // 版本
 */
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            String jsonValue;
            if (parameter instanceof String) {
                // 如果参数已经是字符串，验证是否为有效的JSON
                String stringValue = (String) parameter;
                try {
                    objectMapper.readTree(stringValue);
                    jsonValue = stringValue;
                } catch (JsonProcessingException e) {
                    // 如果不是有效的JSON，将其转换为JSON字符串
                    jsonValue = objectMapper.writeValueAsString(parameter);
                }
            } else {
                // 其他类型都转换为JSON字符串
                jsonValue = objectMapper.writeValueAsString(parameter);
            }
            ps.setObject(i, jsonValue, Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize parameter to JSON", e);
        }
    }

    @Override
    public Object getNullableResult(java.sql.ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public Object getNullableResult(java.sql.ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public Object getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private Object parseJson(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse JSON: " + json, e);
        }
    }
}
