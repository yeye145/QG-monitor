package com.qg.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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
@Component
public class JsonbTypeHandler extends BaseTypeHandler<Object> {


    private static ObjectMapper staticObjectMapper = new ObjectMapper();

    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        staticObjectMapper = objectMapper;
    }


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            String jsonValue;
            // 只有当参数不是基本类型时才进行 JSON 序列化
            if (isPrimitiveType(parameter)) {
                // 对于基本类型，直接使用其字符串表示
                jsonValue = parameter.toString();
            } else if (parameter instanceof String) {
                String stringValue = (String) parameter;
                // 验证是否为有效的JSON
                try {
                    staticObjectMapper.readTree(stringValue);
                    jsonValue = stringValue;
                } catch (JsonProcessingException e) {
                    // 如果不是有效的JSON，将其转换为JSON字符串
                    jsonValue = staticObjectMapper.writeValueAsString(parameter);
                }
            } else {
                // 复杂对象转换为JSON字符串
                jsonValue = staticObjectMapper.writeValueAsString(parameter);
            }
            ps.setObject(i, jsonValue, Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize parameter to JSON", e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
//        System.out.println("JsonbTypeHandler getNullableResult: " + json);
        return parseJson(json);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
//        System.out.println("JsonbTypeHandler getNullableResult: " + json);
        return parseJson(json);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
//        System.out.println("JsonbTypeHandler getNullableResult: " + json);
        return parseJson(json);
    }

    private Object parseJson(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            // 尝试解析为 JSON 对象
            return staticObjectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            // 如果解析失败，返回原始字符串
            return json;
        }
    }

    /**
     * 判断是否为基本类型
     */
    private boolean isPrimitiveType(Object obj) {
        return obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character ||
                obj instanceof Enum;
    }
}
