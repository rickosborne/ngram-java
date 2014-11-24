package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.storage.INgramStorage;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;

import java.util.ArrayList;
import java.util.LinkedList;

public class NgramPredictor implements IWordPredictor {

    private INgramStorage store;
    private int maxWords;

    public NgramPredictor(INgramStorage store, Config config) {
        this.store = store;
        this.maxWords = config.get("ngramMaxWords", 6);
    }

    @Override
    public void learn(String[] words) {
        if (words.length < 1) return;
        for (int i = 0, l = words.length; i < l; i++) {
            String key = "";
            boolean firstWord = true;
            for (int j = 0, k = i; (j < maxWords) && (k < l); j++, k++) {
                String word = words[k];
                if ((word == null) || word.isEmpty()) continue;
                if (firstWord) firstWord = false;
                else key += " ";
                key += word;
                store.add(j + 1, key);
            }
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) {
        for (int wordCount = Math.min(maxWords - 1, words.length); wordCount > 0; wordCount--) {
            StringBuilder builder = new StringBuilder();
            boolean firstWord = true;
            for (int wordNum = words.length - wordCount; wordNum < words.length; wordNum++) {
                if (firstWord) firstWord = false;
                else builder.append(" ");
                builder.append(words[wordNum]);
            }
            Prediction prediction = store.get(wordCount + 1, builder.toString(), partial);
            if (prediction != null) return prediction;
        }
        if ((partial == null) || partial.isEmpty()) return null;
        return store.get(1, "", partial);
    }
}
