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
        Set<Vertex<String>> visitedDFS = new HashSet<>();
        Map<Vertex<String>, Vertex<String>> parentDFS = new HashMap<>();
        GraphAlgorithms.DFS(graph, A, visitedDFS, parentDFS);

        System.out.println("DFS Forest (Parent relationships):");
        for (Map.Entry<Vertex<String>, Vertex<String>> entry : parentDFS.entrySet()) {
            Vertex<String> child = entry.getKey();
            Vertex<String> parent = entry.getValue();
            System.out.println("Vertex " + child.getElement() +
                    " discovered from " + parent.getElement());
        }

        // -------------------------------
        // TEST BFS
        // -------------------------------
        System.out.println("\nBFS Traversal starting from A:");
        Set<Vertex<String>> visitedBFS = new HashSet<>();
        Map<Vertex<String>, Vertex<String>> parentBFS = new HashMap<>();
        Map<Vertex<String>, Integer> distance = new HashMap<>();

        GraphAlgorithms.BFS(graph, A, visitedBFS, parentBFS, distance);

        System.out.println("BFS Distances from A:");
        for (Map.Entry<Vertex<String>, Integer> entry : distance.entrySet()) {
            System.out.println("Distance from A to " + entry.getKey().getElement() + " = " + entry.getValue());
        }

        System.out.println("\nBFS Forest (Parent relationships):");
        for (Map.Entry<Vertex<String>, Vertex<String>> entry : parentBFS.entrySet()) {
            Vertex<String> child = entry.getKey();
            Vertex<String> parent = entry.getValue();
            System.out.println("Vertex " + child.getElement() +
                    " discovered from " + parent.getElement());
        }

        // -------------------------------
        // TEST PATH RECONSTRUCTION
        // -------------------------------
        List<Vertex<String>> path = GraphAlgorithms.constructPath(A, D, parentBFS);
        System.out.print("Path from A to D using BFS: ");
        if (!path.isEmpty()) {
            for (Vertex<String> v : path) {
                System.out.print(v.getElement() + " ");
            }
            System.out.println();
        } else {
            System.out.println("No path found.");
        }

        // -------------------------------
        // TEST CONNECTIVITY
        // -------------------------------
        System.out.println("\nIs the graph connected? " + GraphAlgorithms.isConnected(graph));
    }
}