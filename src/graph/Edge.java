package graph;

public class Edge<E>{

    private E element;
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
}
