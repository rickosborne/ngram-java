package org.rickosborne.bigram.util;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Util {

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

    public static String[] wordsFromLine (String line) {
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

    public static String join(String delimiter, String[] parts) {
        StringBuffer buffer = new StringBuffer(parts.length * 2);
        boolean isFirst = true;
        for (String part : parts) {
            if (isFirst) isFirst = false;
            else buffer.append(delimiter);
            buffer.append(part);
        }
        return buffer.toString();
    }

    public static String titleCase(String mixed) {
        if ((mixed == null) || mixed.isEmpty()) return mixed;
        if (mixed.length() == 1) return mixed.toUpperCase();
        String upper = mixed.substring(0, 1).toUpperCase();
        String lower = mixed.substring(1, mixed.length()).toLowerCase();
        return upper + lower;
    }

    public static int linesInFile(String fileName) {
        BufferedReader reader = null;
        FileReader file = null;
        int lineCount = 0;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            while (reader.readLine() != null) lineCount++;
        } catch (FileNotFoundException e) {
            if (file != null) try {
                file.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineCount;
    }

}
