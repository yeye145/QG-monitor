package com.qg.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.opengauss.util.PGobject;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @Description: List<Map<String,Object>> 类型处理器
 * @ClassName: ListHandler
 * @Author: lrt
 * @Date: 2025/8/9 10:16
 * @Version: 1.0
 */
public class ListHandler extends BaseTypeHandler<List<Map<String, Object>>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE = new TypeReference<List<Map<String, Object>>>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Map<String, Object>> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            System.out.println("ListHandler setNonNullParameter: " + json);

            // 使用 PGobject 来设置 jsonb 类型
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(json);
            ps.setObject(i, jsonObject);
        } catch (JsonProcessingException e) {
            throw new SQLException("JSON序列化失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        System.out.println("ListHandler getNullableResult (columnName): " + json);
        return parseJson(json);
    }

    @Override
    public List<Map<String, Object>> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        System.out.println("ListHandler getNullableResult (columnIndex): " + json);
        return parseJson(json);
    }

    @Override
    public List<Map<String, Object>> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        System.out.println("ListHandler getNullableResult (CallableStatement): " + json);
        return parseJson(json);
    }

    private List<Map<String, Object>> parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new SQLException("JSON反序列化失败: " + json, e);
        }
    }
}