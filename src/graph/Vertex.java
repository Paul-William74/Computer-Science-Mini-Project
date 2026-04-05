package graph;

/**
 * Vertex class encapsulates and defines a Vertex
 * @param <V> - the data stored at the Vertex
 */
public class Vertex<V> {

    private V element;

    public Vertex(V element) {
        this.element = element;
    }

    public V getElement() {
        return element;
    }
    public void setElement(V element) {
        this.element = element;
    }

    /**
     *
     * equals checks if this Vertex is equal to another object.
     * It is Necessary for using Vertex as keys in Maps or elements in Sets
     *
     * Purpose:
     * This method is necessary to correctly compare Vertex objects,
     * especially when they are used as keys in Maps or elements in Sets.
     * By default, the Object class's equals() checks for reference equality,
     * but in a graph we usually want two Vertex objects to be considered equal
     * if they store the same data element, even if they are different objects in memory.
     *
     * How it works:
     * 1. First, it checks if the object is the same instance (reference equality).
     * 2. Then, it checks if the object is null or of a different class.
     * 3. Finally, it compares the stored element for equality using its own equals() method.
     *
     *  @param obj -the object to compare with this Vertex
     *  return true - true if the other object is a Vertex with the same element, false otherwise
     *
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vertex<?> vertex = (Vertex<?>) obj;
        return element.equals(vertex.element);
    }

    /**
     * Returns a hash code value for this Vertex.
     *
     * Purpose:
     * This method is necessary to use Vertex objects as keys in hash-based collections,
     * such as HashMap or HashSet. The hash code determines which "bucket" the object
     * belongs to. For hash-based collections to work correctly, objects that are
     * equal according to equals() must have the same hash code.
     *
     * How it works:
     * It delegates to the hashCode() method of the stored element, ensuring
     * consistency with equals(): if two Vertex objects store equal elements,
     * they will produce the same hash code.
     *
     * @return hash code - the hash code(or bucket) the stored element belongs to
     */
    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public String toString() {
        return element.toString();
    }
}
