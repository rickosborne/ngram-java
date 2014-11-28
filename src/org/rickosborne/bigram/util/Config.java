package org.rickosborne.bigram.util;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class Config {

    JSONObject config = null;

    public Config(String filePath) {
        try {
            String text = FileUtils.readFileToString(new File(filePath));
            config = new JSONObject(text);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (config == null) config = new JSONObject();
        }
    }

    public JSONObject getObject(String key) {
        JSONObject result = config.optJSONObject(key);
        if (result == null) result = new JSONObject();
        return result;
    }

    public boolean get(String key, boolean defaultValue) {
        return config.optBoolean(key, defaultValue);
    }

    public String get(String key, String defaultValue) {
        return config.optString(key, defaultValue);
    }

    public int get(String key, int defaultValue) {
        return config.optInt(key, defaultValue);
    }

    public String get(String key) {
        return get(key, null);
    }

}
