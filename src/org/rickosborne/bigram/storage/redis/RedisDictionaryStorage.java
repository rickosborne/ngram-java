package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.storage.IDictionaryStorage;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;

public class RedisDictionaryStorage extends RedisStorage implements IDictionaryStorage {

    public RedisDictionaryStorage(Config config) {
        super(config);
        selectDB(1);
    }

    @Override
    public void add(String word) {
        if ((word == null) || word.isEmpty()) return;
        tryIncr(word);
    }

    @Override
    public Prediction get(String partial) {
        if ((partial == null) || partial.isEmpty()) return null;
        return predictionFromKeys(jedis.keys(partial + "*"));
    }
}
