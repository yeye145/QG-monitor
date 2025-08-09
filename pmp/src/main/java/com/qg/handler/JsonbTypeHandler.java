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
public class JsonbTypeHandler extends BaseTypeHandler<Map<String,Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setObject(i, objectMapper.writeValueAsString(parameter), Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        if (json != null) {
            try {
                return objectMapper.readValue(json, HashMap.class);
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        if (json != null) {
            try {
                return objectMapper.readValue(json, HashMap.class);
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        if (json != null) {
            try {
                return objectMapper.readValue(json, HashMap.class);
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }
        return null;
    }
}
