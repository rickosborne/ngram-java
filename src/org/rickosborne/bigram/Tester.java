package org.rickosborne.bigram;

import org.rickosborne.bigram.util.LineReader;
import org.rickosborne.bigram.util.TestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static org.rickosborne.bigram.util.Util.wordsFromLine;

public class Tester {

    private static int maxLetters = 10;

    private BigramModel2 model;
    private TestResult result;
    private OutputStreamWriter logger;

    private static String join(String[] words) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String word : words) {
            if (!first) {
                sb.append(" ");
            } else {
                first = false;
            }
            sb.append(word);
        }
        return sb.toString();
    }

    public Tester(BigramModel2 model, TestResult result, OutputStream log) {
        this.model = model;
        this.result = result;
        this.logger = log == null ? null : new OutputStreamWriter(log);
        log("Trial\tWords\tPartial\tAnswer\t");
        model.setLogger(this.logger);
        log("Winner\tCorrect?\tLetters\tGave Up\n");
    }

    private void log(String message) {
        if (this.logger != null) try {
            this.logger.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepare() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        result.memoryBefore = runtime.totalMemory() - runtime.freeMemory();
    }

    public void train (LineReader lineReader) {
        prepare();
        lineReader.read(new LineReader.ILineHandler() {
            @Override
            public boolean handleLine(String line) {
                result.linesTrained++;
                model.learn(wordsFromLine(line));
                return true;
            }
        });
    }

    public void train (LineReader.TrainTestIterator iterator) {
        prepare();
        String line = iterator.next();
        while (line != null) {
            result.linesTrained++;
            this.model.learn(wordsFromLine(line));
            line = iterator.next();
        }
    }

    public void test (LineReader.TrainTestIterator iterator) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        result.memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        String line = iterator.next();
        while (line != null) {
            result.linesTested++;
            String[] words = wordsFromLine(line);
            if (words.length > 1) {
                for (int wordN = 1, wordCount = words.length; wordN < wordCount; wordN++) {
                    String word = words[wordN];
                    if (word.charAt(0) == '$') continue;
                    result.wordsTested++;
                    String soFar[] = new String[wordN];
                    System.arraycopy(words, 0, soFar, 0, wordN);
                    int letters = Math.min(word.length(), maxLetters);
                    boolean correct = false;
                    boolean gaveUp = false;
                    for (int i = 0; (i < letters) && !correct && !gaveUp; i++) {
                        result.partialsTested++;
                        String knownPart = i > 0 ? word.substring(0, i) : null;
                        log(String.format(
                            "%d\t%s\t%s\t%s\t",
                            result.partialsTested,
                            join(soFar),
                            knownPart == null ? "" : knownPart,
                            word
                        ));
                        String prediction = this.model.predict(soFar, knownPart, word);
                        if ((prediction != null) && prediction.equals(word)) {
                            if (i == 0) result.wordsCorrect++;
                            else result.partialsCorrect++;
                            correct = true;
                        }
                        else if ((prediction == null) && (i > 0)) {
                            gaveUp = true;
                            result.gaveUpCount++;
                        }
                        log(String.format(
                            "%s\t%s\t%d\t%s\n",
                            prediction,
                            (correct ? "Y" : "N"),
                            i,
                            gaveUp ? "Y" : "N"
                        ));
                    }
                }
            }
            line = iterator.next();
        }
    }

}
