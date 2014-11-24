package org.rickosborne.bigram.storage.sqlite;

import org.rickosborne.bigram.storage.ITrigramStorage;
import org.rickosborne.bigram.storage.jdbc.JdbcTrigramStorage;

import java.sql.*;

public class SqliteTrigramStorage extends JdbcTrigramStorage implements ITrigramStorage {

    static {
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
        updateSQL = "" +
                "UPDATE " + tableName + " " +
                "SET seen = seen + 1 " +
                "WHERE (rowid = ?);";
    }

    public SqliteTrigramStorage(String dbFile) throws SQLException, ClassNotFoundException {
        super(dbFile);
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

}
