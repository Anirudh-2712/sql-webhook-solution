package com.example.sqlwebhook.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public QueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String,Object> runFinalQuery(String sql) {
        // queryForMap will throw if no row; wrapping could be added for safety
        return jdbcTemplate.queryForMap(sql);
    }
}
