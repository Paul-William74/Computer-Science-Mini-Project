package graph;

import graph.adt.MyList;
import graph.adt.MyMap;
import graph.implementation.MyArrayList;
import graph.implementation.MyHashMap;


/**

 Adjacency List Graph implementation of the Graph ADT and defines and implements
 extra utility methods.
 *
 * This graph uses a Map where:
 * - Each key is a Vertex
 * - Each value is a List of edges incident to that vertex
 *
 * This structure allows efficient traversal of neighbors and is ideal
 * for sparse graphs (which is common in real-world structures like vessel networks).
 */
public class AdjacencyListGraph<V, E> implements Graph<V, E> {

    private MyMap<Vertex<V>, MyList<Edge<E>>> adjMap = new MyHashMap<>();

    private MyMap<V, Vertex<V>> vertexMap = new MyHashMap<>();

    /**
     * Returns total number of vertices in the graph.
     * @return - the total number of vertices in the graph
     */
    @Override
    public int numVertices() {
        return adjMap.size(); //keys of map = vertices
    }

    /**
     * numEdges is a method that returns total number of edges in the graph,e
     * ach edge is stored twice (once in each endpoint’s adjacency list), so the total is divided by 2.
     * @return -  the total number of edges in a graph
     */
    @Override
    public int numEdges() {

        int sum = 0;

        MyList<Vertex<V>> verts = adjMap.keySet();

        for (int i = 0; i < verts.size(); i++) {
            Vertex<V> v = verts.get(i);
            sum += adjMap.get(v).size();
        }

        return sum / 2;
    }

    /**
     * vertices() returns all the vertices in a graph as a list
     * @return - a keySet which contains all the keys or vertices in the graph
     */
    @Override
    public MyList<Vertex<V>> vertices() {
        return adjMap.keySet();//new MyArrayList<>(adjMap.keySet());
    }

    /**
     * edges() returns a list of all unique edges in the graph.
     * since edges are stored in both endpoints’ adjacency lists,
     * duplicates are filtered during traversal.
     * @return - all the edges in a graph
     */
    @Override
    public MyList<Edge<E>> edges() {
        MyList<Edge<E>> result = new MyArrayList<>();
        MyList<Vertex<V>> verts = adjMap.keySet();

        for (int i = 0; i < verts.size(); i++) {

            Vertex<V> u = verts.get(i);
            MyList<Edge<E>> edges = adjMap.get(u);

            for (int j = 0; j < edges.size(); j++) {

                Edge<E> e = edges.get(j);

                Vertex<V> v = (Vertex<V>) e.getV();

                if (u.hashCode() <= v.hashCode()) {
                    result.add(e);
                }
            }
        }

        return result;
    }

    /**
     * insertVertex is used to insert a vertex to a graph
     * @param value - is the vertex we're inserting into the graph
     * @return - the vertex v which in our project is a vessel or bifucrcation in the vessel
     * image
     */
    @Override
    public Vertex<V> insertVertex(V value) {

        if (vertexMap != null && vertexMap.containsKey(value)) {
            return vertexMap.get(value);
        }

        Vertex<V> v = new Vertex<>(value);

        adjMap.put(v, new MyArrayList<>());

        if (vertexMap != null) {
            vertexMap.put(value, v);
        }

        return v;
    }

    /**
     * insertEdge is A method that adds an edge between two vertcies
     * @param u - the first vertex
     * @param v - the 2nd vertex
     * @param value - the edge we're inserting to connect them or is it the weight being the
     *              distance between 2 vertices
     * @return - the newly created edge between the two vertices (or existing edge if already present)
     */
    @Override
    public Edge<E> insertEdge(Vertex<V> u, Vertex<V> v, E value) {

        if (u == null || v == null){
            return null;
        }

        if (!adjMap.containsKey(u) || !adjMap.containsKey(v)){
            return null;
        }

        if (u.equals(v)){
            return null;
        }

        if (areaAdjacent(u, v)){
            return getEdge(u,v);
        }

        Edge<E> e = new Edge<>(u, v, value);

        adjMap.get(u).add(e);
        adjMap.get(v).add(e);

        return e;
    }

    /**
     * removeVertex removes a vertex from a graph and all the edges connected to it
     * @param v - the vertex we're removing
     */
    @SuppressWarnings("unchecked")
    @Override
    public void removeVertex(Vertex<V> v) {

        if (!adjMap.containsKey(v)){
            return;
        }

        MyList<Edge<E>> edges = adjMap.get(v);

        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge<E> e = edges.get(i);

            Vertex<V> u = (Vertex<V>) e.getU();
            Vertex<V> w = (Vertex<V>) e.getV();

            removeEdge(u, w);
        }

        adjMap.remove(v);

