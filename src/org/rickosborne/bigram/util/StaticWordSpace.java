package org.rickosborne.bigram.util;

import org.rickosborne.bigram.storage.IWordSpaceStorage;

public class StaticWordSpace extends WordSpace {

    public StaticWordSpace(IWordSpaceStorage storage, String dictionaryFile) {
        super(storage, dictionaryFile);
    }

}
