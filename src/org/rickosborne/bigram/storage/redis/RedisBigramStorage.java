package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.storage.IBigramStorage;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;

public class RedisBigramStorage extends RedisStorage implements IBigramStorage {

    public RedisBigramStorage(Config config) {
        super(config);
        selectDB(2);
    }

    @Override
    public void add(String firstWord, String secondWord) throws SQLException {
        if ((firstWord == null) || firstWord.isEmpty() || (secondWord == null) || secondWord.isEmpty()) return;
        tryIncr(firstWord + " " + secondWord);
    }

    @Override
    public Prediction get(String firstWord, String partial) throws SQLException {
        if ((firstWord == null) || firstWord.isEmpty()) return null;
        String key = firstWord + " " + ((partial != null) && !partial.isEmpty() ? partial : "") + "*";
        return predictionFromKeys(jedis.keys(key));
    }
}
