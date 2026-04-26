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
 * Processes retinal fundus images and builds a vessel-segment graph.
 *
 * <p>Pipeline:</p>
 * <ol>
 *     <li>Green channel enhancement</li>
 *     <li>Gaussian smoothing</li>
 *     <li>Masked Otsu thresholding</li>
 *     <li>Noise removal</li>
 *     <li>Skeletonization</li>
 *     <li>Segment extraction</li>
 *     <li>Graph construction</li>
 * </ol>
 */
public class ImageProcessor {

    /**
     * Builds the vessel segment graph from an image file.
     *
     * @param imageFile retinal image file
     * @return graph of vessel segments
     * @throws IOException if image loading fails
     */
    public AdjacencyListGraph<Node, Double> buildSegmentGraph(File imageFile)
            throws IOException {

        BufferedImage image = ImageIO.read(imageFile);

        // Convert to grayscale using inverted green channel (vessels appear bright)
        int[][] gray = convertToGrayScale(image);

        // Smooth to reduce noise
        int[][] smooth = smoothImage(gray);

        // Create FOV mask (exclude dark background)
        boolean[][] mask = createFOVMask(gray);

        // Masked Otsu thresholding
        int threshold = calculateThresholdMasked(smooth, mask);

        // Clamp threshold to reasonable range for retinal images
        threshold = Math.clamp(threshold, 15, 65);

        System.out.println("Otsu threshold: " + threshold);

        // Binarize using threshold and mask
        int[][] binary = convertToBinary(smooth, threshold, mask);

        // Remove isolated noise pixels
        binary = removeNoise(binary);

        saveBinaryImage(binary, "src/Datasets/output/binary.png");

        // Skeletonize to 1-pixel wide lines
        int[][] skeleton = skeletonize(binary);

        saveBinaryImage(skeleton, "src/Datasets/output/skeleton.png");

        // Find all junctions (bifurcations and endpoints)
        Map<String, Junction> junctions = findJunctions(skeleton);

        System.out.println("Found " + junctions.size() + " junctions");

        // Extract vessel segments between junctions
        List<Segment> segments = extractSegments(skeleton, junctions);

        System.out.println("Extracted " + segments.size() + " segments");

        // Compute features for each segment
        for (Segment s : segments) {
            computeSegmentFeatures(s, gray, binary);
        }

        // Build graph: each segment = one vertex
        AdjacencyListGraph<Node, Double> graph = new AdjacencyListGraph<>();
        Map<Segment, Vertex<Node>> segmentToVertex = new HashMap<>();

        for (Segment s : segments) {
            Node node = new Node(s.id, 0, 0);
            node.setArea(s.area);
            node.setCircularity(s.circularity);
            node.setAspectRatio(s.aspectRatio);
            node.setTexture(s.textureContrast);

            Vertex<Node> v = graph.insertVertex(node);
            segmentToVertex.put(s, v);
        }

        // Connect segments that share a junction
        connectSegments(graph, segmentToVertex, segments);

        // Visualisation
        saveSegmentColourImage(skeleton, segments, "src/Datasets/output/segments_coloured.png");

        System.out.println("Graph built: " + graph.numVertices() + " vertices, " +
                graph.numEdges() + " edges");

        return graph;
    }

    /* ==========================================================
       IMAGE PREPROCESSING
       ========================================================== */

