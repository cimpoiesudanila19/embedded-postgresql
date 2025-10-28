package org.example;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .start();

        Connection conn = postgres.getPostgresDatabase().getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SHOW max_connections");
        if (rs.next()) {
            System.out.println("Max connections: " + rs.getString(1));
        }
        stmt.execute("CREATE DATABASE myapp");
    }
}
