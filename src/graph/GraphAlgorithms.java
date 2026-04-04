package graph;

import java.util.Map;
import java.util.Set;

public class GraphAlgorithms {

    public static <V, E> void DFS(Graph<V, E> g, Vertex<V> u, Set<Vertex<V>> visited,
                                  Map<Vertex<V>, Vertex<V>> parent) {
        visited.add(u);

        for (Edge<E> e : g.outgoingEdges(u)) {
            Vertex<V> v = g.opposite(u, e);
            if (!visited.contains(v)) {
                parent.put(v, u); // explicitly mark u as parent of v
                DFS(g, v, visited, parent);
            }
        }
    }
}
