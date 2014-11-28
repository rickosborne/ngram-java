package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

public interface INgramStorage {

    public void add(String[] words);
    public Prediction get(String[] words, String partial);

}
