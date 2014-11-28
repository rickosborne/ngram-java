package org.rickosborne.bigram.util;

import java.io.*;

public class Util {

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
