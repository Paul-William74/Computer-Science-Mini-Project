package graph;

import java.util.*;

import graph.adt.MyMap;
import graph.adt.MyQueue;
import graph.adt.MySet;
import graph.adt.MyList;
import graph.implementation.MyArrayList;
import graph.implementation.MyHashMap;
import graph.implementation.MyLinkedQueue;
import graph.implementation.MyHashSet;


/**
 * A utility class that defines and implements graph algorithms like DFS or BFS
 */
public class GraphAlgorithms {

    /**
     * DFS is a method that performs Depth First Search traversal from a given vertex
     *
     * This method explores the graph by visiting a vertex, then recursively visiting
     * all of its unvisited neighboring vertices before backtracking.
     *
     * @param g - the graph being traversed
     * @param u - the starting vertex for the DFS traversal
     * @param visited - a set that keeps track of all visited vertices to avoid revisiting
     * @param parent - a map that records the parent(previous vertex) of each visited vertex,
     *               effectively storing the DFS tree
     * @param <V> - the type of data stored at vertices
     * @param <E> - the type of data stored at Edges
     */
    public static <V, E> void DFS(
            Graph<V, E> g,
            Vertex<V> u,
            MySet<Vertex<V>> visited,
            MyMap<Vertex<V>, Vertex<V>> parent
    ) {

        if (u == null || visited.contains(u)) return;

        visited.add(u);

        MyList<Edge<E>> edges = g.outgoingEdges(u);
        if (edges == null) return;

        for (int i = 0; i < edges.size(); i++) {
            Edge<E> e = edges.get(i);

            Vertex<V> v = g.opposite(u, e);

            if (v != null && !visited.contains(v)) {
                parent.put(v, u);
                DFS(g, v, visited, parent);
            }
        }
    }


    /**
     * This method is repsonsible for Breadth-First Search(BFS), it allows one to explore a graph
     * level by level starting from the source vertex.
     *
     * @param g        - the graph we're exploring
     * @param start    - the starting vertex
     * @param visited  - set to keep track of visited vertices
     * @param parent   - a map to store the parent of each vertex
     * @param distance - a map that store the shortest distance from the starting vertex
     * @param <V> - the type of data stored at vertices
     * @param <E> - the type of data stored at Edges
     */
    public static <V, E> void BFS(
            Graph<V, E> g,
            Vertex<V> start,
            MySet<Vertex<V>> visited,
            MyMap<Vertex<V>, Vertex<V>> parent,
            MyMap<Vertex<V>, Integer> distance
    ) {

        if (start == null) return;

        MyQueue<Vertex<V>> queue = new MyLinkedQueue<>();

        visited.add(start);
        distance.put(start, 0);
        queue.enqueue(start);

        while (!queue.isEmpty()) {

            Vertex<V> u = queue.dequeue();

            MyList<Edge<E>> edges = g.outgoingEdges(u);
            if (edges == null) continue;

            for (int i = 0; i < edges.size(); i++) {

                Vertex<V> v = g.opposite(u, edges.get(i));

                if (v != null && !visited.contains(v)) {

                    visited.add(v);
                    parent.put(v, u);
                    distance.put(v, distance.get(u) + 1);

                    queue.enqueue(v);
                }
            }
        }
    }


    /**
     * A method that reconstructs the path from beginning to end using the parent map
     *
     * Uses:
     * 1. Can be used in GDD to determine if the path between 2 key nodes has changed,
     * or detect changes in vessel routes(paths)
     * 2. Could be used indirectly in KNN implementation, as path can be used to derive features
     * used in KNN from path length or shape which can be used as input in KNN
     *
     * @param start - the starting vertex
     * @param end - the destination vertex
     * @param parent - the map that stores parent relationships
     * @return list -  a list of vertices representing the path from start to end in order.
     *  *         Returns an empty list if the end vertex is unreachable.
     * @param <V> - the type of data stored at vertices
     */
    public static <V> MyList<Vertex<V>> constructPath(
            Vertex<V> start,
            Vertex<V> end,
            MyMap<Vertex<V>, Vertex<V>> parent
    ) {

        MyList<Vertex<V>> path = new MyArrayList<>();

        if (start == null || end == null) return path;

        // unreachable check
        if (!start.equals(end) && parent.get(end) == null) {
            return path;
        }

        Vertex<V> current = end;

        while (current != null) {
            path.add(current); // temporarily reversed
            current = parent.get(current);
        }

        // reverse manually (since no Collections.reverse)
        MyList<Vertex<V>> reversed = new MyArrayList<>();

        for (int i = path.size() - 1; i >= 0; i--) {
            reversed.add(path.get(i));
        }

        return reversed;
    }

    /**
     * isConnected is used to check if the Graph is fully connected
     *
     * Uses:
     * 1.Can be used in GDD implementation to compare valid graphs only, to basically reject
     * an invalid graph if disconnected as that indicates the data structure is broken
     * 2. Can be used in KNN to make sure only valid graphs are used
     *
     * @param g - the graph
     * @return true or false - true if every vertex can be reached from any starting vertex, false otherwise.
     *  *         An empty graph is considered connected.
     * @param <V> - the type of data stored at vertices
     * @param <E> - the type of data stored at Edges
     */
    public static <V, E> boolean isConnected(Graph<V, E> g) {

        MyList<Vertex<V>> vertices = g.vertices();

        if (vertices == null || vertices.size() == 0) {
            return true;
        }

        MySet<Vertex<V>> visited = new MyHashSet<>();
        MyMap<Vertex<V>, Vertex<V>> parent = new MyHashMap<>();
        MyMap<Vertex<V>, Integer> distance = new MyHashMap<>();

        DFS(g, vertices.get(0), visited, parent);

        return visited.size() == g.numVertices();
    }
}
