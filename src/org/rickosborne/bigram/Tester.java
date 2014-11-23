package org.rickosborne.bigram;

import org.rickosborne.bigram.util.LineReader;
import org.rickosborne.bigram.util.TestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Tester {

    private static Pattern[] patterns = {
        Pattern.compile("\\$"),
        Pattern.compile("\\b[0-9.,]+\\b"),
        Pattern.compile("(:-?[)D]|\\(-?:)"),
        Pattern.compile("(?=\\W|^)(:\\(|\\):)"),
        Pattern.compile(";-?[)D]"),
        Pattern.compile("\\s+[@#]\\w+")
    };
    private static String[] replacements = {
        " ",
        " \\$number ",
        " \\$smile ",
        " \\$frown ",
        " \\$wink ",
        " "
    };
    private static String wordBreaks = "[^\\w'_$]+";
    private static String[] empty = {};
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

    public Tester(BigramModel2 model, TestResult result, OutputStream log) throws IOException {
        this.model = model;
        this.result = result;
        this.logger = new OutputStreamWriter(log);
        log("Trial\tWords\tPartial\tAnswer\t");
        model.setLogger(this.logger);
        log("Winner\tCorrect?\tLetters\tGave Up\n");
    }

    private void log(String message) {
        try {
            this.logger.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] wordsFromLine (String line) {
        if ((line == null) || line.isEmpty()) return empty;
        String result = line.toLowerCase();
        for (int i = 0, patternCount = patterns.length; i < patternCount; i++) {
            result = patterns[i].matcher(result).replaceAll(replacements[i]);
        }
        String[] parts = result.split(wordBreaks);
        ArrayList<String> words = new ArrayList<String>();
        for (String word : parts) {
            if ((word != null) && !word.isEmpty()) words.add(word);
        }
        return words.toArray(new String[words.size()]);
    }

    public void train (LineReader.TrainTestIterator iterator) throws SQLException {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        result.memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        String line = iterator.next();
        while (line != null) {
            result.linesTrained++;
            this.model.learn(wordsFromLine(line));
            line = iterator.next();
        }
    }

    public void test (LineReader.TrainTestIterator iterator) throws IOException, SQLException {
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
