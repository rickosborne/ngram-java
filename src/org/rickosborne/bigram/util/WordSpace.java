package org.rickosborne.bigram.util;

import org.rickosborne.bigram.storage.IWordSpaceStorage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class WordSpace implements IWordSpace {

    HashMap<String, Integer> cache = new HashMap<String, Integer>();
    IWordSpaceStorage storage;

    public WordSpace(IWordSpaceStorage storage, String dictionaryFile) {
        this.storage = storage;
        loadFromStorage();
        if ((dictionaryFile != null) && !dictionaryFile.isEmpty()) loadFromFile(dictionaryFile);
    }

    protected void loadFromStorage() {
        storage.findAll(new IWordSpaceStorage.FindAllCallback() {
            @Override
            public boolean foundPair(String word, int index) {
                if ((index > 0) && !cache.containsKey(word)) cache.put(word, index);
                return true;
            }
        });
    }

    protected void loadFromFile(String dictionaryFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dictionaryFile));
            String line;
            do {
                line = reader.readLine();
                if ((line == null) || line.isEmpty()) continue;
                String word = line.toLowerCase();
                if ((word.charAt(0) == '.') || cache.containsKey(word)) continue;
                int wordId = storage.findOrCreate(word);
                if (wordId == 0) continue;
                cache.put(word, wordId);
            } while (line != null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // don't care
        }
    }

    @Override
    public int idForWord(String word) {
        Integer id = cache.get(word);
        if (id == null) return 0;
        return id;
    }
}
