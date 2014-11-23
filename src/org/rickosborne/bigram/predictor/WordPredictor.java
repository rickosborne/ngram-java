package org.rickosborne.bigram.predictor;

import org.rickosborne.bigram.util.Prediction;

import java.sql.SQLException;

public interface WordPredictor {

    public void learn (String[] words) throws SQLException;

    public Prediction predict (String[] words, String partial) throws SQLException;

}
