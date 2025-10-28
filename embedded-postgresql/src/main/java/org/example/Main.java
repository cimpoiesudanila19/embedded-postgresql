package org.example;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

import static java.nio.file.Files.readAllBytes;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {

        System.out.println("🚀 Запуск Embedded PostgreSQL...");
        EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .start();

        Connection conn = postgres.getPostgresDatabase().getConnection();

        System.out.println("✅ База данных запущена на: " + postgres.getPostgresDatabase());

        System.out.println("📁 Выполнение init-test-db.sql...");
//        executeSqlFile(conn, "src/main/resources/init-test-db.sql");
        String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/init-test-db.sql")));
        try (Statement statement = conn.createStatement()) {
            String[] commands = sql.split(";");
            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    statement.execute(command);
                }
            }
        }

        // Проверяем таблицы
        System.out.println("🔍 Проверка таблиц...");
        checkTable(conn, "users");
        checkTable(conn, "products");

        // Выводим содержимое
        System.out.println("\n📊 Содержимое таблиц:");
        printTableData(conn, "SELECT * FROM users");
        printTableData(conn, "SELECT * FROM products");

        System.out.println("\n🎉 ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ УСПЕШНО!");
        
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SHOW max_connections");
        if (rs.next()) {
            System.out.println("Max connections: " + rs.getString(1));
        }
        stmt.execute("CREATE DATABASE myapp");
    }

    private static void executeSqlFile(Connection connection, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("SQL file not found: " + filePath);
        }

        String sql = new String(readAllBytes(file.toPath()));
        try (Statement statement = connection.createStatement()) {

            // Разделяем SQL по точкам с запятой
            String[] sqlCommands = sql.split(";");
            for (String command : sqlCommands) {
                if (!command.trim().isEmpty()) {
                    statement.execute(command);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkTable(Connection connection, String tableName) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM " + tableName)) {
            rs.next();
            int count = rs.getInt(1);
            System.out.println("✅ Таблица " + tableName + ": " + count + " записей");
        }
    }

    private static void printTableData(Connection connection, String query) throws SQLException {
        System.out.println("\nЗапрос: " + query);
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Заголовки
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-20s", metaData.getColumnName(i));
            }
            System.out.println();

            // Данные
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.printf("%-20s", rs.getString(i));
                }
                System.out.println();
            }
        }
    }
}
