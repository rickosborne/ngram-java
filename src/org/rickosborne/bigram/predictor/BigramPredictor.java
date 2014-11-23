package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.storage.IBigramStorage;
import org.rickosborne.bigram.util.Prediction;
import org.rickosborne.bigram.util.WordList;

import java.sql.SQLException;
import java.util.HashMap;

public class BigramPredictor implements WordPredictor {

    private IBigramStorage store;

    public BigramPredictor(IBigramStorage store) {
        this.store = store;
    }

    @Override
    public void learn(String[] words) throws SQLException {
        if (words.length < 2) return;
        for (int i = 1, l = words.length; i < l; i++) {
            store.add(words[i-1], words[i]);
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) throws SQLException {
        if (words.length < 1) return null;
        String first = words[words.length - 1];
        return store.get(first, partial);
    }
}
