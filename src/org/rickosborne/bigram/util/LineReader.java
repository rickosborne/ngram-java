package org.rickosborne.bigram.util;

import java.io.*;

public class LineReader {

    public static interface Iterator {
        public String next();
    }

    public static interface ILineHandler {
        public boolean handleLine(String line);
    }

    public static interface ILineState {
        public void setLastLine(String fileName, int lastLine);
    }

    private BufferedReader reader = null;
    private String fileName = null;
    private FileReader file = null;
    private int atLine = 0;
    private Thread interruptHandler = null;
    private boolean wantLineStatus = false;
    private ILineState lineStateHandler = null;

    private void ensureReader() {
        if ((reader == null) && (fileName != null)) {
            try {
                file = new FileReader(fileName);
                reader = new BufferedReader(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanUp() {
        if (file != null) try {
            file.close();
            file = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LineReader(BufferedReader reader) {
        this.reader = reader;
    }

    public LineReader(InputStream in) {
        this.reader = new BufferedReader(new InputStreamReader(in));
    }

    public LineReader(String fileName) {
        this.fileName = fileName;
    }

    public void skipLines(int skipCount) {
        ensureReader();
        if (reader == null) return;
        String line = null;
        try {
            while (atLine++ < skipCount) line = reader.readLine();
            if (line != null) System.out.println("Starting after: " + line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(ILineHandler handler) {
        ensureReader();
        if (reader == null) return;
        addInterruptHandler();
        String line = null;
        boolean keepGoing;
        try {
            do {
                line = reader.readLine();
                keepGoing = (line != null) && handler.handleLine(line);
                atLine++;
            } while (keepGoing);
        } catch (IOException e) {
            if (line != null) System.out.println("\nLast line: " + line);
            e.printStackTrace();
        } finally {
            cleanUp();
        }
    }

    private void addInterruptHandler() {
        if (!wantLineStatus) return;
        synchronized (LineReader.class) {
            if (interruptHandler == null) {
                interruptHandler = new Thread() {
                    @Override
                    public void run() {
                    System.out.println(String.format("At line %d.", atLine));
                    if (lineStateHandler != null) lineStateHandler.setLastLine(fileName, atLine);
                    }
                };
            }
            Runtime.getRuntime().addShutdownHook(interruptHandler);
        }
    }

    public void setWantLineStatus(boolean wantLineStatus, ILineState lineStateHandler) {
        this.wantLineStatus = wantLineStatus;
        this.lineStateHandler = lineStateHandler;
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
