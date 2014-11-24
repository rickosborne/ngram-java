package org.rickosborne.bigram.storage.jdbc;

import org.rickosborne.bigram.storage.IBigramStorage;
import org.rickosborne.bigram.util.Prediction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcBigramStorage extends JdbcStorage implements IBigramStorage {

    protected PreparedStatement selectWithPartialStatement;
    protected static String selectWithPartialSQL;

    static {
        tableName = "bigrams";
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "word1 VARCHAR(50) NOT NULL, " +
                "word2 VARCHAR(50) NOT NULL, " +
                "seen INT UNSIGNED NOT NULL, " +
                "PRIMARY KEY (word1, word2)" +
                ");";
        lookupSQL = "" +
                "SELECT seen " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (word2 = ?)" +
                "LIMIT 1;";
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

    public JdbcBigramStorage(String url) throws SQLException, ClassNotFoundException {
        super(url);
        selectWithPartialStatement = connection.prepareStatement(selectWithPartialSQL);
    }

    @Override
    public void add(String firstWord, String secondWord) throws SQLException {
        lookupStatement.setString(1, firstWord);
        lookupStatement.setString(2, secondWord);
        ResultSet existing = lookupStatement.executeQuery();
        PreparedStatement mutate = existing.next() ? updateStatement : insertStatement;
        mutate.setString(1, firstWord);
        mutate.setString(2, secondWord);
        mutate.executeUpdate();
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
