package model;

import AVLs.ElementNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RTreeNode<T extends RTreeEntry> extends model.Node<List<T>>{
    // data members
    private long id;
    public Range<Double>[] ranges;

    private long numChildren;
    private boolean leaf;
    public boolean isLeaf() {return leaf;}
    public void setLeaf(boolean l) {leaf = l;}

    // Ctors
    public RTreeNode(List<T> item, Range<Double>[] ranges) {
        this(item, ranges, true, null);
    }

    public RTreeNode(List<T> item, Range<Double>[] ranges, RTreeNode<T>[] neighbours) {
        this(item, ranges, false, neighbours[2]);
        this.neighbours[0] = neighbours[0];
        this.neighbours[1] = neighbours[1];
    }

    public RTreeNode(List<T> item, Range<Double>[] ranges, boolean leaf, RTreeNode<T> parent) {
        super(item);
        neighbours = new RTreeNode[4];
        neighbours[3] = parent;
        this.leaf = leaf;
        this.ranges = ranges;
    }

    public Range<Double>[] getRanges() {
        return ranges;
    }

    public long getId() { return this.id;}

    public void addEntry(T entry) {
        this.item.add(entry);
    }

    static public boolean isOverlap(Range<Double>[] r1, Range<Double>[] r2 ) {
        if (r1.length != r2.length) throw new IllegalArgumentException("输入的顶点的范围数不一样");

        for (int i = 0; i < r1.length; i++) {
            if (r1[i].getMin() > r2[i].getMax() || r2[i].getMin() > r1[i].getMax())
                return false;
        }

        return true;
    }

    static public double getArea(RTreeNode node) {
        double area = 1.0f;
        for (Range<Double> range : node.ranges) area *= (range.getMax() - range.getMin());
        return area;
    }

    public long getNumChildren() {
        return numChildren;
    }

    void addChild(RTreeNode<T> node) {
        for (int i = 0; i < 3; ++i) {
            if (neighbours[i] == null) {
                neighbours[i] = node;
                ++numChildren;
                return;
            }
        }
        throw new IllegalStateException("Too many children");
    }

    void removeChild(RTreeNode node) {
        for (int i = 0; i < 3; ++i) {
            if (neighbours[i].equals(node)) {
                neighbours[i] = null;
                --numChildren;
                return;
            }
        }
        throw new ElementNotFoundException("No such child");
    }

    /**
     * Recomputes the dependent values
     */
    void tighten() {
        Range<Double>[] n_ranges = Arrays.copyOf(ranges, ranges.length);

        if (this.isLeaf()) {
            // Get all parameter values of entries stored
            List<double[]> elements = item.stream().map(RTreeEntry::getParamValues).collect(Collectors.toList());

            for (int i = 0; i < ranges.length; i++) { // For each dimension
                n_ranges[i].setMin(Double.MAX_VALUE);
                n_ranges[i].setMax(Double.MIN_VALUE);

                for (double[] params : elements) { // For each parameter
                    // Replace smallest with the smallest possible
                    n_ranges[i].setMin(Math.min(n_ranges[i].getMin(), params[i]));

                    // Replace largest with the largest possible
                    n_ranges[i].setMax(Math.max(n_ranges[i].getMax(), params[i]));
                }
            }
        }

        else {
            for (int i = 0; i < ranges.length; i++) { // For each dimension
                // Get the min of the 2 children
                n_ranges[i].setMin(Math.max(
                        ((RTreeNode<T>) neighbours[0]).ranges[i].getMax(),
                        ((RTreeNode<T>) neighbours[1]).ranges[i].getMax()
                ));

                // Get the max of the 2 children
                n_ranges[i].setMin(Math.min(
                    ((RTreeNode<T>) neighbours[0]).ranges[i].getMin(),
                    ((RTreeNode<T>) neighbours[1]).ranges[i].getMin()
                ));
            }
        }

        this.ranges = n_ranges;
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
            if (this.ranges[i].getMax() < e_vals[i])
                expanded *= e_vals[i] - this.getRanges()[i].getMin();

            // Expand start point
            if (this.ranges[i].getMin() > e_vals[i])
                expanded *= this.ranges[i].getMax() - e_vals[i];
        }

        return expanded - area;
    }
}

