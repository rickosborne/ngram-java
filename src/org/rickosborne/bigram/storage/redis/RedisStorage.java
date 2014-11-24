package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;
import redis.clients.jedis.Jedis;

import java.net.SocketTimeoutException;
import java.util.Set;

public class RedisStorage {

    protected Jedis jedis;

    public RedisStorage(Config config) {
        jedis = new Jedis(config.get("redisHost", "localhost"), config.get("redisPort", 6379), config.get("redisTimeout", 30000));
        jedis.clientSetname(this.getClass().getSimpleName());
    }

    protected void selectDB(int dbIndex) {
        jedis.select(dbIndex);
    }

    protected void tryIncr(String key) {
        int attempts = 0;
        boolean success = false;
        while (!success && (attempts < 10)) {
            attempts++;
            try {
                jedis.incr(key);
                success = true;
            } catch (Exception e) {
                try {
                    System.out.print('!');
                    System.out.flush();
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

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
