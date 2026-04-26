package similarity;

import graph.Graph;
import graph.Node;
import imageprocessing.ImageProcessor;
import similarity.KNNClassifier;
import similarity.TrainingLoader;

import java.io.File;
import java.util.List;

public class MainTestKNN {

    public static void main(String[] args) {
        try {
            KNNClassifier<Node, Integer> knn = TrainingLoader.loadTrainingData(3);

            ImageProcessor processor = new ImageProcessor();
            Graph<Node, Integer> testGraph =
                    processor.buildGraph(new File("Datasets/tests/images/01_test.tif"));

            String prediction = knn.predict(testGraph);
            List<String> neighbors = knn.getNearestNeighbors(testGraph);

            System.out.println("Prediction: " + prediction);
            System.out.println("Nearest Neighbors:");
            for (String neighbor : neighbors) {
                System.out.println(neighbor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}