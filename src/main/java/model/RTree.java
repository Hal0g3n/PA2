package model;

import java.util.*;
import static model.RTreeNode.*;

/**
 * Based on R-Trees: A Dynamic Index Structure for Spatial Searching
 * (Antonn Guttmann, 1984)
 *
 * Adapted from: https://searchcode.com/codesearch/raw/79146202/
 *
 * @param <T> the Entry to store in the model.RTree.
 */
public class RTree<T extends Comparable<T> & RTreeEntry> {
    private String id;

    private final int maxEntries;
    private final int minEntries;
    private final int numDims;
    private int size;

    private RTreeNode<T> root;

    public RTree(int maxEntries, int minEntries, int numDims) {
        if (minEntries * 2 > maxEntries) throw new IllegalArgumentException("minEntries太大");
        this.numDims = numDims;
        this.maxEntries = maxEntries;
        this.minEntries = minEntries;

        root = buildRoot(true);
    }

    /**
     * Creates the root, representing the largest search domain
     * @return the root node
     */
    private RTreeNode<T> buildRoot(boolean asLeaf) {
        Pair<Double, Double>[] ranges = new Pair[numDims];

        // Setting Largest Domain in each dimension
        // sqrt(MAX_VALUE)
        for ( int i = 0; i < this.numDims; i++ )
            ranges[i] = new Pair<Double, Double> (
                Math.sqrt(Double.MAX_VALUE),
                -2.0f * Math.sqrt(Double.MAX_VALUE)
            );

        return new RTreeNode<T>(new ArrayList<T>(), ranges, asLeaf, null);
    }

    /**
     * Default Constructor
     * (Basically a segment tree)
     */
    public RTree() {this(1, 0, 1);}

