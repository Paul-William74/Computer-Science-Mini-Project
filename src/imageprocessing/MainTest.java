package imageprocessing;

import graph.AdjacencyListGraph;
import graph.Node;
import graph.Vertex;

import java.io.File;

/**
 * Test driver for retinal vessel graph generation.
 *
 * <p>This class loads a retinal image, runs the image-processing pipeline,
 * constructs the vessel-segment graph, and prints graph statistics
 * together with sample node features.</p>
 */
public class MainTest {

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        try {
            ImageProcessor processor = new ImageProcessor();

            File file = new File("src/Datasets/tests/images/01_test.tif");

            System.out.println("Building vessel-segment graph...");

            AdjacencyListGraph<Node, Double> graph =
                    processor.buildNodeGraph(file);

            System.out.println("\n=== GRAPH STATISTICS ===");
            System.out.println("Vertices (segments): " + graph.numVertices());
            System.out.println("Edges (connections): " + graph.numEdges());

            System.out.println("\n=== SAMPLE SEGMENT FEATURES ===");

            int shown = 0;

            for (Vertex<Node> v : graph.vertices()) {

                Node n = v.getElement();

                System.out.printf(
                        "Segment %d | Area=%.1f | Circ=%.4f | Aspect=%.2f | Texture=%.3f%n",
                        n.getId(),
                        n.getArea(),
                        n.getCircularity(),
                        n.getAspectRatio(),
                        n.getTexture()
                );

                shown++;

                if (shown == 5) {
                    break;
                }
            }

            int endpoints = 0;
            int branchSegments = 0;
            int isolated = 0;

            for (Vertex<Node> v : graph.vertices()) {

                int degree = graph.degree(v);

                if (degree == 0) {
                    isolated++;
                } else if (degree == 1) {
                    endpoints++;
                } else if (degree >= 3) {
                    branchSegments++;
                }
            }

            System.out.println("\n=== TOPOLOGY ===");
            System.out.println("Endpoint segments : " + endpoints);
            System.out.println("Branch segments   : " + branchSegments);
            System.out.println("Isolated segments : " + isolated);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}