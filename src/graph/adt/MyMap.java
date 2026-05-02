package graph.adt;

public interface MyMap<K, V> {

    void put(K key, V value);

    V get(K key);

    boolean containsKey(K key);

    void remove(K key);

    MyList<K> keySet();
    MyList<V> values();

    int size();
}