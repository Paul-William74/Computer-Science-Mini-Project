package imageprocessing;

/**
 * Represents a bifurcation or endpoint on the skeleton.
 */
public class Junction {
    public final int x;
    public final int y;
    public final int degree;    // number of incident skeleton branches

    public Junction(int x, int y, int degree) {
        this.x = x;
        this.y = y;
        this.degree = degree;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Junction other)) return false;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "Junction(" + x + "," + y + ", deg=" + degree + ")";
    }
}