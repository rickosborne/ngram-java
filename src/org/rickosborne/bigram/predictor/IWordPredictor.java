package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;

public interface IWordPredictor {

    public void learn (String[] words);

    public Prediction predict (String[] words, String partial);

}
