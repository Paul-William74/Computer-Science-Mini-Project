package graph;

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
}
