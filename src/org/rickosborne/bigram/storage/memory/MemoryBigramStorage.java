package org.rickosborne.bigram.storage.memory;

import org.rickosborne.bigram.storage.IBigramStorage;
import org.rickosborne.bigram.util.Prediction;
import org.rickosborne.bigram.util.WordList;

import java.util.HashMap;

public class MemoryBigramStorage implements IBigramStorage {

    private HashMap<String, WordList> pairs = new HashMap<String, WordList>();

    @Override
    public void add(String firstWord, String secondWord) {
        if (!pairs.containsKey(firstWord)) pairs.put(firstWord, new WordList());
        pairs.get(firstWord).learn(secondWord);
    }

    @Override
    public Prediction get(String firstWord, String partial) {
        if (!pairs.containsKey(firstWord)) return null;
        return pairs.get(firstWord).predict(partial);
    }
}
