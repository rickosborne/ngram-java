package org.rickosborne.bigram;

import org.rickosborne.bigram.util.LineReader;

import java.util.HashMap;
import java.util.Map;

public class BigramModel {

    private class PairSet {
        private HashMap<String, Long> followers = new HashMap<String, Long>();

        public void learn (String follower) {
            followers.put(follower, 1 + (followers.containsKey(follower) ? followers.get(follower) : 0));
        }

        public String predict () {
            long most = 0;
            String prediction = null;
            for (Map.Entry<String, Long> pair : followers.entrySet()) {
                long seen = pair.getValue();
                if (seen > most) {
                    most = seen;
                    prediction = pair.getKey();
                }
            }
            return prediction;
        }

        public String predict (String knownPart) {
            long most = 0;
            String prediction = null;
            for (Map.Entry<String, Long> pair : followers.entrySet()) {
                long seen = pair.getValue();
                if (seen > most) {
                    String key = pair.getKey();
                    if (key.substring(0, knownPart.length()).equals(knownPart)) {
                        prediction = key;
                        most = seen;
                    }
                }
            }
            return prediction;
        }
    }

    private class PairModel {
        HashMap<String, PairSet> sets = new HashMap<String, PairSet>();

        public void learn (String first, String second) {
            if (!sets.containsKey(first)) sets.put(first, new PairSet());
            PairSet previousSet = sets.get(first);
            previousSet.learn(second);
        }

        public String predict (String leader) {
            if (!sets.containsKey(leader)) return null;
            return sets.get(leader).predict();
        }

        public String predict (String leader, String followPart) {
            if (!sets.containsKey(leader)) return null;
            return sets.get(leader).predict(followPart);
        }

        public long wordCount () {
            return sets.size();
        }
    }

    public class TestResult {
        public long pairsTrained = 0;
        public long startWords = 0;
        public long pairsTested = 0;
        public long correctPredictions = 0;
        public long incorrectPredictions = 0;
        public long correctAfter1 = 0;
    }

    private PairModel model = new PairModel();
    private TestResult result = new TestResult();

    public void learnLines(LineReader.Iterator iterator) {
        String line = iterator.next();
        while (line != null) {
//            System.out.print("-");
//            System.out.flush();
            String previous = null;
            for (String part : line.split("\\W+")) {
                if ((part != null) && !part.isEmpty() && (previous != null) && !previous.isEmpty()) {
                    result.pairsTrained++;
                    model.learn(previous, part);
                }
                if ((part != null) && !part.isEmpty()) {
                    previous = part;
                }
            }
            line = iterator.next();
        }
        result.startWords = model.wordCount();
//        System.out.print("\n");
//        System.out.flush();
    }

    public TestResult testLines(LineReader.Iterator iterator) {
        String line = iterator.next();
        while (line != null) {
//            System.out.print("?");
//            System.out.flush();
            String previous = null;
            for (String part : line.split("\\W+")) {
                if ((part != null) && !part.isEmpty() && (previous != null) && !previous.isEmpty()) {
                    String prediction = model.predict(previous);
                    result.pairsTested++;
                    if ((prediction != null) && prediction.equals(part)) result.correctPredictions++;
                    else {
                        result.incorrectPredictions++;
                        prediction = model.predict(previous, part.substring(0, 1));
                        if ((prediction != null) && prediction.equals(part)) result.correctAfter1++;
                    }
                }
                if ((part != null) && !part.isEmpty()) {
                    previous = part;
                }
            }
            line = iterator.next();
        }
//        System.out.print("\n");
//        System.out.flush();
        return result;
    }

}
