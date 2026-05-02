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
 * REFINED FINAL VERSION
 *<p>
 * Real retinal vessel graph only:
 * - vertices = endpoints / bifurcations
 * - edges = traced vessel branches
 *<p>
 * Improvements:
 * - stronger node merging
 * - suppress fake micro nodes
 * - cleaner branch detection
 * - reduced vertex explosion
 */
public class ImageProcessor {

    /* =======================================================
       PUBLIC ENTRY
       ======================================================= */

    public AdjacencyListGraph<Node, Double> buildNodeGraph(File imageFile)
            throws IOException {

        BufferedImage image = ImageIO.read(imageFile);

        int[][] gray = convertToGrayScale(image);

        int[][] smooth = smooth(gray);

        boolean[][] mask = createCircularMask(gray);

        int threshold = computeThreshold(smooth, mask);

        System.out.println("Threshold = " + threshold);

        int[][] binary = toBinary(smooth, threshold, mask);

        binary = removeNoise(binary);

        saveBinary(binary, "src/Datasets/output/binary.png");

        int[][] skeleton = skeletonize(binary);

        saveBinary(skeleton, "src/Datasets/output/skeleton.png");

        Map<String, Junction> raw = detectJunctions(skeleton);

        System.out.println("Detected raw nodes = " + raw.size());

        List<Node> nodes = clusterJunctions(raw, skeleton);

        System.out.println("Merged nodes = " + nodes.size());

        AdjacencyListGraph<Node, Double> graph =
                new AdjacencyListGraph<>();

        Map<Node, Vertex<Node>> map = new HashMap<>();

        for (Node n : nodes) {
            Vertex<Node> v = graph.insertVertex(n);
            map.put(n, v);
        }

        traceEdges(graph, map, nodes, skeleton);

        removeIsolated(graph);

        System.out.println("Graph built.");

        return graph;
    }

    /* =======================================================
       IMAGE PREPROCESSING
       ======================================================= */

    private int[][] convertToGrayScale(BufferedImage image) {

        int w = image.getWidth();
        int h = image.getHeight();

        int[][] out = new int[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {

                int rgb = image.getRGB(x, y);
                int g = (rgb >> 8) & 255;

                out[x][y] = 255 - g; // vessels bright
            }
        }

        return out;
    }

    private int[][] smooth(int[][] img) {

        int w = img.length;
        int h = img[0].length;

        int[][] out = new int[w][h];

        for (int x = 1; x < w - 1; x++) {
            for (int y = 1; y < h - 1; y++) {

                int sum = 0;

                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        sum += img[x + dx][y + dy];

                out[x][y] = sum / 9;
            }
        }

        return out;
    }

    private boolean[][] createCircularMask(int[][] img) {

        int w = img.length;
        int h = img[0].length;

        boolean[][] mask = new boolean[w][h];

        int cx = w / 2;
        int cy = h / 2;

        int radius = Math.min(w, h) / 2 - 20;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {

                int dx = x - cx;
                int dy = y - cy;

                if (dx * dx + dy * dy < radius * radius)
                    mask[x][y] = true;
            }
        }

