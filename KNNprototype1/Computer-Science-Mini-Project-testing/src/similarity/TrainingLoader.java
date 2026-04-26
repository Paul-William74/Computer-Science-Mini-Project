package similarity;

import graph.Graph;
import graph.Node;
import imageprocessing.ImageProcessor;

import java.io.File;

public class TrainingLoader {

    public static KNNClassifier<Node, Integer> loadTrainingData(int k) {
        KNNClassifier<Node, Integer> knn = new KNNClassifier<>(k);
        ImageProcessor processor = new ImageProcessor();

        loadFolder(knn, processor, "Datasets/knn/healthy", "healthy");
        loadFolder(knn, processor, "Datasets/knn/diseased", "diseased");

        return knn;
    }

    private static void loadFolder(KNNClassifier<Node, Integer> knn,
                                   ImageProcessor processor,
                                   String folderPath,
                                   String label) {

        File folder = new File(folderPath);

        System.out.println("Looking for folder: " + folder.getAbsolutePath());

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Folder not found: " + folderPath);
            return;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in: " + folderPath);
            return;
        }

        System.out.println("Found " + files.length + " files in " + label + " folder.");

        for (File file : files) {
            if (file.isFile()) {
                try {
                    System.out.println("Processing: " + file.getName() + " [" + label + "]");
                    Graph<Node, Integer> graph = processor.buildGraph(file);
                    System.out.println("Built graph: " + file.getName());

                    knn.addTrainingSample(graph, label, file.getName());
                    System.out.println("Loaded " + file.getName() + " as " + label);

                } catch (Exception e) {
                    System.out.println("Failed to load " + file.getName() + " from " + label);
                    e.printStackTrace();
                }
            }
        }
    }

}