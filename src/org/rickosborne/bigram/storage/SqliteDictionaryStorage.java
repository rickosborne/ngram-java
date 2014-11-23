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
        lookupSQL = "" +
                "SELECT ROWID " +
                "FROM " + tableName + " " +
                "WHERE (word = ?);";
        insertSQL = "" +
                "INSERT INTO " + tableName + " (word, seen) " +
                "VALUES (?, 1);";
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (word = ?);";
        selectSQL = "" +
                "SELECT word, seen " +
                "FROM " + tableName + " " +
                "WHERE (SUBSTR(word, 1, ?) = ?);";
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

    @Override
    public Prediction get(String partial) throws SQLException {
        if ((partial == null) || (partial.length() == 0)) return null;
        selectStatement.setInt(1, partial.length());
        selectStatement.setString(2, partial);
        return predictionFromQuery(selectStatement);
    }
}
