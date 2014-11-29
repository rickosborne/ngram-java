package org.rickosborne.bigram.util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Prediction {

    private String word;
    private int seen;
    private int size;
    private HashMap<String,Integer> options = new HashMap<String,Integer>();

    public Prediction() {}

    public Prediction (String word, int seen, int size) {
        setWord(word, seen, size);
    }

    public void setWord (String word, int seen, int size) {
        this.word = word;
        this.seen = seen;
        this.size = size;
    }

    public void addOption(String word, int seen) {
        if (options.containsKey(word)) options.put(word, options.get(word) + seen);
        else options.put(word, seen);
    }

    public String getWord () { return this.word; }
    public int getSeen() { return this.seen; }
    public int getSize() { return this.size; }
    public Map<String,Integer> getOptions() { return this.options; }
    public double getChance() { return (double) this.seen / (double) this.size; }

    public JSONObject asJSONObject() {
        JSONObject json = new JSONObject();
        json.put("word", word);
        json.put("seen", seen);
        json.put("size", size);
        json.put("options", options);
        return json;
    }

}
