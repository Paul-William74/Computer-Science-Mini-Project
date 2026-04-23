package imageprocessing;

import graph.AdjacencyListGraph;
import graph.Node;
import graph.Vertex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Enhanced ImageProcessor that builds a vessel‑segment graph with region‑based features.
 * Now correctly handles endpoints and bifurcations.
 */
public class ImageProcessor {

    // ----------------------------------------------------------------------
    // Public API: build the segment graph (called from MainTest)
    // ----------------------------------------------------------------------
    public AdjacencyListGraph<Node, Double> buildSegmentGraph(File imageFile) throws IOException {
        BufferedImage originalImg = ImageIO.read(imageFile);
        int[][] gray = convertToGrayScale(originalImg);

        // Smooth to reduce noise before thresholding
        int[][] smoothed = smoothImage(gray);

        // Binarize with Otsu
        int threshold = calculateThreshold(smoothed);
        System.out.println("Otsu threshold: " + threshold);
        int[][] binary = convertToBinary(smoothed, threshold);

        // Remove small noise (3x3 majority filter)
        binary = removeNoise(binary);

        // Save intermediate results for debugging
        saveBinaryImage(binary, "src/Datasets/output/binary.png");

        // Skeletonize
        int[][] skeleton = skeletonize(binary);
        saveBinaryImage(skeleton, "src/Datasets/output/skeleton.png");

        // 1. Find bifurcations (degree >= 3) – these are the only junctions that split segments
        Map<String, Junction> junctionMap = findBifurcations(skeleton);
        System.out.println("Found " + junctionMap.size() + " bifurcations.");

        // 2. Extract segments (paths between bifurcations or between bifurcation and endpoint)
        List<Segment> segments = extractSegments(skeleton, junctionMap);
        System.out.println("Extracted " + segments.size() + " vessel segments.");

        // 3. For each segment, compute features from original grayscale and binary mask
        for (Segment seg : segments) {
            computeSegmentFeatures(seg, gray, binary);
        }

        // 4. Build graph: each segment becomes a Vertex<Node>
        AdjacencyListGraph<Node, Double> graph = new AdjacencyListGraph<>();
        Map<Segment, Vertex<Node>> segmentToVertex = new HashMap<>();

        for (Segment seg : segments) {
            Node node = new Node(seg.id, 0, 0);
            node.setArea(seg.area);
            node.setCircularity(seg.circularity);
            node.setAspectRatio(seg.aspectRatio);
            node.setTexture(seg.textureContrast);   // GLCM contrast
            // Optionally store mean intensity in node if you add a field
            Vertex<Node> v = graph.insertVertex(node);
            segmentToVertex.put(seg, v);
        }

        // 5. Add edges between segments that share a bifurcation
        Map<Junction, List<Segment>> junctionToSegments = new HashMap<>();
        for (Segment seg : segments) {
            if (seg.junctionA != null) junctionToSegments.computeIfAbsent(seg.junctionA, k -> new ArrayList<>()).add(seg);
            if (seg.junctionB != null) junctionToSegments.computeIfAbsent(seg.junctionB, k -> new ArrayList<>()).add(seg);
        }

        for (List<Segment> segList : junctionToSegments.values()) {
            for (int i = 0; i < segList.size(); i++) {
                for (int j = i + 1; j < segList.size(); j++) {
                    Vertex<Node> u = segmentToVertex.get(segList.get(i));
                    Vertex<Node> v = segmentToVertex.get(segList.get(j));
                    if (!graph.areaAdjacent(u, v)) {
                        graph.insertEdge(u, v, 1.0);
                    }
                }
            }
        }

        // Optional: save a coloured visualisation of segments
        saveSegmentColourImage(skeleton, segments, "src/Datasets/output/segments_coloured.png");

        System.out.println("Segment graph built: " + segments.size() + " segments, " +
                graph.numEdges() + " edges.");
        return graph;
    }

