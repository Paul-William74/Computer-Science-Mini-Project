package similarity;
import graph.Graph;
import graph.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * So this class is basically a helper class with methods to be used to compute GDD similarity between 2 graphs
 */
public final class GDDSimilarity<V,E> {
    public double computeSimilarity(Graph<V,E> graph1, Graph<V,E> graph2)
    {

        return 0.00; // Placeholder
    }
    // ---------------------------- Helper methods ----------------------------

    /**
     * Types of Orbits of size 3 graphlets:
     * type 0: currentNode -> o -> o
     * type 1: o <- currentNode -> o
     * type 2: triangular shape where all nodes are connected to each other
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

}
