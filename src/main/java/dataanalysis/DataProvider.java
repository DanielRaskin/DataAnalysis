package dataanalysis;

import java.util.Iterator;

/**
 * General interface for data provider
 * @param <T> type of data to provide
 */
public interface DataProvider<T> extends Iterable<T> {
    Iterator<T> iterator();
    void close();
}
