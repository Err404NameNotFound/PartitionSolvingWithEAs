package help;

public class RingBufferFifo<T> {

    private final int capacity;
    private int size;
    private Node<T> first;
    private Node<T> last;

    public RingBufferFifo(int size, T initialValue) {
        this.size = 1;
        capacity = size;
        first = new Node<>(initialValue);
        last = first;
    }

    public void insert(T value) {
        Node<T> next = new Node<>(value);
        first.next = next;
        first = next;
        if (size >= capacity) {
            last = last.next;
        } else {
            ++size;
        }
    }

    public T peek() {
        return last.value;
    }

    public int getSize() {
        return size;
    }

    private static class Node<T> {
        public T value;
        public Node<T> next;

        public Node(T value) {
            this.value = value;
        }
    }
}
