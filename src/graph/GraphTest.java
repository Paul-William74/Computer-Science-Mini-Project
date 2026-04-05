package graph;

import java.util.*;

public class GraphTest {

    public static void main(String[] args) {

        // -------------------------------
        // CREATE GRAPH (NOW USING NODE)
        // -------------------------------
        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        // Insert vertices (nodes with coordinates)
        Vertex<Node> A = graph.insertVertex(new Node(1, 0, 0, 1));
        Vertex<Node> B = graph.insertVertex(new Node(2, 1, 0, 1));
        Vertex<Node> C = graph.insertVertex(new Node(3, 0, 1, 1));
        Vertex<Node> D = graph.insertVertex(new Node(4, 1, 1, 1));

        // -------------------------------
        // TEST DUPLICATE VERTEX HANDLING
        // -------------------------------
        Vertex<Node> duplicateA = graph.insertVertex(new Node(1, 0, 0, 1));

        System.out.println("Testing duplicate vertex insertion:");
        System.out.println("Total vertices (should NOT increase if handled properly): "
                + graph.numVertices());

        // -------------------------------
        // INSERT EDGES (UNDIRECTED)
        // -------------------------------
        graph.insertEdge(A, B, 5);
        graph.insertEdge(A, C, 3);
        graph.insertEdge(B, D, 2);
        graph.insertEdge(C, D, 4);

        // -------------------------------
        // BASIC GRAPH INFO
        // -------------------------------
        System.out.println("\nNumber of vertices: " + graph.numVertices());
        System.out.println("Number of edges: " + graph.numEdges());

        // -------------------------------
        // TEST NEIGHBORS
        // -------------------------------
        System.out.println("\nNeighbors of A:");
        for (Vertex<Node> v : graph.getNeighbors(A)) {
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
        Set<Vertex<Node>> visitedDFS = new HashSet<>();
        Map<Vertex<Node>, Vertex<Node>> parentDFS = new HashMap<>();

        GraphAlgorithms.DFS(graph, A, visitedDFS, parentDFS);

        System.out.println("DFS Forest (Parent relationships):");
        for (Map.Entry<Vertex<Node>, Vertex<Node>> entry : parentDFS.entrySet()) {
            Vertex<Node> child = entry.getKey();
            Vertex<Node> parent = entry.getValue();

            System.out.println("Vertex " + child.getElement() +
                    " discovered from " + parent.getElement());
        }

        // -------------------------------
        // TEST BFS
        // -------------------------------
        System.out.println("\nBFS Traversal starting from A:");
        Set<Vertex<Node>> visitedBFS = new HashSet<>();
        Map<Vertex<Node>, Vertex<Node>> parentBFS = new HashMap<>();
        Map<Vertex<Node>, Integer> distance = new HashMap<>();

        GraphAlgorithms.BFS(graph, A, visitedBFS, parentBFS, distance);

        System.out.println("BFS Distances from A:");
        for (Map.Entry<Vertex<Node>, Integer> entry : distance.entrySet()) {
            System.out.println("Distance from A to " + entry.getKey().getElement()
                    + " = " + entry.getValue());
        }

        System.out.println("\nBFS Forest (Parent relationships):");
        for (Map.Entry<Vertex<Node>, Vertex<Node>> entry : parentBFS.entrySet()) {
            Vertex<Node> child = entry.getKey();
            Vertex<Node> parent = entry.getValue();

            System.out.println("Vertex " + child.getElement() +
                    " discovered from " + parent.getElement());
        }

        // -------------------------------
        // TEST PATH RECONSTRUCTION
        // -------------------------------
        System.out.print("\nPath from A to D using BFS: ");
        List<Vertex<Node>> path = GraphAlgorithms.constructPath(A, D, parentBFS);

        if (!path.isEmpty()) {
            for (Vertex<Node> v : path) {
                System.out.print(v.getElement() + " ");
            }
            System.out.println();
        } else {
            System.out.println("No path found.");
        }

        // -------------------------------
        // TEST CONNECTIVITY
        // -------------------------------
        System.out.println("\nIs the graph connected? "
                + GraphAlgorithms.isConnected(graph));

        // -------------------------------
        // TEST SAFE VERTEX REMOVAL
        // -------------------------------
        System.out.println("\nRemoving vertex B safely:");
        graph.removeVertex(B);

        System.out.println("Vertices after removal: " + graph.numVertices());
        System.out.println("Edges after removal: " + graph.numEdges());

        // -------------------------------
        // TEST EDGE WEIGHTS
        // -------------------------------
        System.out.println("\nTesting getEdgeWeight:");

        System.out.println("Weight of edge A-B: " + ((AdjacencyListGraph<Node, Integer>) graph).getEdgeWeight(A, B));
        System.out.println("Weight of edge A-C: " + ((AdjacencyListGraph<Node, Integer>) graph).getEdgeWeight(A, C));
        System.out.println("Weight of edge B-D: " + ((AdjacencyListGraph<Node, Integer>) graph).getEdgeWeight(B, D));
        System.out.println("Weight of edge C-D: " + ((AdjacencyListGraph<Node, Integer>) graph).getEdgeWeight(C, D));

        // Test non-existent edge
        System.out.println("Weight of edge A-D (should be null): " + ((AdjacencyListGraph<Node, Integer>) graph).getEdgeWeight(A, D));
    }
}