package org.rickosborne.bigram.storage.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.json.JSONObject;

import java.net.UnknownHostException;

public class MongoStorage {

    protected int maxWords;
    protected DBCollection collection;
    protected static final String KEY_ID = "_id";
    protected static final String KEY_INC = "$inc";
    protected static final String KEY_SET = "$set";
    protected static final String KEY_EXISTS = "$exists";
    protected static final String KEY_IFNULL = "$ifNull";
    protected static final String KEY_EVAL = "$";

    public MongoStorage(JSONObject config) {
        maxWords = config.optInt("maxWords", 3);
        try {
            MongoClient client = new MongoClient(
                config.optString("host", "localhost"),
                config.optInt("port", 27017)
            );
            DB db = client.getDB(config.optString("db", "words"));
            collection = db.getCollection(config.optString("collection", "words"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    protected static String[] splitWords(String words) {
        return words.split("\\w+");
    }

    protected static BasicDBObject idTemplate(String word) {
        return new BasicDBObject(KEY_ID, word);
    }

    protected static BasicDBObject inc(String path) {
        return new BasicDBObject(KEY_INC, new BasicDBObject(path, 1));
    }

    protected static BasicDBObject buildBasic(Object... pairs) {
        BasicDBObject o = new BasicDBObject();
        int pairCount = pairs.length / 2;
        for (int pair = 0; pair < pairCount; pair++) {
            int keyIndex = pair * 2, valueIndex = (pair * 2) + 1;
            if (pairs[keyIndex] instanceof String)
                o.append((String) pairs[keyIndex], pairs[valueIndex]);
        }
        return o;
    }

    protected static BasicDBObject exists(String path) {
        return new BasicDBObject(path, new BasicDBObject(KEY_EXISTS, true));
    }

    protected  static BasicDBObject set(String path, Object value) {
        return new BasicDBObject(KEY_SET, new BasicDBObject(path, value));
    }

}
