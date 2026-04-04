package graph;

import java.util.*;

public class AdjacencyListGraph<V, E> implements Graph<V, E> {

    private Map<Vertex<V>, List<Edge<E>>> adjMap = new HashMap<>();
    private List<Edge<E>> edgeList = new ArrayList<>();


    @Override
    public int numVertices() {
        return adjMap.size();
    }

    @Override
    public int numEdges() {
        return edgeList.size();
    }

    @Override
    public List<Vertex<V>> vertices() {
        return new ArrayList<>(adjMap.keySet());
    }

    @Override
    public List<Edge<E>> edges() {
        return edgeList;
    }

    @Override
    public Vertex<V> insertVertex(V value) {
        Vertex<V> v = new Vertex<>(value);
        adjMap.put(v, new ArrayList<>());
        return v;
    }

    @Override
    public Edge<E> insertEdge(Vertex<V> u, Vertex<V> v, E value) {
        Edge<E> e = new Edge<>(u, v, value);

        adjMap.get(u).add(e);
        adjMap.get(v).add(e);

        edgeList.add(e);

        return e;
    }

    @Override
    public void removeVertex(Vertex<V> v) {
        List<Edge<E>> edges = adjMap.get(v);

        for (Edge<E> e : edges) {
            removeEdge(e);
        }

        adjMap.remove(v);
    }

    @Override
    public void removeEdge(Edge<E> e) {
        Vertex<V> u = (Vertex<V>) e.getU();
        Vertex<V> v = (Vertex<V>) e.getV();

        adjMap.get(u).remove(e);
        adjMap.get(v).remove(e);

        edgeList.remove(e);
    }

    @Override
    public List<Edge<E>> outgoingEdges(Vertex<V> v) {
        return adjMap.get(v);
    }

    @Override
    public Vertex<V> opposite(Vertex<V> v, Edge<E> e) {
        if (e.getU().equals(v)) {
            return (Vertex<V>) e.getV();
        }else{
            return (Vertex<V>) e.getU();
        }
    }

    @Override
    public Vertex<V>[] endVertices(Edge<E> e) {
        return (Vertex<V>[]) new Vertex[]{(Vertex<V>) e.getU(), (Vertex<V>) e.getV()};
    }

    @Override
    public int degree(Vertex<V> v) {
        return adjMap.get(v).size();
    }

    @Override
    public boolean areaAdjacent(Vertex<V> u, Vertex<V> v) {
        for (Edge<E> e : adjMap.get(u)) {
            if (opposite(u, e).equals(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Vertex<V>> getNeighbors(Vertex<V> v) {
        List<Vertex<V>> neighbors = new ArrayList<>();

        for (Edge<E> e : adjMap.get(v)) {
            neighbors.add(opposite(v, e));
        }
        return neighbors;
    }

    public List<Vertex<V>> getAllVertices() {
        return new  ArrayList<>(adjMap.keySet());
    }

    public List<Edge<E>> getAllEdges() {
        return edgeList;
    }

    public boolean hasVertex(Vertex<V> v) {
        return adjMap.containsKey(v);
    }

    public boolean hasEdge(Vertex<V> u, Vertex<V> v) {
        return areaAdjacent(u, v);
    }
}
