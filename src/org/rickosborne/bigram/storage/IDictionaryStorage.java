package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;

public interface IDictionaryStorage {

    public void add(String word);
    public Prediction get(String partial);

}
