package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.util.Prediction;
import org.rickosborne.bigram.util.WordList;

import java.util.HashMap;

public class DictionaryPredictor implements WordPredictor {

    private class DictionaryWords extends WordList {

        private Prediction prediction = null; // memo

        @Override
        public Prediction predict(String partial) {
            if (prediction == null) {
                prediction = super.predict(partial);
            }
            return prediction;
        }

    } // DictionaryWords

    private HashMap<String, DictionaryWords> byPartial = new HashMap<String, DictionaryWords>();

    @Override
    public void learn(String[] words) {
        for (String word : words) {
            for (int i = 1, wordLength = word.length(); i <= wordLength; i++) {
                String prefix = word.substring(0, i);
                if (!byPartial.containsKey(prefix)) byPartial.put(prefix, new DictionaryWords());
                byPartial.get(prefix).learn(word);
            }
        }
    }

    @Override
    public Prediction predict(String[] words, String partial) {
        if (partial == null) return null;
        if (!byPartial.containsKey(partial)) return null;
        return byPartial.get(partial).predict(null);
    }
}
