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

            int threshold = calculateThreshold(gray);

            int[][] binary = convertToBinary(gray, threshold);
            saveBinaryImage(binary, "src/Datasets/output");

            return buildGraphFromBinary(binary);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AdjacencyListGraph<Node, Integer> buildGraphFromBinary(int[][] binary) {

        AdjacencyListGraph<Node, Integer> graph = new AdjacencyListGraph<>();

        Map<String, Vertex<Node>> pixelMap = new HashMap<>();

        int id = 0;

        int width = binary.length;
        int height = binary[0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                if (binary[x][y] == 1) {

                    String key = x + "," + y;

                    if (!pixelMap.containsKey(key)) {
                        Node data = new Node(id++, x, y, 1);
                        Vertex<Node> v = graph.insertVertex(data);
                        pixelMap.put(key, v);
                    }

                    Vertex<Node> current = pixelMap.get(key);

                    // LEFT
                    if (x > 0 && binary[x - 1][y] == 1) {
                        Vertex<Node> left = pixelMap.get((x - 1) + "," + y);
                        if (left != null && !graph.areaAdjacent(current, left)) {
                            graph.insertEdge(current, left, 1);
                        }
                    }

                    // UP
                    if (y > 0 && binary[x][y - 1] == 1) {
                        Vertex<Node> up = pixelMap.get(x + "," + (y - 1));
                        if (up != null && !graph.areaAdjacent(current, up)) {
                            graph.insertEdge(current, up, 1);
                        }
                    }
                }
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
        int wB = 0, wF=0;

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
}
