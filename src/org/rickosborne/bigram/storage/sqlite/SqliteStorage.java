package org.rickosborne.bigram.storage.sqlite;

import org.rickosborne.bigram.storage.jdbc.JdbcStorage;

import java.sql.SQLException;

public class SqliteStorage extends JdbcStorage {

    public SqliteStorage(String dbFile) {
        super("jdbc:sqlite:" + dbFile);
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection.createStatement().execute("PRAGMA synchronous = OFF;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
