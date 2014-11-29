package org.rickosborne;

import org.rickosborne.bigram.BigramModel2;
import org.rickosborne.bigram.Tester;
import org.rickosborne.bigram.util.*;

import java.io.*;
import java.text.DecimalFormat;

import static org.rickosborne.bigram.util.Util.join;
import static org.rickosborne.bigram.util.Util.linesInFile;

public class Main {

    private static String params[];
    private static Config config = new Config("config.json");

    public static void main(String[] args) throws IOException {
        params = args;
        InputStream in = System.in;
        int maxTrainLines = getParam("maxTrainLines", 10000),
                maxTestLines = getParam("maxTestLines", 100),
                skipLines = getParam("skipLines", 0);
        String inputFile = getParam("inputFile", null),
                logFile = getParam("logFile", "log.txt"),
                mode = (args.length > 0) ? args[0] : "";
        BigramModel2 model = new BigramModel2(config);
        if (mode.equals("learn")) {
            if ((inputFile == null) || inputFile.isEmpty()) bail("Need an inputFile to learn.");
            maxTrainLines = linesInFile(inputFile);
            LineReader lineReader = new LineReader(inputFile);
            if (skipLines > 0) {
                if (skipLines > maxTrainLines) bail(String.format("Cannot skip %d lines as the file is only %d lines (%s).", skipLines, maxTestLines, inputFile));
                out(String.format("Skipping %d lines of %d.", skipLines, maxTrainLines));
                lineReader.skipLines(skipLines);
                maxTrainLines -= skipLines;
            }
            lineReader.setWantLineStatus(true);
            out("Input:", inputFile, String.valueOf(maxTrainLines), "lines");
            TestResult result = new TestResult();
            Tester tester = new Tester(model, result, new FileOutputStream(logFile));
            tester.train(lineReader);
            return;
        }
        else if (mode.equals("predict")) {
            String[] words = model.splitWords(args[1]);
            if (words.length == 0) bail("None of the words you specified are known to the model.");
            out("Predicting for:", join(" ", words));
            WordList guesses = model.guess(args[1], null, "");
            if (guesses == null) System.out.println("No prediction.");
            else {
                Prediction prediction = guesses.predict(null);
                if (prediction == null) System.out.println("No prediction.");
                else {
                    System.out.println(String.format("Answer: %s %.2f%%", prediction.getWord(), 100.0f * prediction.getChance()));
                    System.out.println(guesses.asJSONObject().toString());
                    System.out.flush();
                }
            }
            return;
        }
        if (inputFile != null) {
            in = new FileInputStream(inputFile);
        }
        LineReader.TrainTestIterator iterator = new LineReader.TrainTestIterator(in, maxTrainLines, maxTestLines);
        TestResult result = new TestResult();
        Tester tester = new Tester(model, result, new FileOutputStream(logFile));
        tester.train(iterator);
        tester.test(iterator);
        DecimalFormat format = new DecimalFormat("#,###");
        System.out.printf(
            "\n" +
            "  Lines trained: %d\n" +
            "   Lines tested: %d\n" +
            "   Words tested: %d / %d = %.2f%%\n" +
            "Partials tested: %d / %d = %.2f%%\n" +
            "   Total tested: %d / %d = %.2f%%\n" +
            "        Gave Up: %d / %d = %.2f%%\n" +
            "         Memory: %s\n",
            result.linesTrained,
            result.linesTested,
            result.wordsCorrect, result.wordsTested, result.wordsTested > 0 ? result.wordsCorrect * 100.0f / result.wordsTested : 0,
            result.partialsCorrect, result.partialsTested, result.partialsTested > 0 ? result.partialsCorrect * 100.0f / result.partialsTested : 0,
            result.wordsCorrect + result.partialsCorrect,
            result.partialsTested,
            result.partialsTested > 0 ? (result.wordsCorrect + result.partialsCorrect) * 100.0f / result.partialsTested : 0,
            result.gaveUpCount, result.partialsTested, result.partialsTested > 0 ? result.gaveUpCount * 100.0f / result.partialsTested : 0,
            format.format(result.memoryAfter - result.memoryBefore)
        );
    }

    private static void out(String... messages) {
        System.out.println(join(" ", messages));
        System.out.flush();
    }

    private static void bail(String message) {
        if (message != null) System.out.println(message);
        System.exit(1);
    }

    private static String getParam(String paramName) {
        for (int i = 0; i < params.length - 1; i++) {
            if (params[i].equals(paramName)) return params[i+1];
        }
        return null;
    }

    private static String getParam(String paramName, String defaultValue) {
        String param = getParam(paramName);
        if (param != null) return param;
        return config.get(paramName, defaultValue);
    }

    private static int getParam(String paramName, int defaultValue) {
        String param = getParam(paramName);
        if (param != null) return Integer.parseInt(param, 10);
        return config.get(paramName, defaultValue);
    }

}
