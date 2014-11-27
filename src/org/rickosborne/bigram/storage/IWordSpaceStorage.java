package org.rickosborne.bigram.storage;

public interface IWordSpaceStorage {

    public interface FindAllCallback {
        public boolean foundPair(String word, int index);
    }

    public int findOrCreate(String word);
    public int find(String word);
    public void findAll(FindAllCallback callback);

}
