package org.rickosborne.bigram.storage.jdbc;

import org.rickosborne.bigram.storage.ITrigramStorage;
import org.rickosborne.bigram.util.Prediction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcTrigramStorage extends JdbcStorage implements ITrigramStorage {

    private static final String selectWithPartialSQL;
    private PreparedStatement selectWithPartialStatement;

    static {
        tableName = "trigrams";
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
        insertSQL = "" +
                "INSERT INTO " + tableName + " (word1, word2, word3, seen) " +
                "VALUES (?, ?, ?, 1);";
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName +" (" +
                "word1 VARCHAR(50) NOT NULL, " +
                "word2 VARCHAR(50) NOT NULL, " +
                "word3 VARCHAR(50) NOT NULL, " +
                "seen INT UNSIGNED NOT NULL, " +
                "PRIMARY KEY (word1, word2, word3)" +
                ");";
        lookupSQL = "" +
                "SELECT seen " +
                "FROM " + tableName + " " +
                "WHERE (word1 = ?) AND (word2 = ?) AND (word3 = ?)" +
                "LIMIT 1;";
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (word1 = ?) AND (word2 = ?) AND (word3 = ?);";
    }

    public JdbcTrigramStorage(String dbFile) throws SQLException, ClassNotFoundException {
        super(dbFile);
        selectWithPartialStatement = connection.prepareStatement(selectWithPartialSQL);
    }

    @Override
    public void add(String firstWord, String secondWord, String thirdWord) throws SQLException {
        lookupStatement.setString(1, firstWord);
        lookupStatement.setString(2, secondWord);
        lookupStatement.setString(3, thirdWord);
        ResultSet existing = lookupStatement.executeQuery();
        PreparedStatement mutate = existing.next() ? updateStatement : insertStatement;
        mutate.setString(1, firstWord);
        mutate.setString(2, secondWord);
        mutate.setString(3, thirdWord);
        mutate.executeUpdate();
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
