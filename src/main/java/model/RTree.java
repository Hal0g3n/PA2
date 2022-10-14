package model;

import java.util.*;
import java.util.stream.Collectors;

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

    private final int maxChildren = 2;
    private final int minChildren = 1;
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
        Range<Double>[] ranges = new Range[numDims];

        // Setting Largest Domain in each dimension
        // sqrt(MAX_VALUE)
        for ( int i = 0; i < this.numDims; i++ )
            ranges[i] = new Range<Double> (
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
    public List<T> search(Range<Double>[] ranges) {
        if (ranges.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        LinkedList<T> results = new LinkedList<T>();
        search(ranges, root, results); // Actual Recursive function
        return results;
    }

    private void search(Range<Double>[] ranges, RTreeNode<T> n, LinkedList<T> results) {
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
                if (!RTreeNode.isOverlap(ranges, ((RTreeNode<T>) n.neighbours[i]).getRanges())) continue;

                // If leaf domain is overlapping
                search(ranges, (RTreeNode<T>) n.neighbours[i], results);
            }
    }

    /**
     * Deletes the entry associated with the given rectangle from the model.RTree
     *
     * @param ranges ranges of each axis to search in
     * @param entry the entry to delete
     * @return true if the entry was deleted from the model.RTree.
     */
    public boolean delete(Range<Double>[] ranges, T entry) {
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
            condenseTree(l);
            size--;
        }

        return (toRemove != null);
    }

    private RTreeNode<T> findLeaf(RTreeNode<T> n, Range<Double>[] ranges, T entry) {
        if (ranges.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        if (n.isLeaf())
            for (T e: n.getItem()) {
                if (!e.equals(entry)) continue;
                return n; // RTreeNode found
            }

        else
            for ( int i = 0; i < 2; ++i ) {
                // If child does not include entry range
                if (!RTreeNode.isOverlap(((RTreeNode<T>) n.neighbours[i]).getRanges(), ranges)) continue;

                // Recurse to find entry in children
                RTreeNode<T> result = findLeaf((RTreeNode<T>) n.neighbours[i], ranges, entry);
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
        Set<T> orphans = new HashSet<>();

        while ( n != root ) {
            if ( n.isLeaf() && (n.getItem().size() < minEntries)) {
                orphans.addAll(n.getItem());
                n.neighbours[3].neighbours[(int) (n.getId() % 2)] = null;
            }
            else if (!n.isLeaf() && (n.getNumChildren() < minChildren)) {
                n.neighbours[3].neighbours[(int) (n.getId() % 2)] = null;
            }
            else n.tighten();

            n = (RTreeNode<T>) n.neighbours[3];
        }

        // now n is the root
        if (n.neighbours[0] == null || n.neighbours[1] == null) {
            // roots with one child are not allowed
            RTreeNode<T> child = (RTreeNode<T>) (n.neighbours[0] == null ? n.neighbours[1] : n.neighbours[0]);
            child.neighbours[3] = null;
            root = child;
        }

        for (T entry: orphans) {
            // add the orphans back
            insert(entry);
        }
    }


    /**
     * Inserting an entry into the R-Tree
     */
    public void insert(T entry) {
        if (entry.getParamValues().length != numDims) throw new IllegalArgumentException("输入的大小不对");

        RTreeNode<T> leaf = chooseLeaf(root, entry);
        leaf.addEntries(entry);

        // It is time to die leaf, you are too fat
        if ( leaf.getItem().size() > maxEntries ) {
            RTreeNode<T>[] splits = splitRTreeNode(leaf);
            adjustTree(splits[0], splits[1]);
        }
        // No splitting, just adjust the tree
        else adjustTree(leaf, null);
    }

    private void adjustTree(RTreeNode<T> n, RTreeNode<T> nn) {
        if ( n == root ) {
            if ( nn != null ) {
                // build new root and add children.
                root = buildRoot(false);
                root.addChild(n);
                n.neighbours[3] = root;
                root.addChild(nn);
                nn.neighbours[3] = root;
            }
            root.tighten();
            return;
        }
        n.tighten();
        if ( nn != null ) {
            nn.tighten();
            if ( ((RTreeNode<T>) n.neighbours[3]).getNumChildren() > maxChildren ) {
                RTreeNode<T>[] splits = splitRTreeNode((RTreeNode<T>) n.neighbours[2]);
                adjustTree(splits[0], splits[1]);
            }
        }
        else if ( n.neighbours[3] != null ) {
            adjustTree((RTreeNode<T>) n.neighbours[3], null);
        }
    }

    /**
     * Splits a node, returning 2 nodes, left and right.
     * ! The actual node itself is recycled as left node
     *
     * ! Does not randomise insertion into nodes, but factors are enough to make this fast
     * @param n The node to split
     */
    private RTreeNode<T>[] splitRTreeNode(RTreeNode<T> n) {
        // Java was scream so here, to appease you
        if (n == null) return null;

        // Generate the new nodes (Recycle the old node)
        RTreeNode<T>[] n_nodes = new RTreeNode[] {n, new RTreeNode<T>(new LinkedList<>(), n.getRanges(), n.isLeaf(), (RTreeNode<T>) n.neighbours[2])};

        // Add children to parent
        if ( n_nodes[1].neighbours[3] != null ) ((RTreeNode<T>) n_nodes[1].neighbours[3]).addChild(n_nodes[1]);

        // List of entries and clear n for reuse
        LinkedList<T> cc = new LinkedList<>(n.getItem());
        n.getItem().clear();

        n.neighbours = new RTreeNode[] {null, null, null, (RTreeNode<T>) n.neighbours[3]}; // Clear the neighbours

        // Select the first elements to add
        T[] ss = pickSeeds(cc);
        n_nodes[0].addEntries(ss[0]);
        n_nodes[1].addEntries(ss[1]);

        while ( !cc.isEmpty() ) {
            if ((n_nodes[0].getNumChildren() >= minEntries) &&
                (n_nodes[1].getNumChildren() + cc.size() == minEntries)) {
                // Case 1: Dump everything into the right node to meet min
                n_nodes[1].addEntries(cc.toArray((T[]) new Object[0]));
                cc.clear();
                return n_nodes;
            }

            else if ((n_nodes[1].neighbours.length >= minEntries) &&
                     (n_nodes[1].neighbours.length + cc.size() == minEntries)) {
                // Case 2: Dump everything into the left node to meet min
                n_nodes[0].addEntries(cc.toArray((T[]) new Object[0]));
                cc.clear();
                return n_nodes;
            }

            // Case 3: Indeterminate, insert one by one
            T c = cc.pop();         // The entry to add


            // Factor 1: Select node with smaller expansion //
            // Get expansion of area to insert c
            double e0 = n_nodes[0].getAreaExpansion(c);
            double e1 = n_nodes[1].getAreaExpansion(c);
            // If factor differentiates, insert and move on
            if (e0 != e1) {
                n_nodes[e0 < e1 ? 0 : 1].addEntries(c);
                continue;
            }


            // Factor 2: Select smaller node //
            double a0 = getArea(n_nodes[0]); // Calculates the Initial Area
            double a1 = getArea(n_nodes[1]); // Calculates the Initial Area
            // If factor differentiates, insert and move on
            if (a0 != a1) {
                n_nodes[a0 < a1 ? 0 : 1].addEntries(c);
                continue;
            }


            // Factor 3: Decide on number of entries //
            if (n_nodes[0].getItem().size() < n_nodes[1].getItem().size())
                n_nodes[0].addEntries(c);
            else n_nodes[1].addEntries(c);
        }

        // Restrict their ranges
        n_nodes[0].tighten();
        n_nodes[1].tighten();

        // And returns the new nodes
        return n_nodes;
    }

    /**
     * Selects the 2 entries acting as the splitting pair
     * One goes in left and the other goes in right
     * Everything else will is only better in one than the other
     *
     * The picked entries are ejected from the list passed in
     *
     * @param entries - The list of entries to split
     */
    private T[] pickSeeds(LinkedList<T> entries) {
        // Collect the Param stuff
        double[][] elements = (double[][]) entries.stream().map(RTreeEntry::getParamValues).toArray();
        T[] arr_entries = (T[]) entries.toArray(); // And also make each entry more accessible

        // keeps track of the best separation between the center 2 nodes
        double bestSep = 0.0f;

        // The best pair of entries to split by
        T[] bestPair = null;
        for ( int dim = 0; dim < numDims; dim++ ) {

            // Many variables to keep track of range of min and max in the dimension
            double dimLb = Double.MAX_VALUE, dimMinUb = Double.MAX_VALUE;
            double dimUb = -1.0f * Double.MAX_VALUE, dimMaxLb = -1.0f * Double.MAX_VALUE;

            // Keeps track of entries with largest Min and smallest Max
            T nMaxLb = null, nMinUb = null;

            // For each entry
            for (int i = 0; i < elements.length; ++i) {
                // Get the parameters from precomp
                double[] params = elements[i];

                // Comparing and updating the min max, etc.
                if ( params[dim] < dimLb ) dimLb = params[dim]; // The minimum
                if ( params[dim] > dimUb ) dimUb = params[dim]; // The maximum

                // The Largest lower bound
                if ( params[dim] > dimMaxLb ) {
                    dimMaxLb = params[dim];
                    nMaxLb = arr_entries[i];
                }

                // The lowest upper bound
                if ( params[dim] < dimMinUb ) {
                    dimMinUb = params[dim];
                    nMinUb = arr_entries[i];
                }
            }

            // Calculate the pairs separation value
            double sep = Math.abs((dimMinUb - dimMaxLb) / (dimUb - dimLb));

            // Check if this split the array "more"
            if ( sep >= bestSep ) {
                // Maximises the split and replaces the smaller one
                bestPair = (T[]) new Object[]{ nMaxLb, nMinUb };
                bestSep = sep;
            }
        }

        // Removes from list and returns the picked Seeds
        entries.remove(bestPair[0]);
        entries.remove(bestPair[1]);
        return bestPair;
    }

    private RTreeNode<T> chooseLeaf(RTreeNode<T> n, T e) {
        // Well it is the leaf
        if ( n.isLeaf() ) return n;

        double minInc = Double.MAX_VALUE;
        RTreeNode<T> next = null;
        for ( RTreeNode<T> c: (RTreeNode<T>[]) n.neighbours ) {
            double inc = c.getAreaExpansion( e );
            if ( inc < minInc ) {
                minInc = inc;
                next = c;
            }
            else if ( inc == minInc ) {
                double curArea = 1.0f;
                double thisArea = 1.0f;
                for ( int i = 0; i < c.getRanges().length; i++ ) {
                    assert next != null;
                    curArea *= next.getRanges()[i].getMax() - next.getRanges()[i].getMin();
                    thisArea *= c.getRanges()[i].getMax() - c.getRanges()[i].getMin();
                }
                if ( thisArea < curArea ) next = c;
            }
        }
        assert next != null;
        return chooseLeaf(next, e);
    }

    public void clear() { root = buildRoot(true); } // Garbage Collector will clear the rest

}