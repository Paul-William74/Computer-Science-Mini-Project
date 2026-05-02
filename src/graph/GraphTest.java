package graph;

import graph.adt.MyList;
import graph.adt.MyMap;
import graph.adt.MySet;
import graph.implementation.MyHashMap;
import graph.implementation.MyHashSet;
import similarity.GDDSimilarity;

import java.util.*;

public class GraphTest {

    public static void main(String[] args) {

        testBasicGraphOperations();
        testEdgeCases();
        testTraversalAlgorithms();
        testPathReconstruction();
        testConnectivity();
        testVertexRemoval();
        testGDDSimilarity();

        System.out.println("\nALL TESTS COMPLETED.");
    }

    // --------------------------------------------------
    private static void testBasicGraphOperations() {

        System.out.println("\n--- BASIC GRAPH TESTS ---");

        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        Vertex<Node> A = graph.insertVertex(new Node(0, 0));
        Vertex<Node> B = graph.insertVertex(new Node(1, 0));
        Vertex<Node> C = graph.insertVertex(new Node(0, 1));

        graph.insertEdge(A, B, 5);
        graph.insertEdge(A, C, 3);

        if (graph.numVertices() == 3
                && graph.numEdges() == 2
                && graph.degree(A) == 2
                && graph.areaAdjacent(A, B)) {

            System.out.println("Basic graph tests passed.");
        } else {
            System.out.println("Basic graph tests FAILED.");
        }
    }

    // --------------------------------------------------
    private static void testEdgeCases() {

        System.out.println("\n--- EDGE CASE TESTS ---");

        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        Vertex<Node> A = graph.insertVertex(new Node(0, 0));

        graph.insertEdge(null, A, 1);
        graph.insertEdge(A, null, 1);

        Vertex<Node> A2 = graph.insertVertex(new Node(0, 0));

        boolean ok =
                graph.numEdges() == 0 &&
                        graph.numVertices() == 1;

        if (ok) {
            System.out.println("Edge case tests passed.");
        } else {
            System.out.println("Edge case tests FAILED.");
        }
    }

    // --------------------------------------------------
    private static void testTraversalAlgorithms() {

        System.out.println("\n--- TRAVERSAL TESTS ---");

        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        Vertex<Node> A = graph.insertVertex(new Node(0, 0));
        Vertex<Node> B = graph.insertVertex(new Node(1, 0));
        Vertex<Node> C = graph.insertVertex(new Node(0, 1));

        graph.insertEdge(A, B, 1);
        graph.insertEdge(B, C, 1);

        MySet<Vertex<Node>> visited = new MyHashSet<>();
        MyMap<Vertex<Node>, Vertex<Node>> parent = new MyHashMap<>();
        MyMap<Vertex<Node>, Integer> distance = new MyHashMap<>();

        GraphAlgorithms.BFS(graph, A, visited, parent, distance);

        if (visited.size() == 3 && distance.get(C) == 2) {
            System.out.println("Traversal tests passed.");
        } else {
            System.out.println("Traversal tests FAILED.");
        }
    }

    // --------------------------------------------------
    private static void testPathReconstruction() {

        System.out.println("\n--- PATH TEST ---");

        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        Vertex<Node> A = graph.insertVertex(new Node(0, 0));
        Vertex<Node> B = graph.insertVertex(new Node(1, 0));
        Vertex<Node> C = graph.insertVertex(new Node(0, 1));

        graph.insertEdge(A, B, 1);
        graph.insertEdge(B, C, 1);

        MySet<Vertex<Node>> visited = new MyHashSet<>();
        MyMap<Vertex<Node>, Vertex<Node>> parent = new MyHashMap<>();
        MyMap<Vertex<Node>, Integer> distance = new MyHashMap<>();

        GraphAlgorithms.BFS(graph, A, visited, parent, distance);

        MyList<Vertex<Node>> path =
                GraphAlgorithms.constructPath(A, C, parent);

        if (path.size() == 3) {
            System.out.println("Path test passed.");
        } else {
            System.out.println("Path test FAILED.");
        }
    }

    // --------------------------------------------------
    private static void testConnectivity() {

        System.out.println("\n--- CONNECTIVITY TEST ---");

        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        Vertex<Node> A = graph.insertVertex(new Node(0, 0));
        Vertex<Node> B = graph.insertVertex(new Node(1, 0));

        graph.insertEdge(A, B, 1);

        boolean connected1 = GraphAlgorithms.isConnected(graph);

        graph.insertVertex(new Node(99, 99));

        boolean connected2 = GraphAlgorithms.isConnected(graph);

        if (connected1 && !connected2) {
            System.out.println("Connectivity tests passed.");
        } else {
            System.out.println("Connectivity tests FAILED.");
        }
    }

    // --------------------------------------------------
    private static void testVertexRemoval() {

        System.out.println("\n--- REMOVAL TEST ---");

        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        Vertex<Node> A = graph.insertVertex(new Node(0, 0));
        Vertex<Node> B = graph.insertVertex(new Node(1, 0));

        graph.insertEdge(A, B, 1);

        graph.removeVertex(B);

        if (graph.numVertices() == 1 && graph.numEdges() == 0) {
            System.out.println("Removal tests passed.");
        } else {
            System.out.println("Removal tests FAILED.");
        }
    }

    // --------------------------------------------------
    private static void testGDDSimilarity() {

        System.out.println("\n--- GDD TEST ---");

        AdjacencyListGraph<Node, Integer> g1 = new AdjacencyListGraph<>();

        Vertex<Node> A = g1.insertVertex(new Node(0, 0));
        Vertex<Node> B = g1.insertVertex(new Node(1, 0));
        Vertex<Node> C = g1.insertVertex(new Node(0, 1));
        Vertex<Node> D = g1.insertVertex(new Node(1, 1));

        g1.insertEdge(A, B, 1);
        g1.insertEdge(B, C, 1);
        g1.insertEdge(C, D, 1);
        g1.insertEdge(D, A, 1);
        g1.insertEdge(A, C, 1); // triangle exists

        AdjacencyListGraph<Node, Integer> g2 = new AdjacencyListGraph<>();

        Vertex<Node> A2 = g2.insertVertex(new Node(0, 0));
        Vertex<Node> B2 = g2.insertVertex(new Node(1, 0));
        Vertex<Node> C2 = g2.insertVertex(new Node(0, 1));
        Vertex<Node> D2 = g2.insertVertex(new Node(1, 1));

        g2.insertEdge(A2, B2, 1);
        g2.insertEdge(B2, C2, 1);
        g2.insertEdge(C2, D2, 1);
        g2.insertEdge(D2, A2, 1); // no triangle

        GDDSimilarity<Node, Integer> gdd = new GDDSimilarity<>();

        double sim = gdd.computeSimilarity(g1, g2);

        System.out.println("FINAL SIMILARITY = " + sim);

        if (sim > 50) {
            System.out.println("GDD test PASSED (graphs reasonably similar)");
        } else {
            System.out.println("GDD test FAILED (graphs too dissimilar)");
        }
    }
}