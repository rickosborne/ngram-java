package org.rickosborne;

import org.rickosborne.bigram.BigramModel2;
import org.rickosborne.bigram.Tester;
import org.rickosborne.bigram.util.*;

import java.io.*;
import java.text.DecimalFormat;

import static org.rickosborne.bigram.util.Util.linesInFile;

public class Main {

    public static void main(String[] args) throws IOException {
        Config config = new Config("config.json");
        InputStream in = System.in;
        int maxTrainLines = config.get("maxTrainLines", 10000),
                maxTestLines = config.get("maxTestLines", 100);
        String inputFile = config.get("inputFile", null),
                logFile = config.get("logFile", "log.txt");
        if ((args.length > 1) && (args[0].equalsIgnoreCase("learn"))) {
            inputFile = args[1];
            maxTrainLines = linesInFile(inputFile);
            System.out.println("Input: " + inputFile + " (" + String.valueOf(maxTrainLines) + ")");
            System.out.flush();
        }
        if (inputFile != null) {
            in = new FileInputStream(inputFile);
        }
        BigramModel2 model = new BigramModel2(config);
        if ((args.length > 1) && (args[0].equalsIgnoreCase("predict"))) {
            String words[] = Tester.wordsFromLine(args[1]);
            WordList guesses = model.guess(words, null, "");
            if (guesses == null) System.out.println("No prediction.");
            else {
                Prediction prediction = guesses.predict(null);
                if (prediction == null) System.out.println("No prediction.");
                else {
                    System.out.println(String.format("Answer: %s %.2f%%", prediction.getWord(), 100.0f * prediction.getChance()));
                    System.out.println(guesses.toString());
                    System.out.flush();
                }
            }
            return;
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

}
