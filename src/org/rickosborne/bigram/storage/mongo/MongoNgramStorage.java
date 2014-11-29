package org.rickosborne.bigram.storage.mongo;

import com.mongodb.*;
import org.json.JSONObject;
import org.rickosborne.bigram.storage.INgramStorage;
import org.rickosborne.bigram.util.Prediction;

import java.util.*;

import static org.rickosborne.bigram.util.Util.join;

public class MongoNgramStorage extends MongoStorage implements INgramStorage {

    private static final String KEY_TARGETS = ">";
    private static final String DOT = ".";
    private static final String TARGET = KEY_TARGETS + DOT;

    public MongoNgramStorage(JSONObject config) {
        super(config);
    }

    private DBObject docForId(String id) {
        try {
            BasicDBObject template = idTemplate(id);
            DBObject doc = collection.findOne(id);
            if (doc == null) {
                template.append(KEY_TARGETS, new BasicDBObject());
                collection.insert(template, WriteConcern.ACKNOWLEDGED);
                doc = collection.findOne(idTemplate(id));
            }
            return doc;
        } catch (ClassCastException e) {
            System.out.print("!(" + id + ")");
            System.out.flush();
        }
        return null;
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
        if (docForId(docId) == null) return;
        BasicDBObject idDoc = idTemplate(docId);
        // 0.want 1.to 2.go 3.to -> 4.park // doc = to // target = park // l = 5
        // $incr: {
        //   ">.park": 1,
        //   "go.>.park": 1,
        //   "go.to.>.park": 1,
        //   "go.to.want.>.park": 1
        // }
        String path = "", pathEnd = TARGET + target;
        BasicDBObject updateIncs = new BasicDBObject(pathEnd, 1);
        for (int wordNum = 1; wordNum <= wordCount; wordNum++) {
            path += words[l - wordNum - 2] + ".";
            updateIncs.append(path + pathEnd, 1);
        }
        collection.update(idDoc, new BasicDBObject(KEY_INC, updateIncs));
    }

    @Override
    public Prediction get(String[] words, String partial) {
        if (words.length < 1) return null;
        String lastWord = words[words.length - 1], firstWord = "";
        DBObject doc = collection.findOne(idTemplate(lastWord));
        if (doc == null) return null;
        int wordIndex = words.length - 2, depth = 0;
        LinkedList<String> matched = new LinkedList<String>();
        matched.addFirst(lastWord);
        while (wordIndex >= 0) {
            String word = words[wordIndex];
            if (doc.containsField(word)) {
                doc = (DBObject) doc.get(word);
                depth++;
                firstWord = word;
                matched.addFirst(word);
            }
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
        Prediction prediction = new Prediction();
        prediction.addOption(String.format("$depth=%d", depth), 0);
        prediction.addOption(String.format("$lastWord=%s", lastWord), 0);
        prediction.addOption(String.format("$firstWord=%s", firstWord), 0);
        prediction.addOption(String.format("$matched=%s", join(" ", matched.toArray(new String[matched.size()]))), 0);
        String guess = null;
        for (String word : targetWords) {
            seen = (Integer) targets.get(word);
            totalSeen += seen;
            prediction.addOption(word, seen);
            if (seen > maxSeen) {
                maxSeen = seen;
                guess = word;
            }
        }
        prediction.setWord(guess, maxSeen, totalSeen);
        if (guess != null) return prediction;
        return null;
    }
}
