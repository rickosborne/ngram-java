package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.storage.ITrigramStorage;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;

public class RedisTrigramStorage extends RedisStorage implements ITrigramStorage {

    public RedisTrigramStorage(Config config) {
        super(config);
        selectDB(3);
    }

    @Override
    public void add(String firstWord, String secondWord, String thirdWord) {
        if ((firstWord == null) || firstWord.isEmpty() || (secondWord == null) || secondWord.isEmpty() || (thirdWord == null) || thirdWord.isEmpty()) return;
        tryIncr(firstWord + " " + secondWord + " " + thirdWord);
    }

    @Override
    public Prediction get(String firstWord, String secondWord, String partial) {
        if ((firstWord == null) || firstWord.isEmpty() || (secondWord == null) || secondWord.isEmpty()) return null;
        String key = firstWord + " " + secondWord + " " + ((partial != null) && !partial.isEmpty() ? partial : "") + "*";
        return predictionFromKeys(jedis.keys(key));
    }
}
