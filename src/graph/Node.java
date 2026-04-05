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
     * Method to help consider nodes equal if they represent the same pixel location
     *
     * @param o   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node other = (Node) o;
        return x  == other.x && y == other.y;
    }

    /**
     * Method to hash based on coordinates to ensures uniqueness per pixel
     * @return
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
