package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.*;

public class SqliteDictionaryStorage extends SqliteStorage implements IDictionaryStorage {

    static {
        tableName = "dictionary";
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "word STRING, " +
                "seen INTEGER, " +
                "PRIMARY KEY (word)" +
                ");";
        insertSQL = "" +
                "INSERT OR IGNORE INTO " + tableName + " (word, seen) " +
                "VALUES (?, 0);";
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (word = ?);";
        selectSQL = "" +
                "SELECT word, seen " +
                "FROM " + tableName + " " +
                "WHERE (SUBSTR(word, 1, ?) = ?);";
    }

    public SqliteDictionaryStorage(String dbFile) {
        super(dbFile);
    }

    @Override
    public void add(String word) {
        try {
            insertStatement.setString(1, word);
            insertStatement.executeUpdate();
            updateStatement.setString(1, word);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Prediction get(String partial) {
        if ((partial == null) || (partial.length() == 0)) return null;
        try {
            selectStatement.setInt(1, partial.length());
            selectStatement.setString(2, partial);
            return predictionFromQuery(selectStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
