package org.rickosborne.bigram.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rosborne on 11/6/14.
 */
public class LineReader {

    public static interface Iterator {
        public String next();
    }

    public static class TrainTestIterator implements Iterator {

        private long trainingLines;
        private long testLines;
        private InputStream inputStream;
        private long currentLine;
        private long totalLines;

        public TrainTestIterator(InputStream inputStream, long trainingLines, long testLines) {
            this.inputStream = inputStream;
            this.trainingLines = trainingLines;
            this.testLines = testLines;
            this.currentLine = 0;
            this.totalLines = trainingLines + testLines + 2;
        }

        public String next() {
            int percentBefore = (int) Math.floor(this.currentLine * 100d / this.totalLines);
            this.currentLine++;
            int percentAfter = (int) Math.floor(this.currentLine * 100d / this.totalLines);
            if ((this.currentLine % 100) == 0) {
                System.out.print(".");
                System.out.flush();
            }
            if (percentBefore != percentAfter) {
                System.out.print("%");
                System.out.flush();
            }
            if ((this.currentLine == this.trainingLines + 1) || (this.currentLine == this.trainingLines + this.testLines + 2)) {
                System.out.print("~");
                System.out.flush();
                return null;
            }
            return nextLine();
        }

        private boolean isCrLf (int chr) {
            return (chr == 13) || (chr == 10);
        }

        private String nextLine() {
            StringBuilder builder = new StringBuilder();
            int nextChar;
            try {
                do {
                    nextChar = this.inputStream.read();
                    if (nextChar == -1) {
                        // end of stream
                    } else if ((builder.length() == 0) && isCrLf(nextChar)) {
                        // trim leading newlines
                    } else if (isCrLf(nextChar)) {
                        nextChar = -1;
                    } else {
                        builder.append((char) nextChar);
                    }
                } while (nextChar > -1);
            } catch (IOException e) {
                // e.printStackTrace();
            }
            if (builder.length() > 0) return builder.toString().toLowerCase();
            return null;
        }
    }

}
