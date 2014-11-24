package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;

public interface IBigramStorage {

    public void add(String firstWord, String secondWord);
    public Prediction get(String firstWord, String partial);

}
