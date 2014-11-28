package org.rickosborne.bigram.storage.redis;

import org.rickosborne.bigram.storage.INgramStorage;
import org.rickosborne.bigram.util.Config;
import org.rickosborne.bigram.util.Prediction;

public class RedisNgramStorage extends RedisStorage implements INgramStorage {



    public RedisNgramStorage(Config config) {
        super(config);
    }

    protected static String join(String delimiter, String[] words) {
        StringBuilder builder = new StringBuilder(words.length * 2);
        boolean firstWord = true;
        for (String word : words) {
            if (firstWord) firstWord = false;
            else builder.append(delimiter);
            builder.append(word);
        }
        return builder.toString();
    }

    @Override
    public void add(String[] words) {
        jedis.select(words.length);
        tryIncr(join(" ", words));
    }

    @Override
    public Prediction get(String[] words, String partial) {
        String prefix = join(" ", words);
        String key = (prefix.isEmpty() ? "" : prefix + " ") + ((partial != null) && !partial.isEmpty() ? partial : "") + "*";
        if (key.isEmpty()) return null;
        jedis.select(words.length + 1);
        return predictionFromKeys(jedis.keys(key));
    }
}
