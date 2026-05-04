package graph.adt;

public interface MyQueue<T> {

    void enqueue(T item);

    T dequeue();

    boolean isEmpty();

    int size();
}