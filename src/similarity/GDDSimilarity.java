package similarity;
import graph.Graph;
import graph.Vertex;

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
        int[] feature1 = getFeatureVector(graph1);
        int[] feature2 = getFeatureVector(graph2);
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
    private Map<Vertex<V>, int[]> countOrbits(Graph<V,E> graph)
    {
        List<Vertex<V>> vertices = graph.vertices();
        int n = vertices.size();
        Map<Vertex<V>, int[]> orbitCounts = new HashMap<>();

        // Initializing orbit count for each vertex
        for(Vertex<V> vertex : vertices)
        {
            orbitCounts.put(vertex, new int[3]);
        }
        // Loop ensures to consider every possible triplet of vertices in the graph
        for (int i = 0; i < n-2;i++)
        {
            for (int j = i + 1; j < n-1; j++)
            {
                for (int k = j + 1; k < n; k++)
                {
                    // 3 sequential vertices in the graph
                    Vertex<V> a = vertices.get(i);
                    Vertex<V> b = vertices.get(j);
                    Vertex<V> c = vertices.get(k);

                    // Count edges among the 3 vertices
                    int edgeAB = graph.areaAdjacent(a,b) ? 1: 0;
                    int edgeAC = graph.areaAdjacent(a,c) ? 1: 0;
                    int edgeBC = graph.areaAdjacent(b,c) ? 1: 0;
                    int totalEdges = edgeAB + edgeAC + edgeBC;

                    // extract current count of orbits to modify/update
                    int[] aOrbits = orbitCounts.get(a);
                    int[] bOrbits = orbitCounts.get(b);
                    int[] cOrbits = orbitCounts.get(c);

                    // Consider connected triples only (2 or 3 edges)
                    if (totalEdges == 2)
                    {
                        // Figure orbit of each node
                        if ((edgeAB + edgeBC) == 2)
                        {
                            // a - b - c : a and c are endpoints with b in the middle
                            aOrbits[0]++; // a - o - o type 0
                            cOrbits[0]++; // c - o - o type 0
                            bOrbits[1]++; // o - b - o type 1
                        }
                        else if ((edgeAC + edgeBC) == 2)
                        {
                            // a - c - b : a and b are endpoints with c in the middle
                            aOrbits[0]++;
                            bOrbits[0]++;
                            cOrbits[1]++;
                        }
                        else if ((edgeAB + edgeAC) == 2)
                        {
                            // b - a - c : b and c are endpoints with a in the middle
                            bOrbits[0]++;
                            cOrbits[0]++;
                            aOrbits[1]++;
                        }
                    }
                    else if(totalEdges == 3)
                    {
                        //
                        aOrbits[2]++;
                        bOrbits[2]++;
                        cOrbits[2]++;
                    }
                    // Update orbit counts
                    orbitCounts.put(a, aOrbits);
                    orbitCounts.put(b, bOrbits);
                    orbitCounts.put(c, cOrbits);

                    // Last case totalEdges <=1 -> ignore
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
    private int[] getFeatureVector(Graph<V,E> graph)
    {
        Map<Vertex<V>, int[]> orbitCounts = countOrbits(graph);
        int totalGraphlets = 0;     // Will be used to normalize the feature vector
        int[] feature = {0,0,0};    // Contains the aggregate amount of each of the 3 orbits considered
                                    // for each vertex within the graph. Thus getting the graph's "fingerprint"

        for (int[] vertexOrbits : orbitCounts.values())
        {
            for (int orbit = 0; orbit < 3; orbit++)
            {
                feature[orbit] += vertexOrbits[orbit];
                totalGraphlets += vertexOrbits[orbit];
            }
        }
        // In case no size 3 graphlets are in the graph - return zero vector
        if (totalGraphlets == 0)
            return new int[]{0,0,0};

        // Normalise
        for (int i = 0; i < 3; i++)
        {
            feature[i] = feature[i]/totalGraphlets;
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
    private double cosineSimilarity(int[] feature1, int[] feature2)
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


}
