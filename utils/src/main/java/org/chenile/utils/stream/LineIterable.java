package org.chenile.utils.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

class LineIterable<T> implements Iterable<T> {
    private final InputStream inputStream;

    public LineIterable(InputStream inputStream){
        this.inputStream = inputStream;
    }


    public static class LineIterator<T> implements Iterator<T> {
        private final BufferedReader reader;
        private String nextLine;
        public LineIterator(InputStream inputStream) {
            this.reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                nextLine = reader.readLine();
            } catch (IOException e) {
                nextLine = null;
            }
        }

        @Override
        public boolean hasNext() {
            return nextLine != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String currentLine = nextLine;
            try {
                nextLine = reader.readLine();
            } catch (IOException e) {
                nextLine = null;
            }
            return (T)currentLine;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LineIterator(inputStream);
    }
}