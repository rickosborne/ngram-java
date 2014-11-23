package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.*;

public class SqliteBigramStorage extends SqliteStorage implements IBigramStorage {

    private PreparedStatement selectWithPartialStatement;
    private static final String selectWithPartialSQL;

    static {
        tableName = "bigrams";
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "word1 STRING, " +
                "word2 STRING, " +
                "seen INTEGER, " +
                "PRIMARY KEY (word1, word2)" +
                ") WITHOUT ROWID;";
        insertSQL = "" +
                "INSERT OR IGNORE INTO " + tableName + " (word1, word2, seen) " +
                "VALUES (?, ?, 0);";
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (word1 = ?) AND (word2 = ?);";
        selectSQL = "" +
                "SELECT word2, seen " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?);";
        selectWithPartialSQL = "" +
                "SELECT word2, seen " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (SUBSTR(word2, 1, ?) = ?);";
    }

    public SqliteBigramStorage(String dbFile) {
        super(dbFile);
        try {
            selectWithPartialStatement = connection.prepareStatement(selectWithPartialSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(String firstWord, String secondWord) {
        try {
            insertStatement.setString(1, firstWord);
            insertStatement.setString(2, secondWord);
            insertStatement.executeUpdate();
            updateStatement.setString(1, firstWord);
            updateStatement.setString(2, secondWord);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Prediction get(String firstWord, String partial) {
        try {
            PreparedStatement select;
            if ((partial == null) || (partial.length() == 0)) {
                select = selectStatement;
            } else {
                select = selectWithPartialStatement;
                select.setInt(2, partial.length());
                select.setString(3, partial);
            }
            select.setString(1, firstWord);
            return predictionFromQuery(select);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
