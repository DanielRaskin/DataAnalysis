package dataanalysis;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;

/**
 * Data provider from CSV data source
 * @param <T> type CSV strings will be converted to
 */
public class CsvDataProvider<T> implements DataProvider<T> {
    private StringArrayDataAdapter<T> dataAdapter;
    private String[] columnNames;
    private LineNumberReader reader;
    private String delimiter;
    private boolean hasNext;
    private T data;

    public CsvDataProvider(StringArrayDataAdapter<T> dataAdapter, LineNumberReader reader, boolean hasColumnNames) {
        this(dataAdapter, reader, hasColumnNames, ',');
    }

    public CsvDataProvider(StringArrayDataAdapter<T> dataAdapter, LineNumberReader reader, boolean hasColumnNames, char delimiter) {
        this.dataAdapter = dataAdapter;
        this.reader = reader;
        this.delimiter = String.valueOf(delimiter);
        try {
            if (hasColumnNames) {
                columnNames = readNext();
            }
            readNextData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public T next() {
                try {
                    var result = data;
                    readNextData();
                    return result;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] readNext() throws IOException {
        var line = reader.readLine();
        return (line == null) ? null : line.split(delimiter);
    }

    private void readNextData() throws IOException {
        boolean success = false;
        while (! success) {
            var columns = readNext();
            hasNext = (columns != null);
            try {
                data = hasNext ? dataAdapter.adapt(columns) : null;
                success = true;
            } catch (DataException e) {
                // do nothing here, just read next line because this line is incorrect
            }
        }
    }
}
