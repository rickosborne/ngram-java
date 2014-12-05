package org.rickosborne.bigram.util;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class State {

    private JSONObject state;
    private File file = null;

    public State(String fileName) {
        this.file = new File(fileName);
        reload();
    }

    private void reload() {
        try {
            state = new JSONObject(FileUtils.readFileToString(file));
        } catch (IOException e) {
            state = new JSONObject();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int get(String key, int defaultValue) {
        return state.optInt(key, defaultValue);
    }

    public void set(String key, int value) {
        try {
            reload();
            state.put(key, value);
            save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            FileUtils.writeStringToFile(file, state.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
