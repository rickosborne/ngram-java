package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;

public interface ITrigramStorage {

    public void add(String firstWord, String secondWord, String thirdWord);
    public Prediction get(String firstWord, String secondWord, String partial);

}
