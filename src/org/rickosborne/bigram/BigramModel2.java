package org.rickosborne.bigram;

import org.rickosborne.bigram.predictor.*;
import org.rickosborne.bigram.storage.*;
import org.rickosborne.bigram.storage.jdbc.JdbcBigramStorage;
import org.rickosborne.bigram.storage.jdbc.JdbcDictionaryStorage;
import org.rickosborne.bigram.storage.jdbc.JdbcTrigramStorage;
import org.rickosborne.bigram.storage.memory.MemoryBigramStorage;
import org.rickosborne.bigram.storage.memory.MemoryDictionaryStorage;
import org.rickosborne.bigram.storage.memory.MemoryTrigramStorage;
import org.rickosborne.bigram.storage.redis.*;
import org.rickosborne.bigram.storage.sqlite.SqliteBigramStorage;
import org.rickosborne.bigram.storage.sqlite.SqliteDictionaryStorage;
import org.rickosborne.bigram.storage.sqlite.SqliteTrigramStorage;
import org.rickosborne.bigram.util.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

public class BigramModel2 {

//    private String[] names = { "dictionary", "bigram", "trigram" };
    private String[] names = { "ngram" };
    private double[] weights;
    private IWordPredictor[] predictors;
    private OutputStreamWriter logger = null;
    private IWordSpace wordSpace;

    public BigramModel2(Config config) {
//        predictors = new IWordPredictor[3];
//        IDictionaryStorage dictionaryStorage;
//        IBigramStorage bigramStorage;
//        ITrigramStorage trigramStorage;
        predictors = new IWordPredictor[1];
        INgramStorage ngramStorage;
        ngramStorage = new RedisNgramStorage(config);
        IWordSpaceStorage wordSpaceStorage = new RedisWordSpaceStorage(config);
        String storageType = config.get("storageType", "sqlite");
        if (storageType.equals("memory")) {
//                dictionaryStorage = new MemoryDictionaryStorage();
//                bigramStorage = new MemoryBigramStorage();
//                trigramStorage = new MemoryTrigramStorage();
        }
        else if (storageType.equals("jdbc")) {
            String jdbcUrl = config.get("jdbcUrl", "jdbc:mysql://localhost:3306/words");
//                dictionaryStorage = new JdbcDictionaryStorage(jdbcUrl);
//                bigramStorage = new JdbcBigramStorage(jdbcUrl);
//                trigramStorage = new JdbcTrigramStorage(jdbcUrl);
        }
        else if (storageType.equals("redis")) {
//                dictionaryStorage = new RedisDictionaryStorage(config);
//                bigramStorage = new RedisBigramStorage(config);
//                trigramStorage = new RedisTrigramStorage(config);
            ngramStorage = new RedisNgramStorage(config);
//            wordSpaceStorage = new RedisWordSpaceStorage(config);
        }
        else {
//                dictionaryStorage = new SqliteDictionaryStorage(config.get("dictionarySqliteFile", "jdbc:sqlite:words-dict.sqlite"));
//                bigramStorage = new SqliteBigramStorage(config.get("bigramSqliteFile", "jdbc:sqlite:words-bi.sqlite"));
//                trigramStorage = new SqliteTrigramStorage(config.get("trigramSqliteFile", "jdbc:sqlite:words-tri.sqlite"));
        }
        String dictionaryFile = config.get("dictionaryFile", "");
        if (config.get("learnWords", "false").equals("true")) wordSpace = new LearningWordSpace(wordSpaceStorage, dictionaryFile);
        else wordSpace = new StaticWordSpace(wordSpaceStorage, dictionaryFile);
//        predictors[0] = new DictionaryPredictor(dictionaryStorage);
//        predictors[1] = new BigramPredictor(bigramStorage);
//        predictors[2] = new TrigramPredictor(trigramStorage);
        predictors[0] = new NgramPredictor(ngramStorage, wordSpace, config);
        weights = new double[1];
        weights[0] = config.get("ngramWeight", 300);
//        weights = new double[3];
//        weights[0] = config.get("dictionaryWeight", 200);
//        weights[1] = config.get("bigramWeight", 220);
//        weights[2] = config.get("trigramWeight", 240);
    }

    private class WeightedWordList extends WordList {
        public Prediction predict() { return this.predict(null); }
    }

    private void log(String message) {
        try {
            if (this.logger != null) this.logger.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLogger(OutputStreamWriter logger) {
        this.logger = logger;
        for (String name : names) {
            log(name + " Guess\t" + name + " Correct?\t" + name + " Seen\t" + name + " Size\t" + name + " Vote\t");
        }
    }

    public void learn(String[] words) {
        for (IWordPredictor predictor : predictors) {
            predictor.learn(words);
        }
    }

    public WordList guess(String[] words, String partial, String answer) {
        WordList guesses = new WeightedWordList();
        for (int i = 0, predictorCount = predictors.length; i < predictorCount; i++) {
            Prediction guess = predictors[i].predict(words, partial);
            if ((guess != null) && (guess.getWord() != null)) {
                int seen = guess.getSeen();
                int size = guess.getSize();
                int vote = (int) Math.round(seen * this.weights[i] / size);
                String word = guess.getWord();
                boolean correct = word.equals(answer);
                log(String.format(
                        "%s\t%s\t%d\t%d\t%d\t",
                        word,
                        correct ? "Y" : "N",
                        seen,
                        size,
                        vote
                ));
                guesses.learn(word, vote);
            }
            else {
                log("\t\t\t\t\t");
            }
        }
        return guesses;
    }

    public String predict(String[] words, String partial, String answer) {
        WordList guesses = guess(words, partial, answer);
        Prediction prediction = guesses.predict(null);
        if (prediction == null) return null;
        return prediction.getWord();
    }

}
