package graph.implementation;

import graph.adt.*;

public class MyHashMap<K, V> implements MyMap<K, V> {

    private class Entry {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private MyList<Entry> entries;

    public MyHashMap() {
        entries = new MyArrayList<>();
    }

    @Override
    public void put(K key, V value) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }
        entries.add(new Entry(key, value));
    }

    @Override
    public V get(K key) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).key.equals(key)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public void remove(K key) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.key.equals(key)) {
                entries.remove(e);
                return;
            }
        }
    }

    @Override
    public MyList<K> keySet() {
        MyList<K> keys = new MyArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            keys.add(entries.get(i).key);
        }
        return keys;
    }

    @Override
    public MyList<V> values() {

        MyList<V> values = new MyArrayList<>();

        for (int i = 0; i < entries.size(); i++) {

            Entry e = entries.get(i);

            if (e != null) {
                values.add(e.value);
            }
        }

        return values;
    }

    @Override
    public int size() {
        return entries.size();
    }
}
