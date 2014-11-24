package org.rickosborne.bigram.storage.memory;

import org.rickosborne.bigram.predictor.BigramPredictor;
import org.rickosborne.bigram.storage.ITrigramStorage;
import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;
import java.util.HashMap;

public class MemoryTrigramStorage implements ITrigramStorage {

    private HashMap<String, BigramPredictor> grams = new HashMap<>();

    @Override
    public void add(String firstWord, String secondWord, String thirdWord) {
        if (!grams.containsKey(firstWord)) grams.put(firstWord, new BigramPredictor(new MemoryBigramStorage()));
        String bigram[] = {secondWord, thirdWord};
        grams.get(firstWord).learn(bigram);
    }

    @Override
    public Prediction get(String firstWord, String secondWord, String partial) {
        if (!grams.containsKey(firstWord)) return null;
        String[] bigramWords = {secondWord};
        return grams.get(firstWord).predict(bigramWords, partial);
    }
}
