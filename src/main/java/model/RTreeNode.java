package model;

import AVLs.ElementNotFoundException;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class RTreeNode<T extends RTreeEntry> extends model.Node<List<T>>{
    // data members
    private BitSet id;
    public Range<Double>[] ranges;

    private long numEntries;
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

    public BitSet getId() { return this.id; }

    public void addEntries(T... entries) {
        this.item.addAll(Arrays.asList(entries));
        numEntries += entries.length;
    }

    static public boolean isOverlap(Range<Double>[] r1, Range<Double>[] r2 ) {
        if (r1.length != r2.length) throw new IllegalArgumentException("输入的顶点的范围数不一样");

        for (int i = 0; i < r1.length; i++) {
            if (r1[i].getMin() > r2[i].getMax() || r2[i].getMin() > r1[i].getMax())
                return false;
        }

        return true;
    }

    static public boolean isInRange(Range<Double>[] r1, Double[] coords) {
        if (r1.length != coords.length) throw new IllegalArgumentException("输入的范围数不一样");

        for (int i = 0; i < r1.length; i++) {
            if (coords[i] < r1[i].getMin() || r1[i].getMax() < coords[i])
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

    public void addChild(RTreeNode<T> node) {
        for (int i = 0; i < 3; ++i) {
            if (neighbours[i] == null) {
                neighbours[i] = node;
                ++numChildren;
                return;
            }
        }
        throw new IllegalStateException("太多小孩了");
    }

    public void removeChild(int index) {
        neighbours[index] = null;
        --numChildren;
    }

    public void removeChild(RTreeNode node) {
        for (int i = 0; i < 3; ++i) {
            if (neighbours[i].equals(node)) {
                neighbours[i] = null;
                --numChildren;
                return;
            }
        }
        throw new ElementNotFoundException("找不到输入的小孩");
    }

    /**
     * Recomputes the dependent values
     */
    public void tighten() {
        // Copies the old ranges
        Range<Double>[] n_ranges = Arrays.copyOf(ranges, ranges.length);

        if (this.isLeaf()) {
            // Get all parameter values of entries stored
            List<Double[]> elements = item.stream().map(RTreeEntry::getParamValues).collect(Collectors.toList());

            for (int i = 0; i < ranges.length; i++) { // For each dimension
                n_ranges[i].setMin(Double.MAX_VALUE);
                n_ranges[i].setMax(Double.MIN_VALUE);

                for (Double[] params : elements) { // For each parameter
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

        // Updates the ranges, making it a tighter domain
        this.ranges = n_ranges;

        // Recompute the number of entries in subtree
        numEntries = item.size();
        for (int i = 0; i < 2; ++i) if (neighbours[i] != null)
            numEntries += ((RTreeNode<T>) neighbours[i]).numEntries;
    }

    /**
     * Returns increase in area to include given element
     * @param e - The element that may be inserted
     */
    double getAreaExpansion(T e) {
        double expanded = 1.0f;           // New area

        Double[] e_vals = e.getParamValues(); // Get the parametrized values

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

        // Calculate Difference and return that
        return expanded - getArea(this);
    }


    /**
     * Returns increase in area to include given element
     * @param e - The element that may be inserted
     */
    double getAreaExpansion( RTreeNode<T> e ) {
        double expanded = 1.0;

        if (e.ranges.length != ranges.length) throw new IllegalArgumentException("e的参数数不对");

        for ( int i = 0; i < e.ranges.length; i++ ) {
            double delta = 0.0; // Change in length
            // Expand end point
            if (this.ranges[i].getMax() < e.ranges[i].getMax())
                delta += e.ranges[i].getMax() - this.getRanges()[i].getMax();

            // Expand start point
            if (this.ranges[i].getMin() > e.ranges[i].getMin())
                delta += this.ranges[i].getMin() - e.ranges[i].getMin();

            // Multiply to expanded running area
            expanded *= delta + this.ranges[i].getMax() - this.ranges[i].getMin();
        }

        // Calculate Difference and return that
        return expanded - getArea(this);
    }

    @Override
    public boolean equals(Object o) {
        // overload of equals just in case, used in removeChild
        return (o instanceof RTreeNode && this.id == ((RTreeNode<T>) o).id);
    }
}

