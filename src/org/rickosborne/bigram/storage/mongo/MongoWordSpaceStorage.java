package org.rickosborne.bigram.storage.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.json.JSONObject;
import org.rickosborne.bigram.storage.IWordSpaceStorage;

public class MongoWordSpaceStorage extends MongoStorage implements IWordSpaceStorage {

    private final static String KEY_SPACE = "wordSpace";
    private final static String KEY_WORDID = "wordSpaceId";
    private BasicDBObject nextQuery = idTemplate(".next");
    private BasicDBObject nextInc = inc(KEY_SPACE);

    public MongoWordSpaceStorage(JSONObject config) {
        super(config);
    }

    private int create(String word) {
        int id = find(word);
        if (id > 0) return id;
        DBObject nextDoc = collection.findAndModify(nextQuery, null, null, false, nextInc, true, true);
        int nextId = (Integer) nextDoc.get(KEY_SPACE);
        DBObject wordDoc = collection.findAndModify(idTemplate(word), null, null, false, buildBasic(KEY_ID, word, KEY_WORDID, nextId), true, true);
        return getWordId(wordDoc);
    }

    private int getWordId(DBObject doc) {
        if (doc.containsField(KEY_WORDID)) return (Integer) doc.get(KEY_WORDID);
        return 0;
    }

    @Override
    public int findOrCreate(String word) {
        return create(word);
    }

    @Override
    public int find(String word) {
        DBObject wordDoc = collection.findOne(idTemplate(word));
        if (wordDoc == null) return 0;
        return getWordId(wordDoc);
    }

    @Override
    public void findAll(FindAllCallback callback) {
        DBCursor cursor = collection.find(exists(KEY_WORDID));
        while (cursor.hasNext()) {
            DBObject doc = cursor.next();
            if (doc == null) continue;
            int wordId = getWordId(doc);
            if (wordId < 1) continue;
            callback.foundPair((String) doc.get(KEY_ID), wordId);
        }
    }
}
