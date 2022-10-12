package model;

import java.util.ArrayList;
import java.util.List;

public class RTreeNode<T> extends model.Node<List<T>>{
    // data members
    private String id;
    public RTreeNode<T>[] neighbours;
    private Pair<Double, Double>[] ranges;
    private boolean leaf;
    public boolean isLeaf() {return leaf;}

    // Ctors
    public RTreeNode(List<T> item, Pair<Double, Double>[] ranges) {
        this(item, ranges, false, null);
    }

    public RTreeNode(List<T> item, Pair<Double, Double>[] ranges, RTreeNode<T>[] neighbours) {
        this(item, ranges, false, neighbours[2]);
        this.neighbours[0] = neighbours[0];
        this.neighbours[1] = neighbours[1];
    }

    public RTreeNode(List<T> item, Pair<Double, Double>[] ranges, boolean leaf, RTreeNode<T> parent) {
        super(item);
        neighbours = new RTreeNode[3];
        neighbours[2] = parent;
        this.leaf = leaf;
        this.ranges = ranges;
    }

    public Pair<Double, Double>[] getRanges() {
        return ranges;
    }

    public void addEntry(T entry) {
        this.item.add(entry);
    }

    static public boolean isOverlap( Pair<Double, Double>[] r1, Pair<Double, Double>[] r2 ) {
        if (r1.length != r2.length) throw new IllegalArgumentException("输入的顶点的范围数不一样");

        for (int i = 0; i < r1.length; i++) {
            if (r1[i].getFirst() > r2[i].getSecond() || r2[i].getFirst() > r1[i].getSecond())
                return false;
        }

        return true;
    }
}

