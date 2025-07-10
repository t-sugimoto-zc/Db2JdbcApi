package com.example.db2jdbcapi.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;

@Service
public class TransactionService {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private static class Session {
        Connection conn;
        Savepoint savepoint;

        Session(Connection conn, Savepoint savepoint) {
            this.conn = conn;
            this.savepoint = savepoint;
        }
    }

    public Map<String, String> startTransaction() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            props.load(input);
        } catch (Exception e) {
            System.out.println("設定ファイルの読み込みに失敗しました: " + e.getMessage());
            return Map.of("error", "設定ファイルの読み込みに失敗しました。", "message", e.getMessage());
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        String clientAppInfo = props.getProperty("db.clientApplicationInformation");

        Properties connectionProps = new Properties();
        connectionProps.setProperty("user", user);
        connectionProps.setProperty("password", password);
        connectionProps.setProperty("ClientApplicationInformation", clientAppInfo);

        System.out.println("DB接続開始: URL=" + url + ", USER=" + user + ", ClientAppInfo=" + clientAppInfo);

        try {
            Connection conn = DriverManager.getConnection(url, connectionProps);
            conn.setAutoCommit(false);
            Savepoint savepoint = conn.setSavepoint();
            String sessionId = UUID.randomUUID().toString();
            sessions.put(sessionId, new Session(conn, savepoint));
            return Map.of("sessionId", sessionId);
        } catch (SQLException e) {
            System.out.println("DB接続失敗: " + e.getMessage());
            return Map.of("error", "トランザクション開始に失敗しました。", "message", e.getMessage());
        }
    }

    public Object executeQuery(String sessionId, String sql) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return Map.of("error", "無効なセッションIDです。");
        }

        try (Statement stmt = session.conn.createStatement()) {
            boolean isResultSet = stmt.execute(sql);

            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
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
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                return Map.of("message", "更新が成功しました。", "updateCount", updateCount);
            }
        } catch (SQLException e) {
            return Map.of("error", "SQL実行中にエラーが発生しました。", "message", e.getMessage());
        }
    }

    public Map<String, String> commit(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session == null) {
            return Map.of("error", "無効なセッションIDです。");
        }

        try {
            session.conn.commit();
            session.conn.close();
            return Map.of("message", "コミットしました。");
        } catch (SQLException e) {
            return Map.of("error", "コミットに失敗しました。", "message", e.getMessage());
        }
    }

    public Map<String, String> rollback(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session == null) {
            return Map.of("error", "無効なセッションIDです。");
        }

        try {
            session.conn.rollback(session.savepoint);
            session.conn.close();
            return Map.of("message", "ロールバックしました。");
        } catch (SQLException e) {
            return Map.of("error", "ロールバックに失敗しました。", "message", e.getMessage());
        }
    }
}
