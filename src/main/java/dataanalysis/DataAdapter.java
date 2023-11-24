package dataanalysis;

/**
 * General interface for data adapter
 * @param <T> type of output data
 * @param <S> type of input data
 */
public interface DataAdapter<T, S> {
    T adapt(S data);
}
