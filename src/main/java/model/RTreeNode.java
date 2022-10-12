package model;

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

    static public boolean isOverlap( RTreeNode n1, RTreeNode n2 ) {
        if (n1.ranges.length != n2.ranges.length) throw new IllegalArgumentException("输入的顶点的范围数不一样");

        for (int i = 0; i < n1.ranges.length; i++) {
            boolean overlapInThisDimension = false;

            if (n1.ranges[i].getValue() == n2.ranges[i][0]) overlapInThisDimension = true;

                else if (scoords[i] < coords[i])
                    if (scoords[i] + sdimensions[i] >= coords[i]) overlapInThisDimension = true;

                    else if (scoords[i] > coords[i])
                        if ( coords[i] + dimensions[i] >= scoords[i] )
                            overlapInThisDimension = true;

            if (!overlapInThisDimension) return false;
        }

        return true;
    }
}


