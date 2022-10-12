
public class RTreeNode<T> extends AVLs.Node<T[]>{
    // data members
    private String id;

    private boolean leaf;
    public boolean isLeaf() {return leaf;}

    // Ctor
    public RTreeNode(T[] item) {
        super(item);
    }
}


