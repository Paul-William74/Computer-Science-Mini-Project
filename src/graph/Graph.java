package graph;

import java.util.List;

/**
Graph interface specifies the operations a Graph supports but not how it does them
 */
public interface Graph<V, E>{

    /*
    Standard Methods found in a Graph ADT that serve as the backbone for a Graph ADT and are
    necessary for algorthms like BFS, DFS, Djikstra's and KNN
    */
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

    //Extra utility method to help with GDD implementation
    List<Vertex<V>> getNeighbors(Vertex<V> v);
}
