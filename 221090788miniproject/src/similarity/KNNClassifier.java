package similarity;

import graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KNNClassifier<V, E> {

    private final int k;
    private final GDDSimilarity<V, E> gdd;
    private final List<double[]> featureVectors;
    private final List<String> labels;
    private final List<String> sampleNames;

    public KNNClassifier(int k) {
        this.k = k;
        this.gdd = new GDDSimilarity<>();
        this.featureVectors = new ArrayList<>();
        this.labels = new ArrayList<>();
        this.sampleNames = new ArrayList<>();
    }

    public void addTrainingSample(Graph<V, E> graph, String label, String sampleName) {
        System.out.println("Starting feature extraction for: " + sampleName);

        double[] features = gdd.getFeatureVector(graph);

        System.out.println("Finished feature extraction for: " + sampleName);
        System.out.println("TRAINING LABEL ADDED: " + label);
        System.out.println("Features: " + Arrays.toString(features));

        featureVectors.add(features);
        labels.add(label);
        sampleNames.add(sampleName);
    }

    public String predict(Graph<V, E> graph) {
        if (featureVectors.isEmpty()) {
            throw new IllegalStateException("No training data loaded.");
        }

        double[] inputFeatures = gdd.getFeatureVector(graph);

        System.out.println("Test features: " + Arrays.toString(inputFeatures));

        List<Neighbor> neighbors = buildSortedNeighbors(inputFeatures);

        System.out.println("Nearest neighbors:");
        int displayLimit = Math.min(k, neighbors.size());

        for (int i = 0; i < displayLimit; i++) {
            Neighbor n = neighbors.get(i);
            System.out.println(
                    n.sampleName + " | " +
                            n.label + " | distance = " +
                            String.format("%.4f", n.distance)
            );
        }

        Map<String, Integer> votes = new HashMap<>();
        Map<String, Double> distanceTotals = new HashMap<>();

        int limit = Math.min(k, neighbors.size());

        for (int i = 0; i < limit; i++) {
            Neighbor n = neighbors.get(i);

            votes.put(n.label, votes.getOrDefault(n.label, 0) + 1);
            distanceTotals.put(n.label, distanceTotals.getOrDefault(n.label, 0.0) + n.distance);
        }

        String bestLabel = null;
        int bestVotes = -1;
        double bestDistance = Double.MAX_VALUE;

        for (String label : votes.keySet()) {
            int voteCount = votes.get(label);
            double distanceTotal = distanceTotals.get(label);

            if (voteCount > bestVotes ||
                    (voteCount == bestVotes && distanceTotal < bestDistance)) {

                bestVotes = voteCount;
                bestDistance = distanceTotal;
                bestLabel = label;
            }
        }

        System.out.println("Prediction votes: " + votes);
        System.out.println("Final prediction: " + bestLabel);

        return bestLabel;
    }

    public List<String> getNearestNeighbors(Graph<V, E> graph) {
        if (featureVectors.isEmpty()) {
            throw new IllegalStateException("No training data loaded.");
        }

        double[] inputFeatures = gdd.getFeatureVector(graph);
        List<Neighbor> neighbors = buildSortedNeighbors(inputFeatures);

        List<String> result = new ArrayList<>();
        int limit = Math.min(k, neighbors.size());

        for (int i = 0; i < limit; i++) {
            Neighbor n = neighbors.get(i);

            result.add(
                    n.sampleName +
                            " | " + n.label +
                            " | distance = " + String.format("%.4f", n.distance)
            );
        }

        return result;
    }

    private List<Neighbor> buildSortedNeighbors(double[] inputFeatures) {
        List<Neighbor> neighbors = new ArrayList<>();

        for (int i = 0; i < featureVectors.size(); i++) {
            double distance = euclideanDistance(inputFeatures, featureVectors.get(i));
            neighbors.add(new Neighbor(distance, labels.get(i), sampleNames.get(i)));
        }

        neighbors.sort(Comparator.comparingDouble(n -> n.distance));

        return neighbors;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;

        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    private static class Neighbor {
        double distance;
        String label;
        String sampleName;

        Neighbor(double distance, String label, String sampleName) {
            this.distance = distance;
            this.label = label;
            this.sampleName = sampleName;
        }
    }
}