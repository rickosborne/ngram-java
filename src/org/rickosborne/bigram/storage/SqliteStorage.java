package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.*;

public class SqliteStorage {

    protected int seenColumn = 2;
    protected int wordColumn = 1;

    protected Connection connection;
    protected PreparedStatement insertStatement, updateStatement, selectStatement, lookupStatement;

    protected static String tableName, createSQL, insertSQL, updateSQL, selectSQL, lookupSQL;

    public SqliteStorage(String dbFile) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        Statement statement = connection.createStatement();
        statement.executeUpdate(createSQL);
        connection.createStatement().execute("PRAGMA synchronous = OFF;");
        insertStatement = connection.prepareStatement(insertSQL);
        updateStatement = connection.prepareStatement(updateSQL);
        selectStatement = connection.prepareStatement(selectSQL);
        lookupStatement = connection.prepareStatement(lookupSQL);
    }

    protected Prediction predictionFromQuery(PreparedStatement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery();
        int totalSeen = 0, maxSeen = 0, seen;
        String word = null;
        while(resultSet.next()) {
            seen = resultSet.getInt(seenColumn);
            totalSeen += seen;
            if (seen > maxSeen) {
                maxSeen = seen;
                word = resultSet.getString(wordColumn);
            }
        }
        if (word != null) return new Prediction(word, maxSeen, totalSeen);
        return null;
    }

}
