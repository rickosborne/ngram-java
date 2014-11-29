package org.rickosborne.bigram.util;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class State {

    private JSONObject state;
    private File file = null;

    public State(String fileName) {
        this.file = new File(fileName);
        try {
            state = new JSONObject(FileUtils.readFileToString(file));
        } catch (IOException e) {
            state = new JSONObject();
        }
    }

    public int get(String key, int defaultValue) {
        return state.optInt(key, defaultValue);
    }

    public void set(String key, int value) {
        state.put(key, value);
        save();
    }

    private void save() {
        try {
            FileUtils.writeStringToFile(file, state.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
