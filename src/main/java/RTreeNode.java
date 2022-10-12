import kotlin.Pair;

public class RTreeNode<T> extends model.Node<T[]>{
    // data members
    private String id;

    private int dims;
    private Pair<Double, Double>[] ranges;
    private boolean leaf;
    public boolean isLeaf() {return leaf;}

    // Ctors
    public RTreeNode(T[] item) {
        this(item, false);
    }

    public RTreeNode(T[] item, RTreeNode<T>[] neighbours) {
        this(item, false);
        this.neighbours[0] = neighbours[0];
        this.neighbours[1] = neighbours[1];
    }

    public RTreeNode(T[] item, boolean leaf) {
        super(item);
        neighbours = new RTreeNode[2];
        this.leaf = leaf;
    }
}


