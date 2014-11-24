package org.rickosborne.bigram.storage.jdbc;

import org.rickosborne.bigram.util.Prediction;
import org.mariadb.jdbc.Driver;
import java.sql.*;

public class JdbcStorage {

    protected int seenColumn = 2;
    protected int wordColumn = 1;

    protected Connection connection;
    protected PreparedStatement insertStatement, updateStatement, selectStatement, lookupStatement;

    protected static String tableName, createSQL, insertSQL, updateSQL, selectSQL, lookupSQL;

    public JdbcStorage(String url) {
        // Class.forName("org.mariadb.jdbc.Driver");
        try {
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.executeUpdate(createSQL);
            insertStatement = connection.prepareStatement(insertSQL);
            updateStatement = connection.prepareStatement(updateSQL);
            selectStatement = connection.prepareStatement(selectSQL);
            lookupStatement = connection.prepareStatement(lookupSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected Prediction predictionFromQuery(PreparedStatement statement) {
        ResultSet resultSet;
        int totalSeen = 0, maxSeen = 0, seen;
        String word = null;
        try {
            resultSet = statement.executeQuery();
            while(resultSet.next()) {
                seen = resultSet.getInt(seenColumn);
                totalSeen += seen;
                if (seen > maxSeen) {
                    maxSeen = seen;
                    word = resultSet.getString(wordColumn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (word != null) return new Prediction(word, maxSeen, totalSeen);
        return null;
    }

}
