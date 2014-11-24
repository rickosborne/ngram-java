package org.rickosborne.bigram.storage.sqlite;

import org.rickosborne.bigram.storage.IDictionaryStorage;
import org.rickosborne.bigram.storage.jdbc.JdbcDictionaryStorage;

import java.sql.*;

public class SqliteDictionaryStorage extends JdbcDictionaryStorage implements IDictionaryStorage {

    static {
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "word STRING, " +
                "seen INTEGER, " +
                "PRIMARY KEY (word)" +
                ");";
        lookupSQL = "" +
                "SELECT ROWID " +
                "FROM " + tableName + " " +
                "WHERE (word = ?);";
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (ROWID = ?);";
    }

    public SqliteDictionaryStorage(String dbFile) throws SQLException, ClassNotFoundException {
        super(dbFile);
    }

    @Override
    public void add(String word) throws SQLException {
        lookupStatement.setString(1, word);
        ResultSet existing = lookupStatement.executeQuery();
        if (existing.next()) {
            updateStatement.setInt(1, existing.getInt(1));
            updateStatement.executeUpdate();
        }
        else {
            insertStatement.setString(1, word);
            insertStatement.executeUpdate();
        }
    }

}
