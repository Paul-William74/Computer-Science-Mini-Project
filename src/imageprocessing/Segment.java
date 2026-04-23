package imageprocessing;

import java.util.ArrayList;
import java.util.List;

/**
 * A vessel segment: a maximal path between two junctions (or between a junction and an endpoint).
 */
public class Segment {
    public final int id;
    public List<int[]> pixels = new ArrayList<>();   // list of (x,y) coordinates on the skeleton
    public Junction junctionA;   // may be null for open end at image border
    public Junction junctionB;

    // Computed features (to be filled later)
    public double area = 0.0;
    public double circularity = 0.0;
    public double aspectRatio = 0.0;
    public double textureMean = 0.0;
    public double textureContrast = 0.0;

    private static int nextId = 0;

    public Segment() {
        this.id = nextId++;
    }

    public void addPixel(int x, int y) {
        pixels.add(new int[]{x, y});
    }

    public int size() {
        return pixels.size();
    }

    @Override
    public String toString() {
        return "Segment " + id + " [" + pixels.size() + " pixels]";
    }
}