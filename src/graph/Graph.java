package graph;

import java.util.List;

public interface Graph<V, E>{

    int numVertices();
    int numEdges();

    List<Vertex<V>> vertices();
    List<Edge<E>> edges();

    Vertex<V> insertVertex(V value);
    Edge<E> insertEdge(Vertex<V> u, Vertex<V> v, E value);

    void removeVertex(Vertex<V> v);
    void removeEdge(Edge<E> e);

    List<Edge<E>> outgoingEdges(Vertex<V> v);

    Vertex<V> opposite(Vertex<V> v,Edge<E> e);

    Vertex<V>[] endVertices(Edge<E> e);

    int degree(Vertex<V> v);

    boolean areaAdjacent(Vertex<V> u, Vertex<V> v);

    List<Vertex<V>> getNeighbors(Vertex<V> v);
}
