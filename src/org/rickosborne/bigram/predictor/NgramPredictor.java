package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.storage.INgramStorage;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.IWordSpace;
import org.rickosborne.bigram.util.Prediction;

import java.util.ArrayList;
import java.util.LinkedList;

public class NgramPredictor implements IWordPredictor {

    private INgramStorage store;
    private int maxWords;
    private IWordSpace wordSpace;

    public NgramPredictor(INgramStorage store, IWordSpace wordSpace, Config config) {
        this.store = store;
        this.wordSpace = wordSpace;
        this.maxWords = config.get("ngramMaxWords", 6);
    }

    @Override
    public void learn(String[] words) {
        if (words.length < 1) return;
        for (int i = 0, l = words.length; i < l; i++) {
            ArrayList<String> keyWords = new ArrayList<String>();
            for (int j = 0, k = i; (j < maxWords) && (k < l); j++, k++) {
                String word = words[k];
                if ((word == null) || word.isEmpty() || (wordSpace.idForWord(word) == 0)) continue;
                keyWords.add(word);
                store.add(keyWords.toArray(new String[keyWords.size()]));
            }
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) {
        for (int wordCount = Math.min(maxWords - 1, words.length); wordCount > 0; wordCount--) {
            ArrayList<String> keyWords = new ArrayList<String>();
            for (int wordNum = words.length - wordCount; wordNum < words.length; wordNum++) {
                String word = words[wordNum];
                if (wordSpace.idForWord(word) == 0) continue;
                keyWords.add(word);
            }
            Prediction prediction = store.get(keyWords.toArray(new String[keyWords.size()]), partial);
            if (prediction != null) return prediction;
        }
        if ((partial == null) || partial.isEmpty()) return null;
        return store.get(new String[0], partial);
    }
}