    /**
     * Searches the model.RTree for objects in query range
     * @return list of entries of objects in query range
     */
    public List<T> search(Pair<Double, Double>[] ranges) {
        if (ranges.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        LinkedList<T> results = new LinkedList<T>();
        search(ranges, root, results); // Actual Recursive function
        return results;
    }

    private void search(Pair<Double, Double>[] ranges, RTreeNode<T> n, LinkedList<T> results) {
        if (ranges.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        if (n.isLeaf()) // If leaf, add the children
            // Check if children is overlapping
            // ** Here I assumed that the entry coordinates are all the same as the leaf node coordinates, so I just check once outside
            // Add Entry to results
            if (RTreeNode.isOverlap(ranges, n.getRanges()))
                results.addAll(n.getItem());
        else // If not leaf, travel down the children
            for (int i = 0; i < 2; ++i) { // 2 children
                // Subtree does not contain the query range
                if (!RTreeNode.isOverlap(ranges, n.neighbours[i].getRanges())) continue;

                // If leaf domain is overlapping
                search(ranges, n.neighbours[i], results);
            }
    }

    /**
     * Deletes the entry associated with the given rectangle from the model.RTree
     *
     * @param ranges ranges of each axis to search in
     * @param entry the entry to delete
     * @return true if the entry was deleted from the model.RTree.
     */
    public boolean delete(Pair<Double, Double>[] ranges, T entry) {
        if (ranges.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        RTreeNode<T> l = findLeaf(root, ranges, entry);
        if (l == null) throw new IllegalStateException("找不到树叶");
        if (!l.isLeaf()) throw new IllegalStateException("找到的不是树叶");
        
        ListIterator<T> li = l.getItem().listIterator();
        T toRemove = null;
        while (li.hasNext()) {
            T e = li.next();
            if ( !e.equals(entry) ) continue;
            toRemove = e;
            break;
        }

        // RTreeNode found, size decrease and condense the tree
        if ( toRemove != null ) {
            // condenseTree(l);
            size--;
        }

        return (toRemove != null);
    }

    private RTreeNode<T> findLeaf(RTreeNode<T> n, double[] coords, double[] dimensions, T entry) {
        if (coords.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");
        if (dimensions.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        if (n.isLeaf())
            for (T e: n.getItem()) {
                if (!e.equals(entry)) continue;
                return n; // RTreeNode found
            }

        else
            for ( int i = 0; i < 2; ++i ) {
                // If child does not include entry range
                if (!RTreeNode.isOverlap((RTreeNode<T>[]) n.neighbours[i]).getRanges(), ranges)) continue;

                // Recurse to find entry in children
                RTreeNode<T> result = findLeaf((RTreeNode<T>[]) n.neighbours[i], ranges, entry);
                if ( result != null ) return result;
            }

        // Nothing :(
        return null;
    }

    /**
     * Tree Compression with node as subtree root
     * @param n the subtree root
     */
    private void condenseTree(RTreeNode<T> n) {
        Set<RTreeNode> q = new HashSet<>();

        while ( n != root ) {
            if ( n.isLeaf() && (n.neighbours.length < minEntries)) {
                q.addAll(List.of((RTreeNode<T>[]) n.neighbours));
                n.neighbours[3].neighbours.remove(n);
            }
            else if (!n.isLeaf() && (n.neighbours.length < minEntries)) {
                // probably a more efficient way to do this...
                LinkedList<RTreeNode> toVisit = new LinkedList<>(List.of((RTreeNode<T>[]) n.neighbours));
                while (!toVisit.isEmpty()) {
                    RTreeNode<T> c = toVisit.pop();
                    if ( c.isLeaf() ) q.addAll(List.of((RTreeNode<T>[]) c.neighbours));
                    else toVisit.addAll(List.of((RTreeNode<T>[]) c.neighbours));
                }
                n.neighbours[3].neighbours.remove(n);
            }
            else tighten(n);

            n = (RTreeNode<T>) n.neighbours[3];
        }

        for (RTreeNode ne: q) {
            @SuppressWarnings("unchecked")
            Entry e = (Entry)ne;
            insert(e.coords, e.dimensions, e.entry);
        }
    }

    /**
     * Inserts the given entry into the model.RTree, associated with the given rectangle.
     * @param coords the corner of the rectangle that is the lower bound in
     * every dimension
     * @param dimensions the dimensions of the rectangle
     * @param entry the entry to insert
     */
    public void insert(double[] coords, double[] dimensions, T entry) {
        if (coords.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");
        if (dimensions.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        RTreeNode<T> l = chooseLeaf(root, entry);
        l.addEntry(entry);

        if ( l.neighbours.length > maxEntries ) {
            RTreeNode[] splits = splitRTreeNode(l);
            adjustTree(splits[0], splits[1]);
        }

        else {
            adjustTree(l, null);
        }
    }

    private void adjustTree(RTreeNode<T> n, RTreeNode<T> nn) {
        if ( n == root ) {
            if ( nn != null ) {
                // build new root and add children.
                root = buildRoot(false);
                root.addChild(n);
                n.neighbours[2] = root;
                root.addChild(nn);
                nn.neighbours[2] = root;
            }
            tighten(root);
            return;
        }
        tighten(n);
        if ( nn != null ) {
            tighten(nn);
            if ( n.neighbours[3].neighbours.length > maxEntries ) {
                RTreeNode[] splits = splitRTreeNode(n.neighbours[3]);
                adjustTree(splits[0], splits[1]);
            }
        }
        else if ( n.neighbours[3] != null ) {
            adjustTree((RTreeNode<T>) n.neighbours[3], null);
        }
    }

    /**
     * TODO: This needs work (they don't maintain binary structure)
     * @param n
     * @return
     */
    private RTreeNode<T>[] splitRTreeNode(RTreeNode<T> n) {
        RTreeNode<T>[] nn = new RTreeNode<T>[] {n, new RTreeNode<T>(n.getRanges(), n.isLeaf())};
        nn[1].neighbours[3] = n.neighbours[3];
        if ( nn[1].neighbours[3] != null ) {
            ((RTreeNode<T>) nn[1].neighbours[3]).addChild(nn[1]);
        }
        LinkedList<T> cc = new LinkedList<>(n.getItem());

        n.neighbours = new RTreeNode[] {null, null, null}; // Clear the neighbours
        RTreeNode<T>[] ss = pickSeeds(cc);
        nn[0].addChild(ss[0]);
        nn[1].addChild(ss[1]);
        while ( !cc.isEmpty() ) {
            if ((nn[0].neighbours.length >= minEntries) &&
                (nn[1].neighbours.length + cc.size() == minEntries)) {

                nn[1].neighbours.addAll(cc);
                cc.clear();
                return nn;
            }

            else if ((nn[1].neighbours.length >= minEntries) &&
                     (nn[1].neighbours.length + cc.size() == minEntries)) {
                nn[0].neighbours.addAll(cc);
                cc.clear();
                return nn;
            }

            T c = cc.pop();
            T preferred;

            double e0 = nn[0].getAreaExpansion(c);
            double e1 = nn[1].getAreaExpansion(c);
            if (e0 < e1) preferred = nn[0];
            else if (e0 > e1) preferred = nn[1];
            else {
                double a0 = getArea(nn[0]);
                double a1 = getArea(nn[1]);

                if (a0 < a1) preferred = nn[0];
                else if (e0 > a1) preferred = nn[1];
                else {
                    if (nn[0].neighbours.length < nn[1].neighbours.length) preferred = nn[0];
                    else if (nn[0].neighbours.length > nn[1].neighbours.length) preferred = nn[1];
                    else preferred = nn[(int)Math.round(Math.random())];
                }
            }
            preferred.addChild(c);
        }

        tighten(nn[0]);
        tighten(nn[1]);
        return nn;
    }


    private RTreeNode<T>[] pickSeeds(LinkedList<RTreeNode<T>> nn) {
        RTreeNode<T>[] bestPair = null;
        double bestSep = 0.0f;
        for ( int i = 0; i < numDims; i++ ) {
            double dimLb = Double.MAX_VALUE, dimMinUb = Double.MAX_VALUE;
            double dimUb = -1.0f * Double.MAX_VALUE, dimMaxLb = -1.0f * Double.MAX_VALUE;
            RTreeNode<T> nMaxLb = null, nMinUb = null;
            for ( RTreeNode<T> n: nn) {
                if ( n.getRanges()[i].getFirst() < dimLb ) dimLb = n.getRanges()[i].getFirst();
                if ( n.getRanges()[i].getSecond() > dimUb ) dimUb = n.getRanges()[i].getSecond();
                if ( n.getRanges()[i].getFirst() > dimMaxLb ) {
                    dimMaxLb = n.getRanges()[i].getFirst();
                    nMaxLb = n;
                }
                if ( n.getRanges()[i].getSecond() < dimMinUb ) {
                    dimMinUb = n.getRanges()[i].getSecond()
                    nMinUb = n;
                }
            }

            double sep = Math.abs((dimMinUb - dimMaxLb) / (dimUb - dimLb));
            if ( sep >= bestSep ) {
                bestPair = new RTreeNode[]{ nMaxLb, nMinUb };
                bestSep = sep;
            }
        }

        nn.remove(bestPair[0]);
        nn.remove(bestPair[1]);
        return bestPair;
    }

    private void tighten(RTreeNode<T> n) {
        double[] minCoords = new double[n.getRanges().length];
        double[] maxDimensions = new double[n.getRanges().length];
        for (int i = 0; i < minCoords.length; i++ ) {
            minCoords[i] = Double.MAX_VALUE;
            maxDimensions[i] = 0.0f;

            for (RTreeNode<T> c: (RTreeNode<T>[])n.neighbours) {
                // we may have bulk-added a bunch of children to a node (eg. in splitRTreeNode)
                // so here we just enforce the child->parent relationship.
                c.neighbours[3] = n;
                if (c.getRanges()[i].getFirst() < minCoords[i]) minCoords[i] = c.getRanges()[i].getFirst();
                if (c.getRanges()[i].getSecond() > maxDimensions[i]) maxDimensions[i] = c.getRanges()[i].getSecond();
            }
        }
        System.arraycopy(minCoords, 0, n.coords, 0, minCoords.length);
        System.arraycopy(maxDimensions, 0, n.dimensions, 0, maxDimensions.length);
    }

    private RTreeNode<T> chooseLeaf(RTreeNode<T> n, T e) {
        if ( n.isLeaf() ) {
            return n;
        }
        double minInc = Double.MAX_VALUE;
        RTreeNode<T> next = null;
        for ( RTreeNode<T> c: (RTreeNode<T>[]) n.neighbours ) {
            double inc = getRequiredExpansion( c, e );
            if ( inc < minInc ) {
                minInc = inc;
                next = c;
            }
            else if ( inc == minInc ) {
                double curArea = 1.0f;
                double thisArea = 1.0f;
                for ( int i = 0; i < c.getRanges().length; i++ ) {
                    curArea *= next.dimensions[i];
                    thisArea *= c.dimensions[i];
                }
                if ( thisArea < curArea ) next = c;
            }
        }
        return chooseLeaf(next, e);
    }

    public void clear() { root = buildRoot(true); } // Garbage Collector will clear the rest

}