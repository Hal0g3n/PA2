package AVLs;

public class Node<T> {
    private T item;
    Node<T>[] neighbours; //Package accessibility

    // Constructor to create AVLs.Node containing item of generic class T
    public Node(T item){ this.item = item; }

    // Constructor to create AVLs.Node containing item of generic class T,
    // with numEdges number of edges (but child nodes unknown)
    public Node(T item, int numNeighbours){
        this(item);
        this.neighbours = new Node[numNeighbours];
    }
    // Constructor to create AVLs.Node containing item of generic class T,
    // with child nodes defined in the neighbours AVLs.Node array
    public Node(T item, Node<T>[] neighbours){
        this(item);
        this.neighbours = new Node[neighbours.length];
        for (int i=0; i<neighbours.length; i++)
            this.neighbours[i] = new Node(neighbours[i]);
    }

    // Copy constructor to copy the AVLs.Node object n
    public Node(Node<T> n){ this(n.item, n.neighbours); }

    // Accessor method returns item stored in AVLs.Node
    public T getItem() { return item;}

    // Mutator method sets the item to be stored in AVLs.Node
    public void setItem(T item) { this.item = item;}

}
