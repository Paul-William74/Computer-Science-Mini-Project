package graph;

import java.util.*;

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

    // Maps each vertex to its list of directly connected edges
    private Map<Vertex<V>, List<Edge<E>>> adjMap = new HashMap<>();

    // Stores all edges in the graph for quick access
    private List<Edge<E>> edgeList = new ArrayList<>();



    /**
     * Returns total number of vertices in the graph.
     * @return - the total number of vertices in the graph
     */
    @Override
    public int numVertices() {
        return adjMap.size(); //keys of map = vertices
    }

    /**
     * numEdges is a method that returns total number of edges in the graph.
     * @return -  the total number of edges in a graph
     */
    @Override
    public int numEdges() {
        return edgeList.size();
    }

    /**
     * vertices() returns all the vertices in a graph as a list
     * @return - a keySet which contains all the keys or vertices in the graph
     */
    @Override
    public List<Vertex<V>> vertices() {
        return new ArrayList<>(adjMap.keySet());
    }

    /**
     * edges() returns all the edges in the graph has a list.
     * @return - all the edges in a graph
     */
    @Override
    public List<Edge<E>> edges() {
        return edgeList;
    }

    /**
     * insertVertex is used to insert a vertex to a graph
     * @param value - is the vertex we're inserting into the graph
     * @return - the vertex v which in our project is a vessel or bifucrcation in the vessel
     * image
     */
    @Override
    public Vertex<V> insertVertex(V value) {
        Vertex<V> v = new Vertex<>(value);
        adjMap.put(v, new ArrayList<>());
        return v;
    }

    /**
     * insertEdge is A method that adds an edge between two vertcies
     * @param u - the first vertex
     * @param v - the 2nd vertex
     * @param value - the edge we're inserting to connect them or is it the weight being the
     *              distance between 2 vertices
     * @return - the weight which is the distance between 2 vertices
     */
    @Override
    public Edge<E> insertEdge(Vertex<V> u, Vertex<V> v, E value) {
        Edge<E> e = new Edge<>(u, v, value);

        //we add an edge to both vertices because the graph is an undirected type of graph
        adjMap.get(u).add(e);
        adjMap.get(v).add(e);

        edgeList.add(e);

        return e;
    }

    /**
     * removeVertex removes a vertex from a graph and all the edges connected to it
     * @param v - the vertex we're removing
     */
    @Override
    public void removeVertex(Vertex<V> v) {
        List<Edge<E>> edges = adjMap.get(v);

        for (Edge<E> e : edges) {
            removeEdge(e);
        }

        adjMap.remove(v);
    }

    /**
     * removeEdge removes an Edge from a graph
     * @param e - the edge that we're removing from a graph
     */
    @Override
    public void removeEdge(Edge<E> e) {
        Vertex<V> u = (Vertex<V>) e.getU();
        Vertex<V> v = (Vertex<V>) e.getV();

        adjMap.get(u).remove(e);
        adjMap.get(v).remove(e);

        edgeList.remove(e);
    }

    /**
     * outgoingEdges returns all the edges next to the target vertex
     * @param v - the vertice we're focusing on
     * @return
     */
    @Override
    public List<Edge<E>> outgoingEdges(Vertex<V> v) {
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
    public List<Vertex<V>> endVertices(Edge<E> e) {
        return List.of((Vertex<V>) e.getU(), (Vertex<V>) e.getV());
    }

    /**
     * degree is a method that returns the number of edges directly connected to the vertex
     * @param v - the vertex
     * @return - the number of edges directly connected to it
     */
    @Override
    public int degree(Vertex<V> v) {
        return adjMap.get(v).size();
    }

    /**
     * areaAdjacent is a method that returns true if there is an edge connecting two vertices
     * @param u - vertex A
     * @param v - vertex B
     * @return - true if two vertices are connected by an edge and false if they aren't
     */
    @Override
    public boolean areaAdjacent(Vertex<V> u, Vertex<V> v) {
        for (Edge<E> e : adjMap.get(u)) {
            if (opposite(u, e).equals(v)) {
                return true;
            }
        }
        return false;
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
    public List<Vertex<V>> getNeighbors(Vertex<V> v) {
        List<Vertex<V>> neighbors = new ArrayList<>();

        for (Edge<E> e : adjMap.get(v)) {
            neighbors.add(opposite(v, e));
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

}
