package graph;

import similarity.GDDSimilarity;

import java.util.*;

public class GraphTest {

    public static void main(String[] args) {

        // -------------------------------
        // CREATE GRAPH (NOW USING NODE)
        // -------------------------------
        Graph<Node, Integer> graph = new AdjacencyListGraph<>();

        // Insert vertices (nodes with coordinates)
        Vertex<Node> A = graph.insertVertex(new Node(1, 0, 0));
        Vertex<Node> B = graph.insertVertex(new Node(2, 1, 0));
        Vertex<Node> C = graph.insertVertex(new Node(3, 0, 1));
        Vertex<Node> D = graph.insertVertex(new Node(4, 1, 1));

        // -------------------------------
        // TEST DUPLICATE VERTEX HANDLING
        // -------------------------------
        Vertex<Node> duplicateA = graph.insertVertex(new Node(1, 0, 0));

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

        // -------------------------------
        // TEST GDD SIMILARITY
        // -------------------------------
        System.out.println("\n--- Testing GDD Similarity ---");

        AdjacencyListGraph<Node, Integer> triangleGraph = new AdjacencyListGraph<>();
        Vertex<Node> T1 = triangleGraph.insertVertex(new Node(10, 0, 0));
        Vertex<Node> T2 = triangleGraph.insertVertex(new Node(11, 1, 0));
        Vertex<Node> T3 = triangleGraph.insertVertex(new Node(12, 0, 1));
        triangleGraph.insertEdge(T1, T2, 1);
        triangleGraph.insertEdge(T2, T3, 1);
        triangleGraph.insertEdge(T3, T1, 1);

        // Compute similarity of the with itself – this should be 1.0
        GDDSimilarity<Node, Integer> gdd = new GDDSimilarity<>();
        double selfSimilarity = gdd.computeSimilarity(triangleGraph, triangleGraph);
        System.out.println("Similarity of graph with itself: " + selfSimilarity + "%");

        AdjacencyListGraph<Node, Integer> houseShapeGraph = new AdjacencyListGraph<>();
        Vertex<Node> H1 = houseShapeGraph.insertVertex(new Node(13, 0, 0));
        Vertex<Node> H2 = houseShapeGraph.insertVertex(new Node(14, 1, 0));
        Vertex<Node> H3 = houseShapeGraph.insertVertex(new Node(15, 1, 1));
        Vertex<Node> H4 = houseShapeGraph.insertVertex(new Node(16, 0, 1));
        Vertex<Node> H5 = houseShapeGraph.insertVertex(new Node(17, 1, 2));
        houseShapeGraph.insertEdge(H1, H2, 2);
        houseShapeGraph.insertEdge(H2, H3, 2);
        houseShapeGraph.insertEdge(H3, H4, 2);
        houseShapeGraph.insertEdge(H4, H1, 2);
        houseShapeGraph.insertEdge(H4, H5, 2);
        houseShapeGraph.insertEdge(H5, H3, 1);

        double crossSimilarity = gdd.computeSimilarity(houseShapeGraph, triangleGraph);
        System.out.printf("Similarity of graph with another: %.2f%%", crossSimilarity);
    }
}