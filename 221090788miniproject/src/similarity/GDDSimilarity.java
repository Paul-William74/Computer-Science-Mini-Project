package similarity;

import graph.Graph;
import graph.Vertex;
import graph.adt.MyList;
import graph.adt.MyMap;
import graph.implementation.MyHashMap;

public final class GDDSimilarity<V, E> {

    public double computeSimilarity(Graph<V, E> graph1, Graph<V, E> graph2) {
        double[] feature1 = getFeatureVector(graph1);
        double[] feature2 = getFeatureVector(graph2);

        return cosineSimilarity(feature1, feature2) * 100;
    }

    private MyMap<Vertex<V>, int[]> countOrbits(Graph<V, E> graph) {
        MyList<Vertex<V>> vertices = graph.vertices();
        int n = vertices.size();

        MyMap<Vertex<V>, int[]> orbitCounts = new MyHashMap<>();

        for (int i = 0; i < n; i++) {
            orbitCounts.put(vertices.get(i), new int[3]);
        }

        int emptyTriples = 0;
        int singleEdgeTriples = 0;
        int pathGraphlets = 0;
        int triangleGraphlets = 0;

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

                    if (totalEdges == 0) {
                        emptyTriples++;
                    } else if (totalEdges == 1) {
                        singleEdgeTriples++;
                    } else if (totalEdges == 2) {
                        pathGraphlets++;

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

                    } else if (totalEdges == 3) {
                        triangleGraphlets++;

                        inc(a, orbitCounts, 2);
                        inc(b, orbitCounts, 2);
                        inc(c, orbitCounts, 2);
                    }
                }
            }
        }

        System.out.println("GDD summary:");
        System.out.println("Vertices: " + n);
        System.out.println("Empty triples: " + emptyTriples);
        System.out.println("Single-edge triples ignored: " + singleEdgeTriples);
        System.out.println("Path graphlets: " + pathGraphlets);
        System.out.println("Triangle graphlets: " + triangleGraphlets);

        return orbitCounts;
    }

    public double[] getFeatureVector(Graph<V, E> graph) {
        MyMap<Vertex<V>, int[]> orbitCounts = countOrbits(graph);

        double[] feature = {0, 0, 0};
        int total = 0;

        for (int[] arr : orbitCounts.values()) {
            for (int i = 0; i < 3; i++) {
                feature[i] += arr[i];
                total += arr[i];
            }
        }

        if (total != 0) {
            for (int i = 0; i < 3; i++) {
                feature[i] /= total;
            }
        }

        double vertexCount = graph.numVertices();
        double edgeCount = graph.numEdges();
        double avgDegree = vertexCount == 0 ? 0.0 : (2.0 * edgeCount) / vertexCount;

        int branchPoints = 0;

        for (Vertex<V> v : graph.vertices()) {
            if (graph.degree(v) >= 3) {
                branchPoints++;
            }
        }

        double normalizedBranchPoints = vertexCount == 0 ? 0.0 : branchPoints / vertexCount;

        return new double[]{
                feature[0],
                feature[1],
                feature[2],
                avgDegree,
                normalizedBranchPoints
        };
    }

    private double cosineSimilarity(double[] feature1, double[] feature2) {
        double dotProduct = 0;
        double v1Dot = 0;
        double v2Dot = 0;

        for (int i = 0; i < feature1.length; i++) {
            dotProduct += feature1[i] * feature2[i];
            v1Dot += feature1[i] * feature1[i];
            v2Dot += feature2[i] * feature2[i];
        }

        if (v1Dot == 0 || v2Dot == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(v1Dot) * Math.sqrt(v2Dot));
    }

    private void inc(Vertex<V> v, MyMap<Vertex<V>, int[]> map, int idx) {
        int[] arr = map.get(v);
        arr[idx]++;
        map.put(v, arr);
    }
}