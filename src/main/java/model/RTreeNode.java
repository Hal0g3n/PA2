package model;

import java.util.ArrayList;
import java.util.List;

public class RTreeNode<T> extends model.Node<List<T>>{
    // data members
    private String id;

    private int dims;
    private Pair<Double, Double>[] ranges;
    private boolean leaf;
    public boolean isLeaf() {return leaf;}

    // Ctors
    public RTreeNode(List<T> item, Pair<Double, Double>[] ranges) {
        this(item, ranges, false);
    }

    public RTreeNode(List<T> item, Pair<Double, Double>[] ranges, RTreeNode<T>[] neighbours) {
        this(item, ranges, false);
        this.neighbours[0] = neighbours[0];
        this.neighbours[1] = neighbours[1];
    }

    public RTreeNode(List<T> item, Pair<Double, Double>[] ranges, boolean leaf) {
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

class Entry extends Node {
    final T entry;

    public Entry(double[] coords, double[] dimensions, T entry) {
        // an entry isn't actually a leaf (its parent is a leaf)
        // but all the algorithms should stop at the first leaf they encounter,
        // so this little hack shouldn't be a problem.
        super(coords, dimensions, true);
        this.entry = entry;
    }
}