        if (vertexMap != null) {

            V keyToRemove = null;

            MyList<V> keys = vertexMap.keySet();
            for (int i = 0; i < keys.size(); i++) {
                V key = keys.get(i);
                if (vertexMap.get(key).equals(v)) {
                    keyToRemove = key;
                    break;
                }
            }

            if (keyToRemove != null) {
                vertexMap.remove(keyToRemove);
            }
        }
    }

    /**
     * removeEdge removes an Edge from a graph
     * @param u - endpoint vertex
     * @param v - the  other endpoint vertex
     */
    @Override
    public void removeEdge(Vertex<V> u, Vertex<V> v) {

        if (u == null || v == null){
            return;
        }
        if (!adjMap.containsKey(u) || !adjMap.containsKey(v)){
            return;
        }

        MyList<Edge<E>> edgesU = adjMap.get(u);
        Edge<E> target = null;

        for (int i = 0; i < edgesU.size(); i++) {
            Edge<E> e = edgesU.get(i);

            if ((e.getU().equals(u) && e.getV().equals(v)) ||
                    (e.getU().equals(v) && e.getV().equals(u))) {
                target = e;
                break;
            }
        }

        if (target == null) return;

        adjMap.get(u).remove(target);
        adjMap.get(v).remove(target);
    }



    /**
     * outgoingEdges returns all the edges next to the target vertex
     * @param v - the vertice we're focusing on
     * @return
     */
    @Override
    public MyList<Edge<E>> outgoingEdges(Vertex<V> v) {
        return adjMap.get(v);
    }

    /**
     * opposite is a method that when given a vertex and an edge,
     * returns the vertex connected to the target vertex through the edge
     *
     * It's used  in traversal algorithms like DFS
     *
     * @param v - the vertex we're looking at
     * @param e - the edge connected to the vertex
     * @return - the vertex on the opposite end
     */
    @Override
    public Vertex<V> opposite(Vertex<V> v, Edge<E> e) {
        if (e.getU().equals(v)) {
            return (Vertex<V>) e.getV();
        }else{
            return (Vertex<V>) e.getU();
        }
    }

    /**
     * endvertices returns the two vertices on both ends of an edge
     * @param e - the edge we're looking at
     * @return
     */
    @Override
    public MyList<Vertex<V>> endVertices(Edge<E> e) {
        MyList<Vertex<V>> list = new MyArrayList<>();
        list.add((Vertex<V>) e.getU());
        list.add((Vertex<V>) e.getV());
        return list;
    }

    /**
     * degree is a method that returns the number of edges directly connected to the vertex
     * @param v - the vertex
     * @return - the number of edges directly connected to it
     */
    @Override
    public int degree(Vertex<V> v) {
        if (v == null || !adjMap.containsKey(v)) {
            return 0;
        }

        return adjMap.get(v).size();
    }

    /**
     * areaAdjacent is a method that checks whether two vertices are directly connected by an edge.
     * @param u - vertex A
     * @param v - vertex B
     * @return - true if two vertices are connected by an edge and false if they aren't
     */
    @Override
    public boolean areaAdjacent(Vertex<V> u, Vertex<V> v) {
        return getEdge(u, v) != null;
    }

    /**
     * getNeighbors is a method returns all the neigboring vertices from target vertex v
     * within 1 hop
     *
     * However this doesn't replace DFS, this method only looks at local connectivty
     * and doesn't do traversal
     * @param v - the vertex
     * @return - all vertices next to v
     */
    @Override
    public MyList<Vertex<V>> getNeighbors(Vertex<V> v) {

        MyList<Vertex<V>> neighbors = new MyArrayList<>();

        if (v == null || !adjMap.containsKey(v)){
            return neighbors;
        }

        MyList<Edge<E>> edges = adjMap.get(v);
        if (edges == null){
            return neighbors;
        }

        for (int i = 0; i < edges.size(); i++) {

            Edge<E> e = edges.get(i);
            Vertex<V> oppositeVertex = opposite(v, e);

            if (oppositeVertex != null) {
                neighbors.add(oppositeVertex);
            }
        }

        return neighbors;
    }


    /**
     * Utility method to check if a vertex exists in the graph
     * @param v - the target vertex
     * @return - true or false
     */
    public boolean hasVertex(Vertex<V> v) {
        return adjMap.containsKey(v);
    }

    /**
     * getEdge is a UTILITY method used to retrieve an edge that connects two vertices
     * if it exists
     * @param u  - one endpoint vertex
     * @param v - the other endpoint vertex
     * @return - the edge object connecting u and v, or null if no such edge exists
     */
    @Override
    public Edge<E> getEdge(Vertex<V> u, Vertex<V> v) {

        if (u == null || v == null){
            return null;
        }

        MyList<Edge<E>> edges = adjMap.get(u);
        if (edges == null){
            return null;
        }

        for (int i = 0; i < edges.size(); i++) {
            Edge<E> e = edges.get(i);

            if ((e.getU().equals(u) && e.getV().equals(v)) ||
                    (e.getU().equals(v) && e.getV().equals(u))) {
                return e;
            }
        }

        return null;
    }

    /**
     * getEdgeWeight retrieves the weight of an edge from an edge
     *
     * Uses:
     * 1. Distances between nodes could be stored as Edge weights and this method could
     * make it easy to retrieve them and use in KNN implementation.
     * @param u  - one endpoint vertex
     * @param v - the other endpoint vertex
     * @return weight - the element of an edge which is the weight and can serve as distance value
     * between nodes
     */
    public E getEdgeWeight(Vertex<V> u, Vertex<V> v) {
        Edge<E> e = getEdge(u, v);

        if (e != null) {
            return e.getElement();
        }

        return null;
    }

}
