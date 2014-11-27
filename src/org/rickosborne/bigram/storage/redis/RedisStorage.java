package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.UUID;

public class RedisStorage {

    protected Jedis jedis;
    protected final static String LOCK_KEY = ".lock";
    protected final static int RETRY_COUNT = 10;
    protected final static int LOCK_RETRY_DELAY = 100;
    protected final static int WRITE_RETRY_DELAY = 1000;

    public RedisStorage(Config config) {
        jedis = new Jedis(config.get("redisHost", "localhost"), config.get("redisPort", 6379), config.get("redisTimeout", 30000));
        jedis.clientSetname(this.getClass().getSimpleName());
    }

    protected void selectDB(int dbIndex) {
        jedis.select(dbIndex);
    }

    protected long tryIncr(String key) {
        int attempts = 0;
        while (attempts < RETRY_COUNT) {
            attempts++;
            try {
                return jedis.incr(key);
            } catch (Exception e) {
                try {
                    System.out.print('!');
                    System.out.flush();
                    Thread.sleep(WRITE_RETRY_DELAY);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return 0;
    }

    /*
    protected String lockDB(int dbIndex) {
        jedis.select(dbIndex);
        int attempts = 0;
        String lockValue = UUID.randomUUID().toString();
        while (attempts < RETRY_COUNT) {
            attempts++;
            try {
                if (jedis.setnx(LOCK_KEY, String.valueOf(lockValue)) == 1) return lockValue;
                Thread.sleep(LOCK_RETRY_DELAY);
            } catch (Exception e) {
              e.printStackTrace();
            }
        }
        return null;
    }

    protected void unlockDB(int dbIndex, String key) {
        jedis.select(dbIndex);
        int attempts = 0;
        while (attempts < RETRY_COUNT) {
            attempts++;
            try {
                String lockValue = jedis.get(LOCK_KEY);
                if (lockValue.equals(key)) jedis.del(LOCK_KEY);
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }
    */

    protected Prediction predictionFromKeys(Set<String> keys) {
        if ((keys == null) || keys.isEmpty()) return null;
        int maxSeen = 0, totalSeen = 0, seen;
        String word = null;
        for (String key : keys) {
            seen = Integer.parseInt(jedis.get(key), 10);
            totalSeen += seen;
            if (seen > maxSeen) {
                word = key;
                maxSeen = seen;
            }
        }
        if (word == null) return null;
        String[] words = word.split(" ");
        return new Prediction(words[words.length - 1], maxSeen, totalSeen);
    }

}
