package org.rickosborne.bigram.storage;

import org.rickosborne.bigram.util.Prediction;

public interface INgramStorage {

    public void add(int wordCount, String words);
    public Prediction get(int wordCount, String words, String partial);

}
