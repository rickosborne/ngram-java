package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.storage.ITrigramStorage;
import org.rickosborne.bigram.util.Prediction;

import java.util.HashMap;

public class TrigramPredictor implements WordPredictor {

    private ITrigramStorage store;

    public TrigramPredictor(ITrigramStorage store) {
        this.store = store;
    }

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
            store.add(first, second, third);
            first = second;
            second = third;
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) {
        if (words.length < 2) return null;
        String first = words[words.length - 2], second = words[words.length - 1];
        return store.get(first, second, partial);
    }
}
