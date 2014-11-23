package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

public interface IDictionaryStorage {

    public void add(String word);
    public Prediction get(String partial);

}
