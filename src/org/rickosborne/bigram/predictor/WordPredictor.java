package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.util.Prediction;

public interface WordPredictor {

    public void learn (String[] words);

    public Prediction predict (String[] words, String partial);

}
