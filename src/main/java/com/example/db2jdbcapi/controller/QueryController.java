package com.example.db2jdbcapi.controller;

import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.*;
import java.io.InputStream;

@RestController
@RequestMapping("/query")
public class QueryController {

    @GetMapping
    public Object executeQuery(@RequestParam String sql) {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            props.load(input);
        } catch (Exception e) {
            return Map.of("error", "設定ファイルの読み込みに失敗しました。", "message", e.getMessage());
        }

        String url = props.getProperty("db.url");
        Properties connectionProps = new Properties();
        connectionProps.setProperty("user", props.getProperty("db.user"));
        connectionProps.setProperty("password", props.getProperty("db.password"));
        connectionProps.setProperty("ClientApplicationInformation",
                props.getProperty("db.clientApplicationInformation"));

        try (Connection conn = DriverManager.getConnection(url, connectionProps);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            List<Map<String, Object>> results = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }

            return results;

        } catch (SQLException e) {
            return Map.of("error", "SQL実行中にエラーが発生しました。", "message", e.getMessage());
        }
    }
}
