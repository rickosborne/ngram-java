package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.storage.INgramStorage;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;

public class RedisNgramStorage extends RedisStorage implements INgramStorage {

    public RedisNgramStorage(Config config) {
        super(config);
    }

    @Override
    public void add(int wordCount, String words) {
        jedis.select(wordCount);
        tryIncr(words);
    }

    @Override
    public Prediction get(int wordCount, String words, String partial) {
        String key = ((words == null) || words.isEmpty() ? "" : words + " ") + ((partial != null) && !partial.isEmpty() ? partial : "") + "*";
        if (key.isEmpty()) return null;
        jedis.select(wordCount);
        return predictionFromKeys(jedis.keys(key));
    }
}
