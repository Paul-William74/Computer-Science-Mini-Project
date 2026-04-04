package graph;

import java.util.*;

public class GraphTest {

    public static void main(String[] args) {

        // -------------------------------
        // CREATE GRAPH
        // -------------------------------
        Graph<String, Integer> graph = new AdjacencyListGraph<>();

        // Insert vertices
        Vertex<String> A = graph.insertVertex("A");
        Vertex<String> B = graph.insertVertex("B");
        Vertex<String> C = graph.insertVertex("C");
        Vertex<String> D = graph.insertVertex("D");

        // Insert edges (undirected)
        graph.insertEdge(A, B, 5);
        graph.insertEdge(A, C, 3);
        graph.insertEdge(B, D, 2);
        graph.insertEdge(C, D, 4);

        // -------------------------------
        // BASIC GRAPH INFO
        // -------------------------------
        System.out.println("Number of vertices: " + graph.numVertices());
        System.out.println("Number of edges: " + graph.numEdges());

        // -------------------------------
        // TEST NEIGHBORS
        // -------------------------------
        System.out.println("\nNeighbors of A:");
        for (Vertex<String> v : graph.getNeighbors(A)) {
            System.out.println(v.getElement());
        }

        // -------------------------------
        // TEST DEGREE
        // -------------------------------
        System.out.println("\nDegree of A: " + graph.degree(A));

        // -------------------------------
        // TEST ADJACENCY
        // -------------------------------
        System.out.println("\nAre A and B adjacent? " + graph.areaAdjacent(A, B));
        System.out.println("Are A and D adjacent? " + graph.areaAdjacent(A, D));

        // -------------------------------
        // TEST DFS
        // -------------------------------
        System.out.println("\nDFS Traversal starting from A:");

        Set<Vertex<String>> visited = new LinkedHashSet<>();
        Map<Vertex<String>, Vertex<String>> forest = new HashMap<>();

        GraphAlgorithms.DFS(graph, A, visited, forest);

        for (Vertex<String> v : visited) {
            System.out.println("Visited: " + v.getElement());
        }

        // -------------------------------
        // DFS FOREST (PARENT-CHILD)
        // -------------------------------
        System.out.println("\nDFS Forest (how we reached each node):");

        for (Map.Entry<Vertex<String>, Vertex<String>> entry : forest.entrySet()) {
            Vertex<String> child = entry.getKey();
            Vertex<String> parent = entry.getValue();

            System.out.println("Vertex " + child.getElement() +
                    " was discovered from " + parent.getElement());
        }
    }
}