package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.storage.IBigramStorage;
import org.rickosborne.bigram.util.Prediction;
import org.rickosborne.bigram.util.WordList;

import java.util.HashMap;

public class BigramPredictor implements WordPredictor {

    private IBigramStorage store;

    public BigramPredictor(IBigramStorage store) {
        this.store = store;
    }

    @Override
    public void learn(String[] words) {
        String first = null;
        for (String second : words) {
            if (first == null) {
                first = second;
                continue;
            }
            store.add(first, second);
            first = second;
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) {
        if (words.length < 1) return null;
        String first = words[words.length - 1];
        WordList known = store.get(first);
        if (known == null) return null;
        return known.predict(partial);
    }
}
