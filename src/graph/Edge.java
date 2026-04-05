package graph;

import java.util.Objects;

/**
 * Edge class encapsulates a Edge connecting two vertices
 */
public class Edge<E>{

    private E element; //represents optional data like weight or label
    private Vertex<?> u;
    private Vertex<?> v;

    public Edge(Vertex<?> u, Vertex<?> v,E element) {
        this.u = u;
        this.v = v;
        this.element = element;
    }

    public E getElement() {
        return element;
    }

    public Vertex<?> getU() {
        return u;
    }

    public Vertex<?> getV() {
        return v;
    }

    /**
     * Checks if this Edge is equal to another object.
     *
     * Purpose:
     * This method is necessary to correctly compare Edge objects, especially
     * when used in collections like Sets or as keys in Maps. By default, Object's
     * equals() checks reference equality, but in a graph we want two Edge objects
     * to be considered equal if they connect the same pair of vertices with the same element,
     * regardless of the order of the vertices for undirected graphs.
     *
     * How it works:
     * 1. Checks if the object is the same instance (reference equality).
     * 2. Checks if the object is null or of a different class.
     * 3. For undirected graphs, it considers two edges equal if:
     *      - u and v match exactly, or
     *      - u and v are swapped (since order doesn’t matter)
     * 4. Also checks if the optional element (e.g., weight or label) is equal.
     *
     * @param obj - the object to compare with this Edge
     * @return true - true if the other object is an Edge connecting the same vertices
     *         (in either order) with the same element, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge<?> edge = (Edge<?>) obj;
        // For undirected graph, check both directions
        return (u.equals(edge.u) && v.equals(edge.v) || u.equals(edge.v) && v.equals(edge.u))
                && (Objects.equals(element, edge.element));
    }

    /**
     * Returns a hash code value for this Edge.
     *
     * Purpose:
     * This method is necessary for using Edge objects in hash-based collections
     * like HashMap or HashSet. The hash code must be consistent with equals(), so
     * that two equal edges produce the same hash code.
     *
     * How it works:
     * 1. For undirected edges, the order of vertices does not matter, so the vertex
     *    hash is calculated as u.hashCode() + v.hashCode().
     * 2. The optional element’s hash code is combined using a multiplier (31)
     *    to reduce collisions.
     * 3. This ensures that edges connecting the same vertices (regardless of order)
     *    with the same element have identical hash codes.
     *
     * @return the hash code of this edge
     */
    @Override
    public int hashCode() {
        // For undirected edge, order of vertices doesn’t matter
        int vertexHash = u.hashCode() + v.hashCode();
        return 31 * vertexHash + (element != null ? element.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "(" + u.getElement() + " -- " + v.getElement() + ", " + element + ")";
    }
}
