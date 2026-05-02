package graph.implementation;

import graph.adt.*;

public class MyHashSet<T> implements MySet<T> {

    private MyList<T> data;

    public MyHashSet() {
        data = new MyArrayList<>();
    }

    @Override
    public void add(T item) {
        if (!data.contains(item)) {
            data.add(item);
        }
    }

    @Override
    public boolean contains(T item) {
        return data.contains(item);
    }

    @Override
    public void remove(T item) {
        data.remove(item);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.size() == 0;
    }
}
