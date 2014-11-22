package org.rickosborne;

import org.rickosborne.bigram.BigramModel;
import org.rickosborne.bigram.BigramModel2;
import org.rickosborne.bigram.Tester;
import org.rickosborne.bigram.util.LineReader;
import org.rickosborne.bigram.util.TestResult;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;

public class Main {

    public static void main(String[] args) {
        InputStream in = System.in;
        int maxTrainLines = 10000, maxTestLines = 100;
        if (args.length > 0) {
            try {
                in = new FileInputStream(args[0]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (args.length > 1) {
                maxTrainLines = Integer.parseInt(args[1], 10);
                if (args.length > 2) {
                    maxTestLines = Integer.parseInt(args[2], 10);
                }
            }
        }
        BigramModel2 model = new BigramModel2();
        LineReader.TrainTestIterator iterator = new LineReader.TrainTestIterator(in, maxTrainLines, maxTestLines);
        TestResult result = new TestResult();
        FileOutputStream logFile;
        try {
            logFile = new FileOutputStream("log.txt");
            Tester tester = new Tester(model, result, logFile);
            tester.train(iterator);
            tester.test(iterator);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
