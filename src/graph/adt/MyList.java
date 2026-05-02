package graph.adt;

import java.util.Iterator;

public interface MyList<T> extends Iterable<T> {
//make list interface extend iterator, and when you make a queue, it's automatically iterable along with whatever arraylist you wanna make will be iterable
    void add(T item);
//a set underneath the hood uses a map to load data, if map refuses to load data, then that data makes it unique, can take hashing logic from Set.
    //can use map implementation to make a set.
    void remove(T item);

    T get(int index);

    void removeAt(int index); // 👈 ADD THIS

    int size();

    boolean isEmpty();

    boolean contains(T item);

    @Override
    Iterator<T> iterator();
}
