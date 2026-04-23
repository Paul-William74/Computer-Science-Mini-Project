package imageprocessing;

import graph.AdjacencyListGraph;
import graph.Node;
import graph.Vertex;

import java.io.File;

public class MainTest {

    public static void main(String[] args) {
        try {
            ImageProcessor processor = new ImageProcessor();
            File file = new File("src/Datasets/tests/images/01_test.tif");

            System.out.println("Building vessel‑segment graph...");
            AdjacencyListGraph<Node, Double> graph = processor.buildSegmentGraph(file);

            System.out.println("\n=== Graph Statistics ===");
            System.out.println("Vertices (segments): " + graph.numVertices());
            System.out.println("Edges (between segments): " + graph.numEdges());

            // Print features of first 5 segments
            System.out.println("\n=== Sample Segment Features ===");
            int count = 0;
            for (Vertex<Node> v : graph.vertices()) {
                Node segNode = v.getElement();
                System.out.printf("Segment %d: area=%.1f, circ=%.3f, aspect=%.2f, texture=%.2f%n",
                        segNode.getId(), segNode.getArea(), segNode.getCircularity(),
                        segNode.getAspectRatio(), segNode.getTexture());
                if (++count >= 5) break;
            }

            // Count bifurcations and endpoints in the segment graph
            int endpoints = 0, bifurcations = 0;
            for (Vertex<Node> v : graph.vertices()) {
                int deg = graph.degree(v);
                if (deg == 1) endpoints++;
                else if (deg >= 3) bifurcations++;
            }
            System.out.println("\n=== Network Topology ===");
            System.out.println("Endpoint segments: " + endpoints);
            System.out.println("Bifurcation points (junctions between segments): " + bifurcations);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}