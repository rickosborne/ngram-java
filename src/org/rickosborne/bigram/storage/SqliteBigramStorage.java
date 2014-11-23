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
                ");";
        lookupSQL = "" +
                "SELECT ROWID " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (word2 = ?);";
        insertSQL = "" +
                "INSERT INTO " + tableName + " (word1, word2, seen) " +
                "VALUES (?, ?, 1);";
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

    public SqliteBigramStorage(String dbFile) throws SQLException, ClassNotFoundException {
        super(dbFile);
        selectWithPartialStatement = connection.prepareStatement(selectWithPartialSQL);
    }

    @Override
    public void add(String firstWord, String secondWord) throws SQLException {
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
    }

    @Override
    public Prediction get(String firstWord, String partial) throws SQLException {
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
    }
}
