package model;

import java.util.List;

public class RTreeNode<T extends RTreeEntry> extends model.Node<List<T>>{
    // data members
    private String id;
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
        neighbours = new RTreeNode[2];
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

    static public double getArea(RTreeNode node) {
        double area = 1.0f;
        for (Pair<Double, Double> range : node.ranges) area *= (range.getSecond() - range.getFirst());
        return area;
    }

    void addChild(RTreeNode node) {

    }

    void removeChild(RTreeNode node) {

    }

    /**
     * Returns increase in area to include given element
     * @param e - The element that may be inserted
     */
    double getAreaExpansion(T e) {
        double area = getArea(this); // Original Area
        double expanded = 1.0f;           // New area

        double[] e_vals = e.getParamValues(); // Get the parametrized values

        if (e_vals.length != ranges.length) throw new IllegalArgumentException("e的参数数不对");

        double[] deltas = new double[this.getRanges().length];

        for ( int i = 0; i < e_vals.length; i++ ) {
            // Expand end point
            if (this.ranges[i].getSecond() < e_vals[i])
                area *= e_vals[i] - this.getRanges()[i].getFirst();

            // Expand start point
            if (this.ranges[i].getFirst() > e_vals[i])
                area *= this.ranges[i].getSecond() - e_vals[i];
        }

        return expanded - area;
    }
}

