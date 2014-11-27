package org.rickosborne.bigram.util;

import org.rickosborne.bigram.storage.IWordSpaceStorage;

public class LearningWordSpace extends WordSpace {

    public LearningWordSpace(IWordSpaceStorage storage, String dictionaryFile) {
        super(storage, dictionaryFile);
    }

    @Override
    public int idForWord(String word) {
        Integer id = cache.get(word);
        if (id != null) return id;
        id = storage.findOrCreate(word);
        cache.put(word, id);
        return id;
    }

}
