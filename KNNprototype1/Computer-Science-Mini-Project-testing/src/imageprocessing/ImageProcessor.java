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

    public AdjacencyListGraph<Node, Integer> buildGraph(File file) {

        try {
            BufferedImage image = ImageIO.read(file);

            int[][] gray = convertToGrayScale(image);

            gray = smoothImage(gray);
            int threshold=120;
            //int threshold = calculateThreshold(gray);

            int[][] binary = convertToBinary(gray, threshold);
            binary = removeNoise(binary);

            //saveBinaryImage(binary, "Datasets/output/binary.png");

            int[][] skeleton = new Skeletonization(binary).getSkeleton();
            //saveBinaryImage(skeleton, "Datasets/output/skeleton.png");

            System.out.println("Threshold: " + threshold);
            AdjacencyListGraph<Node, Integer> g = buildGraphFromBinary(skeleton);
            System.out.println("Graph built with nodes: " + g.numVertices());
            return g;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AdjacencyListGraph<Node, Integer> buildGraphFromBinary(int[][] binary) {
        AdjacencyListGraph<Node, Integer> graph = new AdjacencyListGraph<>();

        int width = binary.length;
        int height = binary[0].length;

        Map<String, Vertex<Node>> specialVertices = new HashMap<>();
        int id = 0;

        // Step 1: create vertices only for endpoints and bifurcations
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (binary[x][y] == 1) {
                    int degree = countWhiteNeighbors(binary, x, y);

                    // endpoint = 1 neighbor, bifurcation = 3 or more neighbors
                    if (degree == 1 || degree >= 5) {
                        if (!isNearExistingSpecialVertex(specialVertices, x, y, 6)) {
                            Node node = new Node(id++, x, y, 1);
                            Vertex<Node> v = graph.insertVertex(node);
                            specialVertices.put(x + "," + y, v);
                        }
                    }
                }
            }
        }

        // Step 2: trace vessel segments between special points
        for (Vertex<Node> startVertex : specialVertices.values()) {
            Node startNode = startVertex.getElement();
            int sx = startNode.getX();
            int sy = startNode.getY();

            for (int[] neighbor : getWhiteNeighbors(binary, sx, sy)) {
                int nx = neighbor[0];
                int ny = neighbor[1];

                traceSegment(binary, sx, sy, nx, ny, startVertex, specialVertices, graph);
            }
        }

        return graph;
    }

    private void saveBinaryImage(int[][] binary, String path) {
        try {
            int width = binary.length;
            int height = binary[0].length;

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {

                    int val = binary[x][y] == 1 ? 255 : 0;
                    int rgb = (val << 16) | (val << 8) | val;

                    img.setRGB(x, y, rgb);
                }
            }

            ImageIO.write(img, "png", new File(path));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Otsu's Algortitm
    private int calculateThreshold(int[][] grayScale){

        int[] hist=new int[256];
        int width= grayScale.length;
        int height=grayScale[0].length;

        for (int[] ints : grayScale) {
            for (int y = 0; y < height; y++) {
                hist[ints[y]]++;
            }
        }

        int total=width*height;
        float sum=0;

        for(int i=0;i<hist.length;i++){
            sum+=i*hist[i];
        }

        float sumB=0;
        int wB = 0;
        int wF;

        float maxVar=0;
        int threshold=0;

        for (int t = 0; t < 256; t++) {
            wB += hist[t];
            if (wB == 0) continue;

            wF = total - wB;
            if (wF == 0) break;

            sumB += (float) (t * hist[t]);

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * wF * (mB - mF) * (mB - mF);

            if (varBetween > maxVar) {
                maxVar = varBetween;
                threshold = t;
            }
        }
        return threshold;
    }
    private int[][] convertToBinary(int[][] grayScale, int threshold){
        int width=grayScale.length;
        int height=grayScale[0].length;

        int[][] binary=new int[width][height];

        for(int x=0;x<width;x++){
            for (int y=0;y< height;y++){

                if(grayScale[x][y]>threshold){
                    binary[x][y]=1;
                }
                else {
                    binary[x][y]=0;
                }
            }
        }
        return binary;
    }

    private int[][] convertToGrayScale(BufferedImage image){
        int width=image.getWidth();
        int height=image.getHeight();

        int[][] grayScale=new int[width][height];

        for (int x=0;x<width;x++){
            for(int y=0;y<height;y++){

                int rgb=image.getRGB(x,y);

                int r=(rgb >> 16) & 0xff;
                int g=(rgb >> 8) & 0xff;
                int b= (rgb) & 0xff;

                int intensity=(r+b+g)/3;

                grayScale[x][y]=intensity;
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

    private int[][] removeNoise(int[][] binary) {
        int width = binary.length;
        int height = binary[0].length;

        int[][] cleaned = new int[width][height];

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                int count = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (binary[x + i][y + j] == 1) count++;
                    }
                }

                // keep only strong pixels
                if (count >= 3) {
                    cleaned[x][y] = 1;
                }
            }
        }

        return cleaned;
    }
    private int countWhiteNeighbors(int[][] binary, int x, int y) {
        int count = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < binary.length && ny >= 0 && ny < binary[0].length) {
                    if (binary[nx][ny] == 1) count++;
                }
            }
        }

        return count;
    }

    private java.util.List<int[]> getWhiteNeighbors(int[][] binary, int x, int y) {
        java.util.List<int[]> neighbors = new java.util.ArrayList<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

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
                              int prevX, int prevY,
                              int currX, int currY,
                              Vertex<Node> startVertex,
                              Map<String, Vertex<Node>> specialVertices,
                              AdjacencyListGraph<Node, Integer> graph) {

        int length = 1;

        while (true) {
            String key = currX + "," + currY;

            // Stop if we reached another special vertex
            if (specialVertices.containsKey(key)) {
                Vertex<Node> endVertex = specialVertices.get(key);

                if (!startVertex.equals(endVertex) && !graph.areaAdjacent(startVertex, endVertex)) {
                    graph.insertEdge(startVertex, endVertex, length);
                }
                return;
            }

            java.util.List<int[]> neighbors = getWhiteNeighbors(binary, currX, currY);

            // Remove the pixel we came from
            java.util.List<int[]> nextSteps = new java.util.ArrayList<>();
            for (int[] n : neighbors) {
                if (!(n[0] == prevX && n[1] == prevY)) {
                    nextSteps.add(n);
                }
            }

            if (nextSteps.isEmpty()) {
                return;
            }

            // If branch unexpectedly appears, stop
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
                                                int x, int y,
                                                int minDistance) {
        for (Vertex<Node> v : specialVertices.values()) {
            Node n = v.getElement();
            int dx = n.getX() - x;
            int dy = n.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < minDistance) {
                return true;
            }
        }
        return false;
    }


}
