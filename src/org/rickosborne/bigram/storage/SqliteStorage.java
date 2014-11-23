package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.*;

public class SqliteStorage {

    protected int seenColumn = 2;
    protected int wordColumn = 1;

    protected Connection connection;
    protected PreparedStatement insertStatement, updateStatement, selectStatement;

    protected static String tableName, createSQL, insertSQL, updateSQL, selectSQL;

    public SqliteStorage(String dbFile) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            Statement statement = connection.createStatement();
            statement.executeUpdate(createSQL);
            insertStatement = connection.prepareStatement(insertSQL);
            updateStatement = connection.prepareStatement(updateSQL);
            selectStatement = connection.prepareStatement(selectSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected Prediction predictionFromQuery(PreparedStatement statement) {
        try {
            ResultSet resultSet = statement.executeQuery();
            int totalSeen = 0, maxSeen = 0, seen;
            String word = null;
            while(resultSet.next()) {
                seen = resultSet.getInt(seenColumn);
                if (seen > maxSeen) {
                    maxSeen = seen;
                    word = resultSet.getString(wordColumn);
                }
            }
            if (word != null) return new Prediction(word, maxSeen, totalSeen);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
