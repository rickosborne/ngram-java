package org.rickosborne.bigram;

import org.json.JSONObject;
import org.rickosborne.bigram.predictor.IWordPredictor;
import org.rickosborne.bigram.storage.IWordSpaceStorage;
import org.rickosborne.bigram.util.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import static org.rickosborne.bigram.util.Util.titleCase;
import static org.rickosborne.bigram.util.Util.wordsFromLine;

public class BigramModel2 {

    private String[] names;
    private double[] weights;
    private IWordPredictor[] predictors;
    private OutputStreamWriter logger = null;
    private IWordSpace wordSpace;
    private final static String PKG_NAME = BigramModel2.class.getPackage().getName();
    private final static String PKG_PREDICTOR = PKG_NAME + ".predictor.";
    private final static String PKG_STORAGE = PKG_NAME + ".storage.";
    private final static String PKG_UTIL = PKG_NAME + ".util.";

    private IWordPredictor buildPredictor(String name, JSONObject options, Config config) {
        try {
            String storageName = options.optString("storage", "memory");
            if ((storageName == null) || storageName.isEmpty()) return null;
            Class<?> predictorClass = Class.forName(PKG_PREDICTOR + titleCase(name) + "Predictor");
            if (predictorClass == null) return null;
            Class<?> storageClass = Class.forName(PKG_STORAGE + storageName.toLowerCase() + "." + titleCase(storageName) + titleCase(name) + "Storage");
            if (storageClass == null) return null;
            Class[] interfaces = storageClass.getInterfaces();
            Class storageInterface = interfaces[0];
            for (Class iface : interfaces) if (iface.getSimpleName().contains("Storage")) storageInterface = iface;
            Constructor predictorConstructor = predictorClass.getDeclaredConstructor(storageInterface, IWordSpace.class, config.getClass());
            if (predictorConstructor == null) return null;
            Constructor storageConstructor;
            try {
                storageConstructor = storageClass.getDeclaredConstructor(Config.class);
                return (IWordPredictor) predictorConstructor.newInstance(storageConstructor.newInstance(config), wordSpace, config);
            } catch (NoSuchMethodException e) {
                storageConstructor = storageClass.getDeclaredConstructor(JSONObject.class);
                return (IWordPredictor) predictorConstructor.newInstance(storageConstructor.newInstance(options), wordSpace, config);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearStopWords (final IWordSpace wordSpace, JSONObject options) {
        String stopWords = options.optString("stopWords", "");
        String stopWordsFile = options.optString("stopWordsFile", "");
        if ((stopWordsFile != null) && !stopWordsFile.isEmpty()) {
            LineReader reader = new LineReader(stopWordsFile);
            reader.read(new LineReader.ILineHandler() {
                @Override
                public boolean handleLine(String line) {
                    wordSpace.removeWord(line.toLowerCase());
                    return true;
                }
            });
        }
        if ((stopWords != null) && !stopWords.isEmpty()) {
            for (String word : stopWords.split("\\s+")) {
                wordSpace.removeWord(word);
            }
        }
    }

    private IWordSpace buildWordSpace(Config config) {
        JSONObject options = config.getObject("wordSpace");
        String storageType = options.optString("storage", "memory");
        Constructor storageConstructor;
        IWordSpaceStorage storage;
        try {
            Class<?> storageClass = Class.forName(PKG_STORAGE + storageType.toLowerCase() + "." + titleCase(storageType) + "WordSpaceStorage");
            try {
                storageConstructor = storageClass.getDeclaredConstructor(Config.class);
                storage = (IWordSpaceStorage) storageConstructor.newInstance(config);
            } catch (NoSuchMethodException e) {
                storageConstructor = storageClass.getDeclaredConstructor(options.getClass());
                storage = (IWordSpaceStorage) storageConstructor.newInstance(options);
            }
            String dictionaryFile = options.optString("dictionaryFile", "");
            String spaceType = options.optString("type", "static");
            Class<?> typeClass = Class.forName(PKG_UTIL + titleCase(spaceType) + "WordSpace");
            Constructor typeConstructor = typeClass.getDeclaredConstructor(IWordSpaceStorage.class, String.class);
            IWordSpace space = (IWordSpace) typeConstructor.newInstance(storage, dictionaryFile);
            clearStopWords(space, options);
            return space;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void buildPredictors(Config config) {
        JSONObject predictorConfig = config.getObject("predictors");
        ArrayList<IWordPredictor> predictorList = new ArrayList<IWordPredictor>();
        ArrayList<Double> weightList = new ArrayList<Double>();
        ArrayList<String> nameList = new ArrayList<String>();
        for (String predictorName : JSONObject.getNames(predictorConfig)) {
            JSONObject options = predictorConfig.optJSONObject(predictorName);
            if (options == null) options = new JSONObject();
            IWordPredictor predictor = buildPredictor(predictorName, options, config);
            if (predictor == null) continue;
            predictorList.add(predictor);
            weightList.add((double) options.optInt("weight", 100));
            nameList.add(predictorName);
        }
        predictors = predictorList.toArray(new IWordPredictor[predictorList.size()]);
        names = nameList.toArray(new String[nameList.size()]);
        weights = new double[weightList.size()];
        for (int i = 0; i < weightList.size(); i++) weights[i] = weightList.get(i);
    }

    public BigramModel2(Config config) {
        wordSpace = buildWordSpace(config);
        buildPredictors(config);
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

    public String[] splitWords(String words) {
        LinkedList<String> knownWords = new LinkedList<String>();
        for (String word : wordsFromLine(words)) {
            if (wordSpace.idForWord(word) > 0) knownWords.add(word);
        }
        return knownWords.toArray(new String[knownWords.size()]);
    }

    public WordList guess(String words, String partial, String answer) {
        return guess(splitWords(words), partial, answer);
    }

    public WordList guess(String[] words, String partial, String answer) {
        WordList guesses = new WeightedWordList();
        for (int i = 0, predictorCount = predictors.length; i < predictorCount; i++) {
            Prediction guess = predictors[i].predict(words, partial);
            if ((guess != null) && (guess.getWord() != null)) {
                int seen = guess.getSeen(),
                    size = guess.getSize();
                double weight = predictors.length == 1 ? 1.0 : (this.weights[i] / size);
                int vote = (int) Math.round(seen * weight);
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
                for (Map.Entry<String,Integer> pair : guess.getOptions().entrySet()) {
                    guesses.learn(pair.getKey(), (int) Math.round(pair.getValue() * weight));
                }
                // guesses.learn(word, vote);
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
