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
     *
     * @param graph - graph whose nodes are to be considered
     * @return - a map of the number of times each orbit of size 3 graphlets has appeared in the graph
     */
    private Map<Vertex<V>, int[]> countOrbits(Graph<V,E> graph)
    {
        List<Vertex<V>> vertices = graph.vertices();
        int n = vertices.size();
        Map<Vertex<V>, int[]> orbitcounts = new HashMap<>();

        // Initializing orbit count for each vertex
        for(Vertex<V> vertex : vertices)
        {
            orbitcounts.put(vertex, new int[3]);
        }

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

                    // Consider connected triples only
                }
            }
        }

        return new HashMap<>(); // Only a placeholder

    }

}