        return mask;
    }

    private int computeThreshold(int[][] img, boolean[][] mask) {

        long sum = 0;
        long count = 0;

        for (int x = 0; x < img.length; x++) {
            for (int y = 0; y < img[0].length; y++) {

                if (mask[x][y]) {
                    sum += img[x][y];
                    count++;
                }
            }
        }

        return (int) (sum / count) + 18;
    }

    private int[][] toBinary(int[][] img,
                             int threshold,
                             boolean[][] mask) {

        int w = img.length;
        int h = img[0].length;

        int[][] out = new int[w][h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {

                if (mask[x][y] && img[x][y] > threshold)
                    out[x][y] = 1;
            }
        }

        return out;
    }

    private int[][] removeNoise(int[][] img) {

        int w = img.length;
        int h = img[0].length;

        int[][] out = new int[w][h];

        for (int x = 1; x < w - 1; x++) {
            for (int y = 1; y < h - 1; y++) {

                int count = 0;

                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        count += img[x + dx][y + dy];

                if (count >= 5)
                    out[x][y] = 1;
            }
        }

        return out;
    }

    private int[][] skeletonize(int[][] binary) {
        return new Skeletonization(binary).getSkeleton();
    }

    /* =======================================================
       NODE DETECTION
       ======================================================= */

    private Map<String, Junction> detectJunctions(int[][] skel) {

        Map<String, Junction> map = new HashMap<>();

        for (int x = 1; x < skel.length - 1; x++) {
            for (int y = 1; y < skel[0].length - 1; y++) {

                if (skel[x][y] != 1)
                    continue;

                int n = neighbourCount(skel, x, y);

                if (n == 1 || n >= 3) {
                    map.put(x + "," + y,
                            new Junction(x, y, n));
                }
            }
        }

        return map;
    }

    private int neighbourCount(int[][] img, int x, int y) {

        int c = 0;

        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (!(dx == 0 && dy == 0))
                    c += img[x + dx][y + dy];

        return c;
    }

    /* =======================================================
       STRONG NODE MERGING
       ======================================================= */

    private List<Node> clusterJunctions(Map<String, Junction> raw,
                                        int[][] skel) {

        boolean[][] visited =
                new boolean[skel.length][skel[0].length];

        List<Node> nodes = new ArrayList<>();

        for (Junction j : raw.values()) {

            if (visited[j.x][j.y])
                continue;

            Queue<int[]> q = new LinkedList<>();
            List<int[]> cluster = new ArrayList<>();

            q.add(new int[]{j.x, j.y});
            visited[j.x][j.y] = true;

            while (!q.isEmpty()) {

                int[] p = q.poll();
                cluster.add(p);

                for (int dx = -4; dx <= 4; dx++) {
                    for (int dy = -4; dy <= 4; dy++) {

                        int nx = p[0] + dx;
                        int ny = p[1] + dy;

                        if (inside(skel, nx, ny))
                            continue;

                        if (visited[nx][ny])
                            continue;

                        if (raw.containsKey(nx + "," + ny)) {
                            visited[nx][ny] = true;
                            q.add(new int[]{nx, ny});
                        }
                    }
                }
            }

            if (cluster.size() < 2)
                continue;

            int sx = 0;
            int sy = 0;

            for (int[] p : cluster) {
                sx += p[0];
                sy += p[1];
            }

            int cx = sx / cluster.size();
            int cy = sy / cluster.size();

            Node n = new Node(cx, cy);

            nodes.add(n);
        }

        return nodes;
    }

    /* =======================================================
       EDGE TRACING
       ======================================================= */

    private void traceEdges(
            AdjacencyListGraph<Node, Double> graph,
            Map<Node, Vertex<Node>> map,
            List<Node> nodes,
            int[][] skel) {

        for (Node start : nodes) {

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {

                    if (dx == 0 && dy == 0)
                        continue;

                    int nx = start.getX() + dx;
                    int ny = start.getY() + dy;

                    if (inside(skel, nx, ny))
                        continue;

                    if (skel[nx][ny] != 1)
                        continue;

                    follow(graph, map, nodes,
                            start, nx, ny, skel);
                }
            }
        }
    }

    private void follow(
            AdjacencyListGraph<Node, Double> graph,
            Map<Node, Vertex<Node>> map,
            List<Node> nodes,
            Node start,
            int x,
            int y,
            int[][] skel) {

        int px = start.getX();
        int py = start.getY();

        int cx = x;
        int cy = y;

        int length = 1;

        while (true) {

            Node found = findNode(nodes, cx, cy, start);

            if (found != null) {

                Vertex<Node> u = map.get(start);
                Vertex<Node> v = map.get(found);

                if (!graph.areaAdjacent(u, v)) {
                    graph.insertEdge(u, v, (double) length);
                }

                return;
            }

            int[] next = nextPixel(skel, px, py, cx, cy);

            if (next == null)
                return;

            px = cx;
            py = cy;

            cx = next[0];
            cy = next[1];

            length++;

            if (length > 1500)
                return;
        }
    }

    private int[] nextPixel(
            int[][] skel,
            int px, int py,
            int cx, int cy) {

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                if (dx == 0 && dy == 0)
                    continue;

                int nx = cx + dx;
                int ny = cy + dy;

                if (inside(skel, nx, ny))
                    continue;

                if (nx == px && ny == py)
                    continue;

                if (skel[nx][ny] == 1)
                    return new int[]{nx, ny};
            }
        }

        return null;
    }

    private Node findNode(List<Node> nodes,
                          int x, int y,
                          Node ignore) {

        for (Node n : nodes) {

            if (n == ignore)
                continue;

            if (Math.abs(n.getX() - x) <= 2 &&
                    Math.abs(n.getY() - y) <= 2)
                return n;
        }

        return null;
    }

    /* =======================================================
       CLEANUP
       ======================================================= */

    private void removeIsolated(
            AdjacencyListGraph<Node, Double> graph) {

        List<Vertex<Node>> remove = new ArrayList<>();

        for (Vertex<Node> v : graph.vertices()) {
            if (graph.degree(v) == 0)
                remove.add(v);
        }

        for (Vertex<Node> v : remove)
            graph.removeVertex(v);
    }

    /* =======================================================
       HELPERS
       ======================================================= */

    private boolean inside(int[][] img, int x, int y) {

        return x < 0 ||
                y < 0 ||
                x >= img.length ||
                y >= img[0].length;
    }

    private void saveBinary(int[][] img, String path) {

        try {

            int w = img.length;
            int h = img[0].length;

            BufferedImage out =
                    new BufferedImage(w, h,
                            BufferedImage.TYPE_BYTE_BINARY);

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {

                    int v = img[x][y] == 1 ? 255 : 0;

                    out.setRGB(x, y,
                            (v << 16) | (v << 8) | v);
                }
            }

            ImageIO.write(out, "png", new File(path));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}