package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.*;

public class SqliteTrigramStorage extends SqliteStorage implements ITrigramStorage {

    private static final String selectWithPartialSQL;

    static {
        tableName = "trigrams";
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName +" (" +
                "word1 STRING, " +
                "word2 STRING, " +
                "word3 STRING, " +
                "seen INTEGER, " +
                "PRIMARY KEY (word1, word2, word3)" +
                ");";
        lookupSQL = "" +
                "SELECT ROWID " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (word2 = ?) AND (word3 = ?);";
        insertSQL = "" +
                "INSERT INTO " + tableName + " (word1, word2, word3, seen) " +
                "VALUES (?, ?, ?, 1);";
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (rowid = ?);";
        selectSQL = "" +
                "SELECT word3, seen " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (word2 = ?)" +
                "ORDER BY 2 DESC;";
        selectWithPartialSQL = "" +
                "SELECT word3, seen " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (word2 = ?) AND (SUBSTR(word3, 1, ?) = ?)" +
                "ORDER BY 2 DESC;";
    }

    private PreparedStatement selectWithPartialStatement;

    public SqliteTrigramStorage(String dbFile) throws SQLException, ClassNotFoundException {
        super(dbFile);
        selectWithPartialStatement = connection.prepareStatement(selectWithPartialSQL);
    }

    @Override
    public void add(String firstWord, String secondWord, String thirdWord) throws SQLException {
        lookupStatement.setString(1, firstWord);
        lookupStatement.setString(2, secondWord);
        lookupStatement.setString(3, thirdWord);
        ResultSet existing = lookupStatement.executeQuery();
        if (existing.next()) {
            updateStatement.setInt(1, existing.getInt(1));
            updateStatement.executeUpdate();
        }
        else {
            insertStatement.setString(1, firstWord);
            insertStatement.setString(2, secondWord);
            insertStatement.setString(3, thirdWord);
            insertStatement.executeUpdate();
        }
    }

    @Override
    public Prediction get(String firstWord, String secondWord, String partial) throws SQLException {
        PreparedStatement select;
        if ((partial == null) || (partial.length() == 0)) {
            select = selectStatement;
        } else {
            select = selectWithPartialStatement;
            select.setInt(3, partial.length());
            select.setString(4, partial);
        }
        select.setString(1, firstWord);
        select.setString(2, secondWord);
        return predictionFromQuery(select);
    }
}
