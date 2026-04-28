package graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node represents a segmented region in the graph with each node
 * corresponding to a meaningful object(vessel segment, lesion)
 */
public class Node {
    private final int id;

    //Variables that represent position
    private final int x;
    private final int y;

    // variables that represent the features of a node for analysis (filled by Paul)
    private double texture;
    private double area;
    private double circularity;
    private double aspectRatio;

    private int value; //1 = vessel, 0 = background

    public List<int[]> pixels = new ArrayList<>();


    private static int nextId=0;

    public Node(int x, int y) {
        this.id=nextId++;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getTexture() {return texture;}
    public double getArea() { return area;}
    public double getCircularity() { return circularity;}
    public double getAspectRatio() { return aspectRatio;}
    public int getValue() {
        return value;
    }

    public void setArea(double area) {
        this.area = area;
    }
    public void setCircularity(double circularity) {
        this.circularity = circularity;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void setTexture(double texture) {
        this.texture = texture;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * equals helps compare one Node with another to determine equality.
     * In the context of a node-based graph, two Node objects are considered equal
     * if they represent the same region in the graph (i.e., they have the same
     * id)
     * This method is critical for ensuring that sets, maps, and other data structures
     * do not store duplicate nodes for the same node.
     *
     * @param o   the reference object with which to compare.
     * @return true or false - true if the object is a Node and has the same id
     *                       as 'this' Node or false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node other = (Node) o;
        return id == other.id;
        //x  == other.x && y == other.y;
    }

    /**
     * hashCode works out a hash code for this Node based on its id.
     * Hash codes are used by hash-based data structures (e.g., HashSet, HashMap)
     * to quickly determine uniqueness. By hashing only the x and y coordinates,
     * two Node objects representing the same pixel will have the same hash code,
     * ensuring correct behavior in collections and preventing duplicate nodes
     * for the same pixel.
     *
     * @return hash code - an integer data type hash code calculated from the Node id
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node" + id + " (" + x + ", " + y + ")";
    }
}
