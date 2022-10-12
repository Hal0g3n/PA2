package model;

public class RTreeNode<T> extends model.Node<T[]>{
    // data members
    private String id;

    private int dims;
    private Pair<Double, Double>[] ranges;
    private boolean leaf;
    public boolean isLeaf() {return leaf;}

    // Ctors
    public RTreeNode(T[] item, Pair<Double, Double>[] ranges) {
        this(item, ranges, false);
    }

    public RTreeNode(T[] item, Pair<Double, Double>[] ranges, RTreeNode<T>[] neighbours) {
        this(item, ranges, false);
        this.neighbours[0] = neighbours[0];
        this.neighbours[1] = neighbours[1];
    }

    public RTreeNode(T[] item, Pair<Double, Double>[] ranges, boolean leaf) {
        super(item);
        neighbours = new RTreeNode[2];
        this.leaf = leaf;
        this.ranges = ranges;
    }

    static public boolean isOverlap( RTreeNode n1, RTreeNode n2 ) {
        if (n1.ranges.length != n2.ranges.length) throw new IllegalArgumentException("输入的顶点的范围数不一样");

        for (int i = 0; i < n1.ranges.length; i++) {
            if ((double) n1.ranges[i].getFirst() > (double) n2.ranges[i].getSecond() || (double) n2.ranges[i].getFirst() > (double) n1.ranges[i].getSecond())
                return false;
        }

        return true;
    }
}


