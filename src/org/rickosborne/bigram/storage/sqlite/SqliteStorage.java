package org.rickosborne.bigram.storage.sqlite;

import org.rickosborne.bigram.storage.jdbc.JdbcStorage;

import java.sql.SQLException;

public class SqliteStorage extends JdbcStorage {

    public SqliteStorage(String dbFile) throws ClassNotFoundException, SQLException {
        super("jdbc:sqlite:" + dbFile);
        Class.forName("org.sqlite.JDBC");
        connection.createStatement().execute("PRAGMA synchronous = OFF;");
    }

}
