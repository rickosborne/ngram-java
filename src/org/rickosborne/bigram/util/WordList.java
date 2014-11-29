package org.rickosborne.bigram.util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WordList {

    protected HashMap<String, Integer> words = new HashMap<String, Integer>();

    public void learn(String word) {
        learn(word, 1);
    }

    public void learn(String word, int seen) {
        this.words.put(word, seen + (this.words.containsKey(word) ? this.words.get(word) : 0));
    }

    public Prediction predict(String partial) {
        String guess = null;
        int max = 0, total = 0;
        Prediction prediction = new Prediction();
        for (Map.Entry<String, Integer> pair : this.words.entrySet()) {
            int seen = pair.getValue();
            String word = pair.getKey();
            if ((partial != null) && ((word.length() < partial.length()) || !(word.substring(0, partial.length()).equals(partial)))) continue;
            prediction.addOption(word, seen);
            total += seen;
            if (seen > max) {
                max = seen;
                guess = pair.getKey();
            }
        }
        prediction.setWord(guess, max, total);
        return prediction;
    }

    public JSONObject asJSONObject() {
        return new JSONObject(words);
    }

}
