package org.rickosborne.bigram.storage.mongo;

import com.mongodb.*;
import org.json.JSONObject;
import org.rickosborne.bigram.storage.INgramStorage;
import org.rickosborne.bigram.util.Prediction;

import java.util.*;

public class MongoNgramStorage extends MongoStorage implements INgramStorage {

    private static final String KEY_TARGETS = ">";
    private static final String DOT = ".";
    private static final String TARGET = KEY_TARGETS + DOT;

    public MongoNgramStorage(JSONObject config) {
        super(config);
    }

    private DBObject docForId(String id) {
        BasicDBObject template = idTemplate(id);
        DBObject doc = collection.findOne(id);
        if (doc == null) {
            template.append(KEY_TARGETS, new BasicDBObject());
            collection.insert(template, WriteConcern.ACKNOWLEDGED);
            doc = collection.findOne(idTemplate(id));
        }
        return doc;
    }

    private static BasicDBObject newLevel() {
        return new BasicDBObject(KEY_TARGETS, new BasicDBObject());
    }

    private static BasicDBObject incTarget(String path, String target) {
        return inc(path + (path.isEmpty() ? "" : DOT) + TARGET + target);
    }

    private static BasicDBObject zeroIfNull(String path, String target) {
        String targetPath = path + TARGET + target;
        BasicDBList list = new BasicDBList();
        list.add(targetPath);
        list.add(0);
        return new BasicDBObject(KEY_EVAL + targetPath, new BasicDBObject(KEY_IFNULL, list));
    }

    private static BasicDBObject newLevelIfNull(String path, String target) {
        String targetPath = path + (path.isEmpty() ? "" : ".") + TARGET + target;
        BasicDBList list = new BasicDBList();
        list.add(KEY_EVAL + targetPath);
        list.add(newLevel());
        return new BasicDBObject(KEY_EVAL + targetPath, new BasicDBObject(KEY_IFNULL, list));
    }

    private static BasicDBObject docHasNo(String id, String path) {
        BasicDBObject doc = idTemplate(id);
        doc.append(path, new BasicDBObject(KEY_EXISTS, false));
        return doc;
    }

    @Override
    public void add(String[] words) {
        int l = words.length;
        if (l < 2) return;
        int wordCount = Math.min(maxWords, words.length - 2);
        String target = words[l - 1];
        String docId = words[l - 2];
        docForId(docId);
        BasicDBObject idDoc = idTemplate(docId);
        // want to go to park // doc = to // target = park
        // no word // ">.park": { $ifNull: ... } // $inc: { ">.park": 1 } // word = go
        // "go": { $ifNull: [ "$go", {">":{}} ] }
        // word 1: go // "go.>.park": { $ifNull: [ "$go.>.park", 0 ] } // $inc: { "go.>.park": 1 }
        // "go.to": { $ifNull: [ "$go.to", {">":{}} ] }
        // word 2: to // "go.to.>.park": { $ifNull: ... } // $inc: { "go.to.>park": 1 }
        // "go.to.want": { $ifNull: ... }
        String path = "", word = "";
        int i = 0;
        BulkWriteOperation builder = collection.initializeOrderedBulkOperation();
        do {
            if (i > 0) word = words[l - i - 2];
            i++;
            if (!word.isEmpty()) {
                if (!path.isEmpty()) path += ".";
                path += word;
                builder.find(docHasNo(docId, path)).updateOne(set(path, newLevel()));
            }
//            builder.find(idDoc).updateOne(zeroIfNull(path, target));
            builder.find(idDoc).updateOne(incTarget(path, target));
        } while (i < wordCount);
        builder.execute();
    }

    @Override
    public Prediction get(String[] words, String partial) {
        if (words.length < 1) return null;
        String lastWord = words[words.length - 1];
        DBObject doc = collection.findOne(idTemplate(lastWord));
        if (doc == null) return null;
        int wordIndex = words.length - 2;
        while (wordIndex >= 0) {
            String word = words[wordIndex];
            if (doc.containsField(word)) doc = (DBObject) doc.get(word);
            else wordIndex = 0;
            wordIndex--;
        }
        DBObject targets = (DBObject) doc.get(KEY_TARGETS);
        Set<String> targetWords = targets.keySet();
        if ((partial != null) && !partial.isEmpty()) {
            int partialLength = partial.length();
            HashSet<String> newTargetWords = new HashSet<String>();
            for (String word : targetWords) {
                if ((word.length() >= partialLength) && word.substring(0, partialLength).equals(partial))
                    newTargetWords.add(word);
            }
            targetWords = newTargetWords;
        }
        int maxSeen = 0, totalSeen = 0, seen;
        String guess = null;
        for (String word : targetWords) {
            seen = (Integer) targets.get(word);
            totalSeen += seen;
            if (seen > maxSeen) {
                maxSeen = seen;
                guess = word;
            }
        }
        if (guess != null) return new Prediction(guess, maxSeen, totalSeen);
        return null;
    }
}
