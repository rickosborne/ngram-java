package org.rickosborne.bigram.util;

public class Prediction {

    private String word;
    private int seen;
    private int size;

    public Prediction (String word, int seen, int size) {
        this.word = word;
        this.seen = seen;
        this.size = size;
    }

    public String getWord () { return this.word; }
    public int getSeen() { return this.seen; }
    public int getSize() { return this.size; }
    public double getChance() { return (double) this.seen / (double) this.size; }

}