    /**
     * Converts image to grayscale using inverted green channel.
     * Inverted because vessels are dark in original, we want them bright.
     *
     * @param image source image
     * @return grayscale matrix (0-255, vessels bright)
     */
    private int[][] convertToGrayScale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] gray = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int g = (rgb >> 8) & 255;
                gray[x][y] = 255 - g; // Invert: dark vessels become bright
            }
        }
        return gray;
    }

    /**
     * Applies 3x3 Gaussian smoothing.
     *
     * @param img input image
     * @return smoothed image
     */
    private int[][] smoothImage(int[][] img) {
        int w = img.length;
        int h = img[0].length;
        int[][] out = new int[w][h];

        int[][] kernel = {{1, 2, 1}, {2, 4, 2}, {1, 2, 1}};

        for (int x = 1; x < w - 1; x++) {
            for (int y = 1; y < h - 1; y++) {
                int sum = 0;
                int weight = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        sum += img[x + i][y + j] * kernel[i + 1][j + 1];
                        weight += kernel[i + 1][j + 1];
                    }
                }
                out[x][y] = sum / weight;
            }
        }
        return out;
    }

    /**
     * Creates circular field-of-view mask based on intensity threshold.
     *
     * @param gray grayscale image
     * @return mask of valid retina pixels
     */
    private boolean[][] createFOVMask(int[][] gray) {
        int w = gray.length;
        int h = gray[0].length;
        boolean[][] mask = new boolean[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // Background is black (<5), retina is brighter
                if (gray[x][y] > 8) {
                    mask[x][y] = true;
                }
            }
        }
        return mask;
    }

    /**
     * Otsu threshold using only pixels inside retina mask.
     *
     * @param gray grayscale image
     * @param mask field mask
     * @return threshold
     */
    private int calculateThresholdMasked(int[][] gray, boolean[][] mask) {
        int[] hist = new int[256];
        int total = 0;
        int w = gray.length;
        int h = gray[0].length;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (mask[x][y]) {
                    hist[gray[x][y]]++;
                    total++;
                }
            }
        }

        if (total == 0) return 35; // fallback

        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * hist[i];
        }

        double sumB = 0;
        int wB = 0;
        double maxVar = -1;
        int threshold = 35;

        for (int t = 0; t < 256; t++) {
            wB += hist[t];
            if (wB == 0) continue;

            int wF = total - wB;
            if (wF == 0) break;

            sumB += (double) t * hist[t];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double var = (double) wB * wF * (mB - mF) * (mB - mF);

            if (var > maxVar) {
                maxVar = var;
                threshold = t;
            }
        }

        return Math.max(15, Math.min(80, threshold));
    }

    /**
     * Converts grayscale to binary image.
     *
     * @param gray grayscale image
     * @param threshold threshold value
     * @param mask retina mask
     * @return binary image
     */
    private int[][] convertToBinary(int[][] gray, int threshold, boolean[][] mask) {
        int w = gray.length;
        int h = gray[0].length;
        int[][] out = new int[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (mask[x][y] && gray[x][y] > threshold) {
                    out[x][y] = 1;
                }
            }
        }
        return out;
    }

    /**
     * Removes isolated noise using neighbour counting.
     *
     * @param img binary image
     * @return cleaned binary image
     */
    private int[][] removeNoise(int[][] img) {
        int w = img.length;
        int h = img[0].length;
        int[][] out = new int[w][h];

        for (int x = 1; x < w - 1; x++) {
            for (int y = 1; y < h - 1; y++) {
                int count = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (img[x + dx][y + dy] == 1) count++;
                    }
                }
                if (count >= 3) {
                    out[x][y] = 1;
                }
            }
        }
        return out;
    }

    /**
     * Skeletonizes binary vessel image.
     *
     * @param binary binary image
     * @return skeleton image
     */
    private int[][] skeletonize(int[][] binary) {
        return new Skeletonization(binary).getSkeleton();
    }

    /* ==========================================================
       GRAPH EXTRACTION
       ========================================================== */

    private Map<String, Junction> findJunctions(int[][] skel) {
        Map<String, Junction> map = new HashMap<>();
        int w = skel.length;
        int h = skel[0].length;

        for (int x = 1; x < w - 1; x++) {
            for (int y = 1; y < h - 1; y++) {
                if (skel[x][y] != 1) continue;

                int[] n = {
                        skel[x][y - 1], skel[x + 1][y - 1], skel[x + 1][y],
                        skel[x + 1][y + 1], skel[x][y + 1], skel[x - 1][y + 1],
                        skel[x - 1][y], skel[x - 1][y - 1]
                };

                int transitions = 0;
                int neighbors = 0;

                for (int i = 0; i < 8; i++) {
                    if (n[i] == 1) neighbors++;
                    if (n[i] == 0 && n[(i + 1) % 8] == 1) transitions++;
                }

                // Endpoint (degree 1)
                if (transitions == 1 && neighbors == 1) {
                    map.put(x + "," + y, new Junction(x, y, 1));
                }
                // Bifurcation (degree 3)
                else if (transitions == 3 && neighbors == 3) {
                    map.put(x + "," + y, new Junction(x, y, 3));
                }
                // Higher order (>=4 bifurcation)
                else if (transitions >= 4 && neighbors >= 4) {
                    map.put(x + "," + y, new Junction(x, y, neighbors));
                }
            }
        }
        return map;
    }

    private List<Segment> extractSegments(int[][] skel, Map<String, Junction> junctions) {
        boolean[][] visited = new boolean[skel.length][skel[0].length];
        List<Segment> list = new ArrayList<>();

        for (Junction j : junctions.values()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int nx = j.x + dx;
                    int ny = j.y + dy;

                    if (inside(skel, nx, ny) && skel[nx][ny] == 1 && !visited[nx][ny]) {
                        Segment s = traceSegment(skel, nx, ny, j, junctions, visited);
                        if (!s.pixels.isEmpty()) {
                            list.add(s);
                        }
                    }
                }
            }
        }
        return list;
    }

    private Segment traceSegment(int[][] skel, int x, int y, Junction start,
                                 Map<String, Junction> junctions, boolean[][] visited) {
        Segment seg = new Segment();
        seg.junctionA = start;

        int cx = x, cy = y;

        while (true) {
            visited[cx][cy] = true;
            seg.addPixel(cx, cy);

            String key = cx + "," + cy;
            if (junctions.containsKey(key)) {
                seg.junctionB = junctions.get(key);
                break;
            }

            int[] next = nextPixel(skel, cx, cy, visited);
            if (next == null) break;

            cx = next[0];
            cy = next[1];
        }
        return seg;
    }

    private int[] nextPixel(int[][] skel, int x, int y, boolean[][] visited) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (inside(skel, nx, ny) && skel[nx][ny] == 1 && !visited[nx][ny]) {
                    return new int[]{nx, ny};
                }
            }
        }
        return null;
    }

    private boolean inside(int[][] img, int x, int y) {
        return x >= 0 && y >= 0 && x < img.length && y < img[0].length;
    }

    private void connectSegments(AdjacencyListGraph<Node, Double> graph,
                                 Map<Segment, Vertex<Node>> map, List<Segment> segments) {
        for (int i = 0; i < segments.size(); i++) {
            for (int j = i + 1; j < segments.size(); j++) {
                if (shareJunction(segments.get(i), segments.get(j))) {
                    Vertex<Node> u = map.get(segments.get(i));
                    Vertex<Node> v = map.get(segments.get(j));
                    if (!graph.areaAdjacent(u, v)) {
                        graph.insertEdge(u, v, 1.0);
                    }
                }
            }
        }
    }

    private boolean shareJunction(Segment a, Segment b) {
        return a.junctionA == b.junctionA || a.junctionA == b.junctionB ||
                a.junctionB == b.junctionA || a.junctionB == b.junctionB;
    }

    /* ==========================================================
       FEATURES (Improved from placeholder)
       ========================================================== */

    private void computeSegmentFeatures(Segment seg, int[][] gray, int[][] binary) {
        // Area = number of skeleton pixels in segment (approximation)
        seg.area = seg.pixels.size();

        // Length = number of skeleton pixels
        double length = seg.pixels.size();

        // Compute average width from binary mask
        double totalWidth = 0;
        int widthSamples = 0;

        for (int i = 0; i < seg.pixels.size(); i += Math.max(1, seg.pixels.size() / 10)) {
            int[] p = seg.pixels.get(i);
            int radius = 1;
            while (radius < 15) {
                boolean atEdge = false;
                // Check horizontal
                if (p[0] + radius < binary.length && binary[p[0] + radius][p[1]] == 0) atEdge = true;
                if (p[0] - radius >= 0 && binary[p[0] - radius][p[1]] == 0) atEdge = true;
                // Check vertical
                if (p[1] + radius < binary[0].length && binary[p[0]][p[1] + radius] == 0) atEdge = true;
                if (p[1] - radius >= 0 && binary[p[0]][p[1] - radius] == 0) atEdge = true;

                if (atEdge) break;
                radius++;
            }
            totalWidth += radius * 2;
            widthSamples++;
        }

        double avgWidth = widthSamples > 0 ? totalWidth / widthSamples : 2.0;
        seg.aspectRatio = length / avgWidth;

        // Circularity: area / (length^2) - lower means more elongated
        seg.circularity = seg.area / (length * length + 0.000001);

        // Texture: mean intensity and contrast from grayscale image
        double sumIntensity = 0;
        int pixelCount = 0;

        for (int[] p : seg.pixels) {
            sumIntensity += gray[p[0]][p[1]];
            pixelCount++;
        }

        double meanIntensity = pixelCount > 0 ? sumIntensity / pixelCount : 0;

        // Simple contrast = standard deviation of intensities
        double sumSq = 0;
        for (int[] p : seg.pixels) {
            double diff = gray[p[0]][p[1]] - meanIntensity;
            sumSq += diff * diff;
        }
        double contrast = pixelCount > 0 ? Math.sqrt(sumSq / pixelCount) : 0;
        seg.textureContrast = contrast;
    }

    /* ==========================================================
       OUTPUT
       ========================================================== */

    private void saveBinaryImage(int[][] img, String path) {
        try {
            int w = img.length;
            int h = img[0].length;
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int v = img[x][y] == 1 ? 255 : 0;
                    out.setRGB(x, y, (v << 16) | (v << 8) | v);
                }
            }
            ImageIO.write(out, "png", new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveSegmentColourImage(int[][] skel, List<Segment> segments, String path) {
        try {
            int w = skel.length;
            int h = skel[0].length;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Random rand = new Random(42);

            for (Segment s : segments) {
                int color = (rand.nextInt(256) << 16) | (rand.nextInt(256) << 8) | rand.nextInt(256);
                for (int[] p : s.pixels) {
                    img.setRGB(p[0], p[1], color);
                }
            }
            ImageIO.write(img, "png", new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}