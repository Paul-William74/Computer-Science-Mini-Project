package graph;

import java.util.*;


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
    public static <V, E> void DFS(Graph<V, E> g, Vertex<V> u, Set<Vertex<V>> visited,
                                  Map<Vertex<V>, Vertex<V>> parent) {

        visited.add(u); //keeps track of visited vertices

        /*
        for each loop iterates through all edges connected to a vertex, and for each
        unvisited neighbor, it records the current vertex as it's parent and
        recursively performs DFS from that neighbor
         */
        for (Edge<E> e : g.outgoingEdges(u)) {
            Vertex<V> v = g.opposite(u, e);
            if (!visited.contains(v)) {
                parent.put(v, u); // explicitly mark u as parent of v
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
    public static <V,E> void BFS(Graph<V,E> g, Vertex<V> start, Set<Vertex<V>> visited,
                                            Map<Vertex<V>, Vertex<V>> parent, Map<Vertex<V>, Integer> distance) {

        Queue<Vertex<V>> queue = new LinkedList<>();

        visited.add(start);
        distance.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            Vertex<V> u = queue.poll();

            for (Edge<E> e : g.outgoingEdges(u)) {
                Vertex<V> v = g.opposite(u, e);

                if (!visited.contains(v)) {
                    visited.add(v);
                    parent.put(v, u);

                    //distance = parent's distance + 1
                    distance.put(v, distance.get(u) + 1);
                    queue.add(v);
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
    public static <V> List<Vertex<V>> constructPath(Vertex<V> start, Vertex<V> end,
                                                    Map<Vertex<V>, Vertex<V>> parent) {
        List<Vertex<V>> path = new LinkedList<>();

        //If the end is not reachable
        if (!parent.containsKey(end) && !start.equals(end)) {
            return path;
        }

        Vertex<V> current = end; // <-- start from end

        while (current != null) {
            path.add(0,current); //add to front to reverse path
            current = parent.get(current);
        }

        return path;
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
    public static <V,E> boolean isConnected(Graph<V,E> g) {
        List<Vertex<V>> vertices = g.vertices();

        if (vertices.isEmpty()) {
            return true;
        }

        Set<Vertex<V>> visited = new HashSet<>();
        Map<Vertex<V>, Vertex<V>> parent = new HashMap<>();
        Map<Vertex<V>, Integer> distance = new HashMap<>();

        BFS(g, vertices.get(0), visited, parent, distance);

        return visited.size() == g.numVertices();
    }
}
