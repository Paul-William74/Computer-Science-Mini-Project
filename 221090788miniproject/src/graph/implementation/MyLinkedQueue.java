package graph.implementation;

import graph.adt.*;

public class MyLinkedQueue<T> implements MyQueue<T> {

    private class Node {
        T value;
        Node next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node front;
    private Node rear;
    private int size = 0;

    @Override
    public void enqueue(T item) {
        Node n = new Node(item);

        if (rear == null) {
            front = rear = n;
        } else {
            rear.next = n;
            rear = n;
        }

        size++;
    }

    @Override
    public T dequeue() {
        if (front == null) return null;

        T val = front.value;
        front = front.next;

        if (front == null) rear = null;

        size--;
        return val;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }
}
