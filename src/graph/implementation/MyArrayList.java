package graph.implementation;

import graph.adt.*;

import java.util.Iterator;

public class MyArrayList<T> implements MyList<T> {

    private Object[] data;
    private int size;

    public MyArrayList() {
        data = new Object[10];
        size = 0;
    }

    @Override
    public void add(T item) {
        if (size == data.length) resize();
        data[size++] = item;
    }

    @Override
    public void remove(T item) {
        for (int i = 0; i < size; i++) {
            if (data[i] != null && data[i].equals(item)) {
                for (int j = i; j < size - 1; j++) {
                    data[j] = data[j + 1];
                }
                data[size - 1] = null;
                size--;
                return;
            }
        }
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return (T) data[index];
    }

    @Override
    public void removeAt(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException();
            }

            for (int i = index; i < size - 1; i++) {
                data[i] = data[i + 1];
            }

            data[size - 1] = null;
            size--;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(T item) {
        for (int i = 0; i < size; i++) {
            if (data[i] != null && data[i].equals(item)) return true;
        }
        return false;
    }

    private void resize() {
        Object[] newData = new Object[data.length * 2];
        for(int i = 0; i < size; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }

    @Override
    public Iterator<T> iterator() {
        return new MyArrayListIterator();
    }

    private class MyArrayListIterator implements Iterator<T> {
        private int current = 0;

        @Override
        public boolean hasNext() {
            return current < size;
        }

        @Override
        public T next() {
            return (T) data[current++];
        }
    }
}