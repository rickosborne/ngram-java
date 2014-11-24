package org.rickosborne.bigram.storage.sqlite;

import org.rickosborne.bigram.storage.jdbc.JdbcBigramStorage;

import java.sql.*;

public class SqliteBigramStorage extends JdbcBigramStorage {

    static {
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "word1 STRING, " +
                "word2 STRING, " +
                "seen INTEGER, " +
                "PRIMARY KEY (word1, word2)" +
                ");";
        lookupSQL = "" +
                "SELECT ROWID " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (word2 = ?);";
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (ROWID = ?);";
    }

    public SqliteBigramStorage(String url) {
        super(url);
    }

    @Override
    public void add(String firstWord, String secondWord) {
        try {
            lookupStatement.setString(1, firstWord);
            lookupStatement.setString(2, secondWord);
            ResultSet existing = lookupStatement.executeQuery();
            if (existing.next()) {
                updateStatement.setInt(1, existing.getInt(1));
                updateStatement.executeUpdate();
            }
            else {
                insertStatement.setString(1, firstWord);
                insertStatement.setString(2, secondWord);
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
