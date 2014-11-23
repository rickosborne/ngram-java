package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;

public interface ITrigramStorage {

    public void add(String firstWord, String secondWord, String thirdWord) throws SQLException;
    public Prediction get(String firstWord, String secondWord, String partial) throws SQLException;

}
