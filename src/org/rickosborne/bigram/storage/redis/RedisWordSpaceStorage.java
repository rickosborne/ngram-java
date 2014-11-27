package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.storage.IWordSpaceStorage;
import org.rickosborne.bigram.util.Config;

public class RedisWordSpaceStorage extends RedisStorage implements IWordSpaceStorage {

    protected final static int DB_INDEX = 0;
    protected final static String NEXT_KEY = ".next";
    protected final static String GLOB_ALL = "*";
    protected final static char HIDDEN_PREFIX = '.';

    public RedisWordSpaceStorage(Config config) {
        super(config);
        jedis.select(DB_INDEX);
    }

    protected int create(String word) {
        if ((word == null) || word.isEmpty() || (word.charAt(0) == HIDDEN_PREFIX)) return 0;
        int foundId = find(word);
        if (foundId > 0) return foundId;
        long nextId = tryIncr(NEXT_KEY);
        if (nextId == 0) return 0;
        if (jedis.setnx(word, String.valueOf(nextId)) != 1) return Integer.valueOf(jedis.get(word), 10);
        return (int) nextId;
        /*
        int intId = 0;
        String key = lockDB(DB_INDEX);
        if (key == null) return 0;
        String foundId = jedis.get(word);
        if ((foundId != null) && !foundId.isEmpty()) {
            intId = Integer.valueOf(foundId, 10);
        } else {
            Long longId = jedis.incr(NEXT_KEY);
            if (longId != null) {
                jedis.set(word, longId.toString());
                intId = longId.intValue();
            }
        }
        unlockDB(DB_INDEX, key);
        return intId;
        */
    }

    @Override
    public int findOrCreate(String word) {
        return create(word);
    }

    @Override
    public int find(String word) {
        String foundId = jedis.get(word);
        if (foundId != null) return Integer.valueOf(foundId, 10);
        return 0;
    }

    @Override
    public void findAll(FindAllCallback callback) {
        for (String key : jedis.keys(GLOB_ALL)) {
            if ((key == null) || key.isEmpty() || (key.charAt(0) == HIDDEN_PREFIX)) continue;
            int value = Integer.parseInt(jedis.get(key));
            if (!callback.foundPair(key, value)) break;
        }
    }
}
