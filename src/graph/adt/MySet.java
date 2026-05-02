package graph.adt;

public interface MySet<T> {

    void add(T item);

    boolean contains(T item);

    void remove(T item);

    int size();

    boolean isEmpty();
}