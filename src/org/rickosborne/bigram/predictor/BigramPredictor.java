package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.util.Prediction;
import org.rickosborne.bigram.util.WordList;

import java.util.HashMap;

public class BigramPredictor implements WordPredictor {

    private HashMap<String, WordList> pairs = new HashMap<String, WordList>();

    @Override
    public void learn(String[] words) {
        String first = null;
        for (String second : words) {
            if (first == null) {
                first = second;
                continue;
            }
            if (!pairs.containsKey(first)) pairs.put(first, new WordList());
            pairs.get(first).learn(second);
            first = second;
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) {
        if (words.length < 1) return null;
        String first = words[words.length - 1];
        if (!pairs.containsKey(first)) return null;
        return pairs.get(first).predict(partial);
    }
}
