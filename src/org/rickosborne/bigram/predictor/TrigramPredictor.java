package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.util.Prediction;

import java.util.HashMap;

public class TrigramPredictor implements WordPredictor {

    private HashMap<String, BigramPredictor> grams = new HashMap<String, BigramPredictor>();

    @Override
    public void learn(String[] words) {
        String first = null, second = null;
        for (String third : words) {
            if (second == null) {
                second = third;
                continue;
            }
            if (first == null) {
                first = second;
                continue;
            }
            if (!grams.containsKey(first)) grams.put(first, new BigramPredictor());
            String bigram[] = {second, third};
            grams.get(first).learn(bigram);
            first = second;
            second = third;
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) {
        if (words.length < 2) return null;
        String first = words[words.length - 2], second = words[words.length - 1];
        if (!grams.containsKey(first)) return null;
        String[] bigramWords = {second};
        return grams.get(first).predict(bigramWords, partial);
    }
}
