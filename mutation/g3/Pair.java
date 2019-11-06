package mutation.g3;

/**
 *
 * @author group 3
 * @param <T> type of the first element
 * @param <U> type of the second element
 */
public class Pair<T, U> {

    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

}
