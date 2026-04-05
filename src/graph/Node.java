package graph;

import java.util.Objects;

/**
 * Node represents a pixel in the image graph.
 * Stores spatial and classification information.
 */
public class Node {
    private int id;
    private int x;
    private int y;
    private int value; //1 = vessel, 0 = background

    public Node(int id, int x, int y, int value) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.value = value;
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

    public int getValue() {
        return value;
    }

    /**
     * equals helps compare one Node with another to determine equality.
     *
     *
     * In the context of a pixel-based graph, two Node objects are considered equal
     * if they represent the same pixel location in the grid (i.e., they have the same
     * x and y coordinates), regardless of other attributes such as value or ID.
     *
     * This method is critical for ensuring that sets, maps, and other data structures
     * do not store duplicate nodes for the same pixel.
     *
     * @param o   the reference object with which to compare.
     * @return true or false - true if the object is a Node and has the same x and y coordinates
     *                       as 'this' Node or false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node other = (Node) o;
        return x  == other.x && y == other.y;
    }

    /**
     * hashCode works out a hash code for this Node based on its pixel coordinates.
     *
     * Hash codes are used by hash-based data structures (e.g., HashSet, HashMap)
     * to quickly determine uniqueness. By hashing only the x and y coordinates,
     * two Node objects representing the same pixel will have the same hash code,
     * ensuring correct behavior in collections and preventing duplicate nodes
     * for the same pixel.
     *
     * @return hash code - an integer data type hash code calculated from the x and y coordinates
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
