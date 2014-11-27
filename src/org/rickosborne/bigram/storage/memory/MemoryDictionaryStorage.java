package org.rickosborne.bigram.storage.memory;

import org.rickosborne.bigram.predictor.DictionaryPredictor;
import org.rickosborne.bigram.storage.IDictionaryStorage;
import org.rickosborne.bigram.util.Prediction;

import java.util.HashMap;

public class MemoryDictionaryStorage implements IDictionaryStorage {

    private HashMap<String, DictionaryPredictor.DictionaryWords> byPartial = new HashMap<String, DictionaryPredictor.DictionaryWords>();

    @Override
    public void add(String word) {
        for (int i = 1, wordLength = word.length(); i <= wordLength; i++) {
            String prefix = word.substring(0, i);
            if (!byPartial.containsKey(prefix)) byPartial.put(prefix, new DictionaryPredictor.DictionaryWords());
            byPartial.get(prefix).learn(word);
        }
    }

    @Override
    public Prediction get(String partial) {
        if (!byPartial.containsKey(partial)) return null;
        return byPartial.get(partial).predict();
    }
}
