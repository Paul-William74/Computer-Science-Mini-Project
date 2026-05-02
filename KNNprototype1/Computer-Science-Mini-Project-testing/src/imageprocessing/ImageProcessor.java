package imageprocessing;

import graph.AdjacencyListGraph;
import graph.Node;
import graph.Vertex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageProcessor {

    private int lastBifurcationCount = 0;

    public int getLastBifurcationCount() {
        return lastBifurcationCount;
    }

    public AdjacencyListGraph<Node, Integer> buildGraph(File file) {
        try {
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                throw new IOException("Could not read image file: " + file.getName());
            }

            int[][] gray = convertToGrayScale(image);
            gray = smoothImage(gray);

            int threshold = 120;

            int[][] binary = convertToBinary(gray, threshold);
            binary = removeNoise(binary);

            int[][] skeleton = new Skeletonization(binary).getSkeleton();

            createOutputFolderIfNeeded();

            saveBinaryImage(binary, "Datasets/output/binary.png");
            saveBinaryImage(skeleton, "Datasets/output/skeleton.png");

            lastBifurcationCount = countBifurcations(skeleton);

            AdjacencyListGraph<Node, Integer> graph = buildGraphFromBinary(skeleton);

            System.out.println("Threshold: " + threshold);
            System.out.println("Bifurcation count: " + lastBifurcationCount);
            System.out.println("Graph built with nodes: " + graph.numVertices());
            System.out.println("Graph built with edges: " + graph.numEdges());

            return graph;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createOutputFolderIfNeeded() {
        File outputFolder = new File("Datasets/output");

        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
    }

    private int countBifurcations(int[][] binary) {
        int count = 0;

        for (int x = 1; x < binary.length - 1; x++) {
            for (int y = 1; y < binary[0].length - 1; y++) {
                if (binary[x][y] == 1 && countWhiteNeighbors(binary, x, y) >= 5) {
                    count++;
                }
            }
        }

        return count;
    }

    private AdjacencyListGraph<Node, Integer> buildGraphFromBinary(int[][] binary) {
        AdjacencyListGraph<Node, Integer> graph = new AdjacencyListGraph<>();

        int width = binary.length;
        int height = binary[0].length;

        Map<String, Vertex<Node>> specialVertices = new HashMap<>();
        int id = 0;

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                if (binary[x][y] == 1) {
                    int degree = countWhiteNeighbors(binary, x, y);

                    if (degree == 1 || degree >= 5) {
                        if (!isNearExistingSpecialVertex(specialVertices, x, y, 6)) {
                            Node node = new Node(id++, x, y, 1);
                            Vertex<Node> vertex = graph.insertVertex(node);
                            specialVertices.put(x + "," + y, vertex);
                        }
                    }
                }
            }
        }

        for (Vertex<Node> startVertex : specialVertices.values()) {
            Node startNode = startVertex.getElement();

            int startX = startNode.getX();
            int startY = startNode.getY();

            for (int[] neighbor : getWhiteNeighbors(binary, startX, startY)) {
                traceSegment(
                        binary,
                        startX,
                        startY,
                        neighbor[0],
                        neighbor[1],
                        startVertex,
                        specialVertices,
                        graph
                );
            }
        }

        return graph;
    }

    private int countWhiteNeighbors(int[][] binary, int x, int y) {
        int count = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                if (dx == 0 && dy == 0) {
                    continue;
                }

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < binary.length && ny >= 0 && ny < binary[0].length) {
                    if (binary[nx][ny] == 1) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    private java.util.List<int[]> getWhiteNeighbors(int[][] binary, int x, int y) {
        java.util.List<int[]> neighbors = new java.util.ArrayList<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                if (dx == 0 && dy == 0) {
                    continue;
                }

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < binary.length && ny >= 0 && ny < binary[0].length) {
                    if (binary[nx][ny] == 1) {
                        neighbors.add(new int[]{nx, ny});
                    }
                }
            }
        }

        return neighbors;
    }

    private void traceSegment(int[][] binary,
                              int prevX,
                              int prevY,
                              int currX,
                              int currY,
                              Vertex<Node> startVertex,
                              Map<String, Vertex<Node>> specialVertices,
                              AdjacencyListGraph<Node, Integer> graph) {

        int length = 1;

        while (true) {
            String key = currX + "," + currY;

            if (specialVertices.containsKey(key)) {
                Vertex<Node> endVertex = specialVertices.get(key);

                if (!startVertex.equals(endVertex) && !graph.areaAdjacent(startVertex, endVertex)) {
                    graph.insertEdge(startVertex, endVertex, length);
                }

                return;
            }

            java.util.List<int[]> neighbors = getWhiteNeighbors(binary, currX, currY);
            java.util.List<int[]> nextSteps = new java.util.ArrayList<>();

            for (int[] neighbor : neighbors) {
                if (!(neighbor[0] == prevX && neighbor[1] == prevY)) {
                    nextSteps.add(neighbor);
                }
            }

            if (nextSteps.isEmpty()) {
                return;
            }

            if (nextSteps.size() > 1) {
                return;
            }

            prevX = currX;
            prevY = currY;

            currX = nextSteps.get(0)[0];
            currY = nextSteps.get(0)[1];

            length++;
        }
    }

    private boolean isNearExistingSpecialVertex(Map<String, Vertex<Node>> specialVertices,
                                                int x,
                                                int y,
                                                int minDistance) {

        for (Vertex<Node> vertex : specialVertices.values()) {
            Node node = vertex.getElement();

            int dx = node.getX() - x;
            int dy = node.getY() - y;

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < minDistance) {
                return true;
            }
        }

        return false;
    }

    private void saveBinaryImage(int[][] binary, String path) {
        try {
            int width = binary.length;
            int height = binary[0].length;

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {

                    int value = binary[x][y] == 1 ? 255 : 0;
                    int rgb = (value << 16) | (value << 8) | value;

                    img.setRGB(x, y, rgb);
                }
            }

            ImageIO.write(img, "png", new File(path));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[][] convertToGrayScale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] grayScale = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int rgb = image.getRGB(x, y);

                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int intensity = (r + g + b) / 3;

                grayScale[x][y] = intensity;
            }
        }

        return grayScale;
    }

    private int[][] smoothImage(int[][] gray) {
        int width = gray.length;
        int height = gray[0].length;

        int[][] result = new int[width][height];

        int[][] kernel = {
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                int sum = 0;
                int weight = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        sum += gray[x + i][y + j] * kernel[i + 1][j + 1];
                        weight += kernel[i + 1][j + 1];
                    }
                }

                result[x][y] = sum / weight;
            }
        }

        return result;
    }

    private int[][] convertToBinary(int[][] grayScale, int threshold) {
        int width = grayScale.length;
        int height = grayScale[0].length;

        int[][] binary = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                binary[x][y] = grayScale[x][y] > threshold ? 1 : 0;
            }
        }

        return binary;
    }

    private int[][] removeNoise(int[][] binary) {
        int width = binary.length;
        int height = binary[0].length;

        int[][] cleaned = new int[width][height];

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                int count = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (binary[x + i][y + j] == 1) {
                            count++;
                        }
                    }
                }

                if (count >= 3) {
                    cleaned[x][y] = 1;
                }
            }
        }

        return cleaned;
    }
}