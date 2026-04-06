package imageprocessing;

import graph.AdjacencyListGraph;
import graph.Node;
import graph.Vertex;

import java.io.File;

public class MainTest {

    public static void main(String[] args) {

        try {
            ImageProcessor processor = new ImageProcessor();

            File file = new File("src/Datasets/tests/images/01_test.tif"); // <-- put your image path here

            System.out.println("Processing image...");

            // Build graph (this runs full pipeline)
            AdjacencyListGraph<Node, Integer> graph = processor.buildGraph(file);

            // =========================
            // GRAPH TESTING
            // =========================
            System.out.println("\n--- GRAPH STATS ---");
            System.out.println("Vertices: " + graph.numVertices());
            System.out.println("Edges: " + graph.numEdges());

            // =========================
            // FEATURE DETECTION
            // =========================
            int endpoints = 0;
            int bifurcations = 0;

            for (Vertex<Node> v : graph.vertices()) {

                int degree = graph.degree(v);

                if (degree == 1) {
                    endpoints++;
                }

                if (degree >= 3) {
                    bifurcations++;
                }
            }

            System.out.println("\n--- FEATURES ---");
            System.out.println("Endpoints: " + endpoints);
            System.out.println("Bifurcations: " + bifurcations);

            // =========================
            // SANITY CHECK SAMPLE NODE
            // =========================
            System.out.println("\n--- SAMPLE NODES ---");

            int count = 0;
            for (Vertex<Node> v : graph.vertices()) {
                System.out.println(v.getElement());

                count++;
                if (count == 5) break; // print only first 5
            }

            System.out.println("\nProcessing complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}