    // ----------------------------------------------------------------------
    // STEP 1: Find only bifurcations (degree >= 3) – not endpoints
    // ----------------------------------------------------------------------
    private Map<String, Junction> findBifurcations(int[][] skeleton) {
        Map<String, Junction> map = new HashMap<>();
        int w = skeleton.length;
        int h = skeleton[0].length;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (skeleton[x][y] == 1) {
                    int degree = countSkeletonNeighbors(skeleton, x, y);
                    if (degree >= 3) {   // only bifurcations, not endpoints
                        map.put(x + "," + y, new Junction(x, y, degree));
                    }
                }
            }
        }
        return map;
    }

    // 8‑neighbour count for skeleton
    private int countSkeletonNeighbors(int[][] skel, int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < skel.length && ny >= 0 && ny < skel[0].length) {
                    if (skel[nx][ny] == 1) count++;
                }
            }
        }
        return count;
    }

    // ----------------------------------------------------------------------
    // STEP 2: Extract segments (bifurcations split, endpoints do not)
    // ----------------------------------------------------------------------
    private List<Segment> extractSegments(int[][] skeleton, Map<String, Junction> bifurcationMap) {
        int w = skeleton.length;
        int h = skeleton[0].length;
        boolean[][] visited = new boolean[w][h];
        List<Segment> segments = new ArrayList<>();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (skeleton[x][y] == 1 && !visited[x][y]) {
                    Segment seg = new Segment();
                    traverseSegment(skeleton, x, y, visited, seg, bifurcationMap);
                    if (!seg.pixels.isEmpty()) {
                        // Determine the two end points of the segment – they could be bifurcations or endpoints
                        int[] first = seg.pixels.get(0);
                        int[] last = seg.pixels.get(seg.pixels.size() - 1);
                        seg.junctionA = findJunctionAt(first[0], first[1], bifurcationMap);
                        seg.junctionB = findJunctionAt(last[0], last[1], bifurcationMap);
                        segments.add(seg);
                    }
                }
            }
        }
        return segments;
    }

    // DFS along skeleton, stopping only at bifurcations (degree >= 3)
    // Endpoints (degree 1) are included as normal pixels.
    private void traverseSegment(int[][] skeleton, int x, int y, boolean[][] visited,
                                 Segment seg, Map<String, Junction> bifurcationMap) {
        if (x < 0 || x >= skeleton.length || y < 0 || y >= skeleton[0].length) return;
        if (skeleton[x][y] != 1) return;
        if (visited[x][y]) return;

        // Stop if we hit a bifurcation that is NOT the starting pixel (to avoid splitting)
        boolean isBifurcation = bifurcationMap.containsKey(x + "," + y);
        if (isBifurcation && !seg.pixels.isEmpty()) {
            return;   // do not include the bifurcation pixel in this segment (it belongs to multiple segments)
        }

        visited[x][y] = true;
        seg.addPixel(x, y);

        // Explore neighbours (8‑direction)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx >= 0 && nx < skeleton.length && ny >= 0 && ny < skeleton[0].length) {
                    if (skeleton[nx][ny] == 1 && !visited[nx][ny]) {
                        // If neighbour is a bifurcation, we stop before going into it
                        if (bifurcationMap.containsKey(nx + "," + ny)) {
                            // Do not traverse into the bifurcation, but we don't add it here.
                            // The bifurcation will be attached later as seg.junctionA/B.
                            continue;
                        }
                        traverseSegment(skeleton, nx, ny, visited, seg, bifurcationMap);
                    }
                }
            }
        }
    }

    private Junction findJunctionAt(int x, int y, Map<String, Junction> bifurcationMap) {
        return bifurcationMap.get(x + "," + y);
    }

    // ----------------------------------------------------------------------
    // FEATURE COMPUTATION (same as before, no changes)
    // ----------------------------------------------------------------------
    private void computeSegmentFeatures(Segment seg, int[][] gray, int[][] binary) {
        // 1. Area: dilate skeleton pixels by radius 2 and intersect with binary mask
        Set<String> vesselPixels = new HashSet<>();
        for (int[] p : seg.pixels) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    int nx = p[0] + dx, ny = p[1] + dy;
                    if (nx >= 0 && nx < binary.length && ny >= 0 && ny < binary[0].length) {
                        if (binary[nx][ny] == 1) {
                            vesselPixels.add(nx + "," + ny);
                        }
                    }
                }
            }
        }
        seg.area = vesselPixels.size();

        // 2. Length (skeleton pixels)
        double length = seg.pixels.size();

        // 3. Average width (sampling)
        double totalWidth = 0;
        int samples = 0;
        for (int i = 0; i < seg.pixels.size(); i += Math.max(1, seg.pixels.size() / 20)) {
            int[] p = seg.pixels.get(i);
            int radius = 1;
            while (radius < 20) {
                boolean edge = false;
                for (int dx = -radius; dx <= radius; dx++) {
                    int nx = p[0] + dx;
                    if (nx >= 0 && nx < binary.length) {
                        if (binary[nx][p[1]] == 0) { edge = true; break; }
                    }
                }
                for (int dy = -radius; dy <= radius; dy++) {
                    int ny = p[1] + dy;
                    if (ny >= 0 && ny < binary[0].length) {
                        if (binary[p[0]][ny] == 0) { edge = true; break; }
                    }
                }
                if (edge) break;
                radius++;
            }
            totalWidth += (radius * 2);
            samples++;
        }
        double avgWidth = samples > 0 ? totalWidth / samples : 2.0;
        seg.aspectRatio = length / avgWidth;

        // 4. Circularity proxy: area / (length^2)
        seg.circularity = seg.area / (length * length + 1e-6);

        // 5. Texture: GLCM contrast and mean intensity
        double[] tex = computeGLCMContrastAndMean(seg, gray);
        seg.textureContrast = tex[0];
        seg.textureMean = tex[1];
    }

    private double[] computeGLCMContrastAndMean(Segment seg, int[][] gray) {
        Set<int[]> pixels = new HashSet<>();
        for (int[] p : seg.pixels) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    int nx = p[0] + dx, ny = p[1] + dy;
                    if (nx >= 0 && nx < gray.length && ny >= 0 && ny < gray[0].length) {
                        pixels.add(new int[]{nx, ny});
                    }
                }
            }
        }

        // Quantise gray levels to 0..7
        int[][] quant = new int[gray.length][gray[0].length];
        for (int x = 0; x < gray.length; x++) {
            for (int y = 0; y < gray[0].length; y++) {
                quant[x][y] = (gray[x][y] * 8) / 256;
            }
        }

        int[][] glcm = new int[8][8];
        double sumIntensity = 0;
        int countPixels = 0;

        for (int[] p : pixels) {
            int x = p[0], y = p[1];
            sumIntensity += gray[x][y];
            countPixels++;

            if (x + 1 < gray.length && pixels.contains(new int[]{x+1, y})) {
                int i = quant[x][y];
                int j = quant[x+1][y];
                glcm[i][j]++;
                glcm[j][i]++;
            }
            if (y + 1 < gray[0].length && pixels.contains(new int[]{x, y+1})) {
                int i = quant[x][y];
                int j = quant[x][y+1];
                glcm[i][j]++;
                glcm[j][i]++;
            }
        }

        double contrast = 0.0;
        double totalPairs = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                totalPairs += glcm[i][j];
                contrast += glcm[i][j] * (i - j) * (i - j);
            }
        }
        if (totalPairs > 0) contrast /= totalPairs;

        double meanIntensity = countPixels > 0 ? sumIntensity / countPixels : 0;
        return new double[]{contrast, meanIntensity};
    }

    // ----------------------------------------------------------------------
    // VISUALISATION: colour each segment uniquely
    // ----------------------------------------------------------------------
    private void saveSegmentColourImage(int[][] skeleton, List<Segment> segments, String path) {
        int w = skeleton.length;
        int h = skeleton[0].length;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Random rand = new Random(42);
        Map<Segment, Integer> segmentColor = new HashMap<>();
        for (Segment seg : segments) {
            int r = rand.nextInt(256);
            int g = rand.nextInt(256);
            int b = rand.nextInt(256);
            segmentColor.put(seg, (r << 16) | (g << 8) | b);
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (skeleton[x][y] == 1) {
                    Segment owner = null;
                    for (Segment seg : segments) {
                        for (int[] p : seg.pixels) {
                            if (p[0] == x && p[1] == y) {
                                owner = seg;
                                break;
                            }
                        }
                        if (owner != null) break;
                    }
                    int colour = owner != null ? segmentColor.get(owner) : 0xFFFFFF;
                    img.setRGB(x, y, colour);
                } else {
                    img.setRGB(x, y, 0x000000);
                }
            }
        }
        try {
            ImageIO.write(img, "png", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // YOUR EXISTING METHODS (unchanged, but all present)
    // ----------------------------------------------------------------------
    private int[][] convertToGrayScale(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int[][] gray = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int rgb = image.getRGB(x, y);

                int g = (rgb >> 8) & 0xff;

                gray[x][y] = 255 - g; // invert so vessels become bright
            }
        }

        return gray;
    }

    private int[][] smoothImage(int[][] gray) {
        int width = gray.length;
        int height = gray[0].length;
        int[][] result = new int[width][height];
        int[][] kernel = {{1,2,1},{2,4,2},{1,2,1}};
        for (int x = 1; x < width-1; x++) {
            for (int y = 1; y < height-1; y++) {
                int sum = 0, weight = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        sum += gray[x+i][y+j] * kernel[i+1][j+1];
                        weight += kernel[i+1][j+1];
                    }
                }
                result[x][y] = sum / weight;
            }
        }
        return result;
    }

    private int calculateThreshold(int[][] grayScale) {
        int[] hist = new int[256];
        int width = grayScale.length;
        int height = grayScale[0].length;
        for (int[] row : grayScale) {
            for (int y = 0; y < height; y++) {
                hist[row[y]]++;
            }
        }
        int total = width * height;
        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * hist[i];
        float sumB = 0;
        int wB = 0, wF;
        float maxVar = 0;
        int threshold = 0;
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
        for (int x = 1; x < width-1; x++) {
            for (int y = 1; y < height-1; y++) {
                int count = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (binary[x+i][y+j] == 1) count++;
                    }
                }
                cleaned[x][y] = count >= 3 ? 1 : 0;
            }
        }
        return cleaned;
    }

    private int[][] skeletonize(int[][] binary) {
        return new Skeletonization(binary).getSkeleton();
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
}