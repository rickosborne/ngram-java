package org.rickosborne.bigram.storage.jdbc;

import org.rickosborne.bigram.storage.IDictionaryStorage;
import org.rickosborne.bigram.util.Prediction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcDictionaryStorage extends JdbcStorage implements IDictionaryStorage {

    static {
        tableName = "dictionary";
        createSQL = "" +
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "word VARCHAR(50) NOT NULL, " +
                "seen INT UNSIGNED NOT NULL, " +
                "PRIMARY KEY (word)" +
                ");";
        lookupSQL = "" +
                "SELECT seen " +
                "FROM " + tableName + " " +
                "WHERE (word = ?)" +
                "LIMIT 1;";
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

    public JdbcDictionaryStorage(String url) {
        super(url);
    }

    @Override
    public void add(String word) {
        try {
            lookupStatement.setString(1, word);
            ResultSet existing = lookupStatement.executeQuery();
            PreparedStatement mutate = existing.next() ? updateStatement : insertStatement;
            mutate.setString(1, word);
            mutate.executeUpdate();
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
