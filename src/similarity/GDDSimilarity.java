package similarity;
import graph.Graph;
import graph.Vertex;
import graph.adt.MyList;
import graph.adt.MyMap;
import graph.implementation.MyHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes graphlet degree distribution (GDD) similarity between two graphs.
 * Uses graphlets of size 3 (paths of length 2 and triangles)
 */
public final class GDDSimilarity<V,E> {
    /**
     * Returns a similarity score between 0-100%
     * for two graphs based on their normalized orbit distributions.
     * @param graph1 first graph (undirected)
     * @param graph2 second graph (undirected)
     * @return cosine similarity between the two graphs' feature vectors
     */
    public double computeSimilarity(Graph<V,E> graph1, Graph<V,E> graph2)
    {
        double[] feature1 = getFeatureVector(graph1);
        double[] feature2 = getFeatureVector(graph2);
        System.out.printf("""
                
                Feature vector of graph 1: {%.2f, %.2f, %.2f}
                Feature vector of graph 2: {%.2f, %.2f, %.2f}
                """, feature1[0],feature1[1],feature1[2],feature2[0],feature2[1],feature2[2]);
        return cosineSimilarity(feature1, feature2)*100;
    }
    // ---------------------------- Helper methods ----------------------------

    /**
     * Types of Orbits of size 3 graphlets:
     * orbit 0: currentNode -> o -> o
     * orbit 1: o <- currentNode -> o
     * orbit 2: node is part of a triangle
     * @param graph - graph whose nodes are to be considered
     * @return - a map of the number of times each orbit of size 3 graphlets has appeared in the graph
     */
    private MyMap<Vertex<V>, int[]> countOrbits(Graph<V, E> graph) {

        MyList<Vertex<V>> vertices = graph.vertices();
        int n = vertices.size();

        System.out.println("\n[DEBUG] TOTAL VERTICES = " + n);

        MyMap<Vertex<V>, int[]> orbitCounts = new MyHashMap<>();

        // init
        for (int i = 0; i < n; i++) {
            orbitCounts.put(vertices.get(i), new int[3]);
        }

        for (int i = 0; i < n; i++) {
            Vertex<V> a = vertices.get(i);

            for (int j = i + 1; j < n; j++) {
                Vertex<V> b = vertices.get(j);

                for (int k = j + 1; k < n; k++) {
                    Vertex<V> c = vertices.get(k);

                    boolean ab = graph.areaAdjacent(a, b);
                    boolean ac = graph.areaAdjacent(a, c);
                    boolean bc = graph.areaAdjacent(b, c);

                    int edgeAB = ab ? 1 : 0;
                    int edgeAC = ac ? 1 : 0;
                    int edgeBC = bc ? 1 : 0;

                    int totalEdges = edgeAB + edgeAC + edgeBC;

                    // ---- DEBUG BLOCK ----
                    System.out.println("\nTRIPLE: "
                            + a.getElement() + ", "
                            + b.getElement() + ", "
                            + c.getElement());

                    System.out.println("Edges: AB=" + ab + " AC=" + ac + " BC=" + bc);

                    if (totalEdges == 0) {
                        System.out.println("→ empty triple");
                        continue;
                    }

                    if (totalEdges == 1) {
                        System.out.println("→ single edge (ignored)");
                        continue;
                    }

                    if (totalEdges == 2) {
                        System.out.println("→ PATH graphlet");

                        if ((edgeAB + edgeBC) == 2) {
                            inc(a, orbitCounts, 0);
                            inc(c, orbitCounts, 0);
                            inc(b, orbitCounts, 1);
                        } else if ((edgeAC + edgeBC) == 2) {
                            inc(a, orbitCounts, 0);
                            inc(b, orbitCounts, 0);
                            inc(c, orbitCounts, 1);
                        } else {
                            inc(b, orbitCounts, 0);
                            inc(c, orbitCounts, 0);
                            inc(a, orbitCounts, 1);
                        }
                    }

                    if (totalEdges == 3) {
                        System.out.println("→ TRIANGLE graphlet");

                        inc(a, orbitCounts, 2);
                        inc(b, orbitCounts, 2);
                        inc(c, orbitCounts, 2);
                    }
                }
            }
        }

        return orbitCounts;
    }
    /**
     * For a given graph, counts how many times each node appears in each orbit,
     * orbit 0: currentNode -> o -> o
     * orbit 1: o <- currentNode -> o
     * orbit 2: node is part of a triangle
     * Then normalizes the aggregated orbit sums to form a 3‑dimensional feature vector.
     * @param graph undirected graph
     * @return 3D feature vector
     */
    private double[] getFeatureVector(Graph<V, E> graph) {

        MyMap<Vertex<V>, int[]> orbitCounts = countOrbits(graph);

        double[] feature = {0, 0, 0};
        int total = 0;

        for (int[] arr : orbitCounts.values()) {
            for (int i = 0; i < 3; i++) {
                feature[i] += arr[i];
                total += arr[i];
            }
        }

        if (total == 0) return new double[]{0, 0, 0};

        for (int i = 0; i < 3; i++) {
            feature[i] /= total;
        }

        return feature;
    }

    /**
     * Computes the cosine similarity between two vectors of the same length.
     * Vectors should be non‑negative (orbit frequencies).
     * @param feature1 - feature vector of a graph
     * @param feature2 - feature vector of another graph
     * @return value between 0.0 - 1.0 representing cosine similarity
     */
    private double cosineSimilarity(double[] feature1, double[] feature2)
    {
        // Back to linear Algebra – cosine similarity between 2 vectors is computed as follows
        // = (v1 dotted with v2)/(||v1|| * ||v2||)
        // it checks if vectors are pointing in the same direction (thus similar) or not
        // if result 1 - vectors point in the same direction, 0 - vectors are perpendicular,
        // -1 - vectors point in opposite directions thus opposite to each other
        double dotProduct = 0;
        double v1Dot = 0;
        double v2Dot = 0;
        for (int i = 0; i < feature1.length; i++)
        {
            dotProduct += (feature1[i] * feature2[i]);
            v1Dot += (feature1[i] * feature1[i]);
            v2Dot += (feature2[i] * feature2[i]);
        }
        if (v1Dot == 0 || v2Dot == 0) return 0.0;
        // I am not sure if we are to use this Math library
        return dotProduct/(Math.sqrt(v1Dot)*Math.sqrt(v2Dot));
    }

    private void inc(Vertex<V> v, MyMap<Vertex<V>, int[]> map, int idx) {
        int[] arr = map.get(v);
        arr[idx]++;
        map.put(v, arr);
    }

}
