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
public class RTree<T extends RTreeEntry> {
    private String id;

    private final int maxEntries;
    private final int minEntries;

    private final int maxChildren = 2;
    private final int minChildren = 1;
    private final int numDims;

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
        // ± sqrt(MAX_VALUE)
        for ( int i = 0; i < this.numDims; i++ )
            ranges[i] = new Range<>(
                Math.sqrt(Double.MAX_VALUE),
                -2.0f * Math.sqrt(Double.MAX_VALUE)
            );

        return new RTreeNode<>(new ArrayList<>(), ranges, asLeaf, null);
    }

    /**
     * Default Constructor
     * (Basically a segment tree)
     */
    public RTree() {this(1, 0, 1);}

    /**
     * Obtains the root
     * @return root of the tree
     */

    public RTreeNode<T> getRoot() {
        return root;
    }

    /**
     * Searches the model.RTree for objects in query range
     * @return list of entries of objects in query range
     */
    public List<T> search(Range<Double>[] ranges) {
        if (ranges.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        LinkedList<T> results = new LinkedList<>();
        search(root, ranges, results); // Actual Recursive function
        return results;
    }

    private void search(RTreeNode<T> n, Range<Double>[] ranges, LinkedList<T> results) {
        if (ranges.length != numDims) throw new IllegalArgumentException("输入的数组大小不对");

        if (n.isLeaf()) // n is leaf, contains entries
            for (T e: n.getItem()) { // For each entry
                if (RTreeNode.isInRange(ranges, e.getParamValues()))
                    results.add(e);
            }
        else // If not leaf, travel down the children
            for (int i = 0; i < maxChildren; ++i) { // 2 children
                // Subtree does not contain the query range or child does not exist
                if (n.neighbours[i] == null || !RTreeNode.isOverlap(ranges, ((RTreeNode<T>) n.neighbours[i]).getRanges())) continue;

                // If leaf domain is overlapping
                search((RTreeNode<T>) n.neighbours[i], ranges, results);
            }
    }

    /**
     * Deletes the entry associated with the given rectangle from the model.RTree
     * @param entry the entry to delete
     * @return true if the entry was deleted from the model.RTree.
     */
    public boolean delete(T entry) {
        RTreeNode<T> leaf = findLeaf(root, entry.getParamValues(), entry);

        // Some checks
        if (leaf == null) throw new IllegalStateException("找不到树叶");
        if (!leaf.isLeaf()) throw new IllegalStateException("找到的不是树叶");

        for (T e : leaf.getItem()) {
            // Reject if not entry
            if ( !e.equals(entry) ) continue;

            // Entry found, kill it now
            leaf.getItem().remove(e);
            condenseTree(leaf); // Try to reduce tree size
            return true;
        }

        // Deletion failed
        return false;
    }

    /**
     * Called to propagate the deletion of a node
     * @param node - The GhostNode to delete
     * @return
     */
    public boolean delete(GhostNode<T> node) {
        if (node.getRanges().length != numDims) throw new IllegalArgumentException("输入的范围大小不对");

        RTreeNode<T> parent = root; // parent of n_node (will be found soon)

        for (int lvl = 0; lvl < node.getId().size(); ++lvl) {

            for (Node<List<T>> child : parent.neighbours) {
                if (child != node) continue;

                // Child found, delete it and condense tree
                parent.removeChild((RTreeNode<T>) child);
                condenseTree(parent);
                return true;
            }

            // Not found at this level, go down
            parent = (RTreeNode<T>) root.neighbours[node.getId().get(lvl) ? 1 : 0];
        }

        return false;
    }


    /**
     * Searches for the leaf containing the entry
     * @param n - The root of subtree to find leaf in
     * @param params - The param values of entry (precomputed to prevent wasting time)
     * @param entry - The entry to find
     */
    private RTreeNode<T> findLeaf(RTreeNode<T> n, Double[] params, T entry) {
        if (params.length != n.getRanges().length) throw new IllegalArgumentException("输入的数组大小不对");

        if (n.isLeaf())
            for (T e: n.getItem()) {
                if (!e.equals(entry)) continue;
                return n; // RTreeNode found
            }

        else
            for ( int i = 0; i < maxChildren; ++i ) {
                // ignore null children
                if (n.neighbours[i] == null) continue;
                // If child does not include entry range
                if (!RTreeNode.isInRange(((RTreeNode<T>) n.neighbours[i]).getRanges(), params)) continue;

                // Recurse to find entry in children
                RTreeNode<T> result = findLeaf((RTreeNode<T>) n.neighbours[i], params, entry);
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
        Set<T> orphans = new HashSet<>(); // Does this need to be a set?

        while ( n != root ) {
            if ( n.isLeaf() && (n.getItem().size() < minEntries)) {
                orphans.addAll(n.getItem());
                ((RTreeNode<T>) n.neighbours[3]).removeChild(n);
            }
            else if (!n.isLeaf() && (n.getNumChildren() < minChildren)) {
                // This only works for our case where minChildren is 1
                // Since the node would have 0 children when it has below minChildren amount of children
                ((RTreeNode<T>) n.neighbours[3]).removeChild(n);
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

        // Choose leaf, and add entry to it
        RTreeNode<T> leaf = chooseLeaf(root, entry);
        leaf.addEntry(entry);

        // It is time to die leaf, you are too fat
        if ( leaf.getItem().size() > maxEntries ) {
            RTreeNode<T>[] splits = splitNode(leaf);
            adjustTree(splits[0], splits[1]);
        }
        // No splitting, just adjust the tree
        else adjustTree(leaf, null);
    }

    /**
     * Inserting the ghost node
     * Called to propagate changes from other branches
     */
    public void insert(GhostNode<T> n_node) {
        RTreeNode<T> parent = root; // parent of n_node (will be found soon)

        for (int lvl = 0; lvl < n_node.getId().size(); ++lvl) {
            // If we cannot take the next step, we insert here
            if (parent.neighbours[n_node.getId().get(lvl) ? 1 : 0] == null || !(parent.neighbours[n_node.getId().get(lvl) ? 1 : 0] instanceof GhostNode)) {
                // Add to parent
                parent.addChild(n_node);

                // It is time to die parent, you are too fat
                if ( parent.neighbours.length > maxChildren ) {
                    RTreeNode<T>[] splits = splitNode(parent);
                    adjustTree(splits[0], splits[1]);
                }
                // No splitting, just adjust the tree
                else adjustTree(parent, null);

                // Insertion Successful
                return;
            }
            else parent = (RTreeNode<T>) root.neighbours[n_node.getId().get(lvl) ? 1 : 0];
        }

        throw new IllegalStateException("找不到输入顶点的长辈");
    }


    /**
     * Selects the best leaf to insert the given entry
     * @param n - The root to find the leaf in
     * @param entry - The entry to insert
     */
    private RTreeNode<T> chooseLeaf(RTreeNode<T> n, T entry) {
        // Well it is the leaf
        if ( n.isLeaf() ) return n;

        // Keeps the minimum increment in area
        double minInc = Double.MAX_VALUE;

        // Keeps track of the node fit to hold entry
        RTreeNode<T> next = null;
        double bestArea = Double.MAX_VALUE; // To tiebreak by area

        // For each child
        for ( int k = 0; k < 2; ++k ) {
            if (n.neighbours[k] == null) continue; // Child does not exist

            RTreeNode<T> child = (RTreeNode<T>) n.neighbours[k];

            // Calculate the area expansion
            double inc = child.getAreaExpansion( entry );
            double area = getArea(child);

            // Not the best node if area expands more or expansion same but larger area
            if (inc > minInc || (inc == minInc && area >= bestArea)) continue;

            // Set the new running best node to explore
            next = child;
            minInc = inc;
            bestArea = area;
        }

        // Explore down into the child
        if (next == null) throw new IllegalStateException("没有适合的孩子");
        return chooseLeaf(next, entry);
    }

    private void adjustTree(RTreeNode<T> node, RTreeNode<T> sibling) {
        if ( node == root ) {
            // There is a sibling to the root? this means node is not the root
            // We need to create a new root!
            if ( sibling != null ) {
                // build new root and add children.
                root = buildRoot(false);

                root.addChild(node);
                root.addChild(sibling);

                // Register new root as parent
                node.neighbours[3] = root;
                sibling.neighbours[3] = root;
            }

            // Update the root domain
            root.tighten();
            return;
        }

        // Node is not root
        node.tighten();
        if ( sibling != null ) { // New Sibling exists
            sibling.tighten();

            // Check if splitting is required
            if ( ((RTreeNode<T>) node.neighbours[3]).getNumChildren() > maxChildren ) {
                RTreeNode<T>[] splits = splitNode((RTreeNode<T>) node.neighbours[3]);
                adjustTree(splits[0], splits[1]);
            }
        }
        else if ( node.neighbours[3] != null ) { // node has parent
            adjustTree((RTreeNode<T>) node.neighbours[3], null);
        }
    }


    /**
     * Splits a node, returning 2 nodes, left and right.
     * ! The actual node itself is recycled as left node
     *
     * ! Does not randomise insertion into nodes, but factors are enough to make this fast
     * @param n The node to split
     */
    private RTreeNode<T>[] splitNode(RTreeNode<T> n) {
        // If it is leaf, call the other function
        if (n == null || n.isLeaf()) return splitLeaf(n);

        // Generate the new nodes (Recycle the old node)
        // have to deep copy ranges
        Range<Double>[] newRanges = new Range[numDims];
        for (int i = 0; i < numDims; ++i) {
            newRanges[i] = new Range<>(n.getRanges()[i].getMin(), n.getRanges()[i].getMax());
        }
        RTreeNode<T>[] n_nodes = new RTreeNode[] {n, new RTreeNode<>(new LinkedList<>(), newRanges, false, (RTreeNode<T>) n.neighbours[3])};

        // Add children to parent
        if ( n_nodes[1].neighbours[3] != null ) ((RTreeNode<T>) n_nodes[1].neighbours[3]).addChild(n_nodes[1]);

        // List of entries and clear n for reuse
        LinkedList<RTreeNode<T>> cc = new LinkedList<>();
        for (int i = 0; i < 3; ++i) {
            // Non-existent
            if (n.neighbours[i] == null) continue;
            cc.add((RTreeNode<T>) n.neighbours[i]);
            n.removeChild(i);
        }

        n.neighbours = new RTreeNode[] {null, null, null, (RTreeNode<T>) n.neighbours[3]}; // Clear the neighbours

        // Select the first elements to add
        ArrayList<RTreeNode<T>> ss = pickNodeSeeds(cc);
        n_nodes[0].addChild(ss.get(0));
        n_nodes[1].addChild(ss.get(1));

        // While there are still stuff to add
        while ( !cc.isEmpty() ) {
            // This while loop is to make it nicer
            // In actuality this is only run once due to constrains

            RTreeNode<T> c = cc.pop();         // The entry to add


            // Factor 1: Select node with smaller expansion //
            // Get expansion of area to insert c
            double e0 = n_nodes[0].getAreaExpansion(c);
            double e1 = n_nodes[1].getAreaExpansion(c);
            // If factor differentiates, insert and move on
            if (e0 != e1) {
                n_nodes[e0 < e1 ? 0 : 1].addChild(c);
                continue;
            }


            // Factor 2: Select smaller node //
            double a0 = getArea(n_nodes[0]); // Calculates the Initial Area
            double a1 = getArea(n_nodes[1]); // Calculates the Initial Area
            // If factor differentiates, insert and move on
            if (a0 != a1) {
                n_nodes[a0 < a1 ? 0 : 1].addChild(c);
                continue;
            }


            // Factor 3: Decide on number of entries //
            if (n_nodes[0].getNumChildren() < n_nodes[1].getNumChildren())
                n_nodes[0].addChild(c);
            else n_nodes[1].addChild(c);
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
     * <p>
     * The picked entries are ejected from the list passed in
     *
     * @param children - The list of entries to split
     */
    private ArrayList<RTreeNode<T>> pickNodeSeeds(LinkedList<RTreeNode<T>> children) {
        // keeps track of the best separation between the center 2 nodes
        double bestSep = 0.0f;

        // The best pair of children to split by
        ArrayList<RTreeNode<T>> bestPair = new ArrayList<>(2);
        bestPair.add(null); bestPair.add(null); // this is an array but java dont like a real array

        for ( int dim = 0; dim < numDims; dim++ ) { // For each dimension

            // Many variables to keep track of range of min and max in the dimension
            double dimLb = Double.MAX_VALUE, dimMinUb = Double.MAX_VALUE;
            double dimUb = -1.0f * Double.MAX_VALUE, dimMaxLb = -1.0f * Double.MAX_VALUE;

            // Keeps track of children with largest Min and smallest Max
            RTreeNode<T> nMaxLb = null, nMinUb = null;

            // For each child
            for (RTreeNode<T> e : children) {

                // Comparing and updating the min max, etc.
                if ( e.getRanges()[dim].getMin() < dimLb ) dimLb = e.getRanges()[dim].getMin(); // The minimum
                if ( e.getRanges()[dim].getMax() > dimUb ) dimUb = e.getRanges()[dim].getMax(); // The maximum

                // The Largest lower bound
                if ( e.getRanges()[dim].getMin() > dimMaxLb ) {
                    dimMaxLb = e.getRanges()[dim].getMin();
                    nMaxLb = e;
                }

                // The lowest upper bound
                if ( e.getRanges()[dim].getMax() < dimMinUb ) {
                    dimMinUb = e.getRanges()[dim].getMax();
                    nMinUb = e;
                }
            }

            // Calculate the pairs separation value
            double sep = Math.abs((dimMinUb - dimMaxLb) / (dimUb - dimLb));

            // Check if this split the array "more"
            if ( sep >= bestSep ) {
                // Maximises the split and replaces the smaller one
                bestPair.set(0, nMaxLb);
                bestPair.set(1, nMinUb);
                bestSep = sep;
            }
        }

        // Removes from list and returns the picked Seeds
        children.remove(bestPair.get(0));
        children.remove(bestPair.get(1));
        return bestPair;
    }

    /**
     * Splits a leaf, returning 2 nodes, left and right leaves
     * ! The actual node itself is recycled as left node
     *
     * ! Does not randomise insertion into nodes, but factors are enough to make this fast
     * @param n The node to split
     */
    private RTreeNode<T>[] splitLeaf(RTreeNode<T> n) {
        // Java was scream so here, to appease you
        if (n == null) return null;

        // Generate the new nodes (Recycle the old node)
        // have to deep copy ranges
        Range<Double>[] newRanges = new Range[numDims];
        for (int i = 0; i < numDims; ++i) {
            newRanges[i] = new Range<>(n.getRanges()[i].getMin(), n.getRanges()[i].getMax());
        }
        RTreeNode<T>[] n_nodes = new RTreeNode[] {n, new RTreeNode<>(new LinkedList<>(), newRanges, true, (RTreeNode<T>) n.neighbours[3])};

        // Add children to parent
        if ( n_nodes[1].neighbours[3] != null ) ((RTreeNode<T>) n_nodes[1].neighbours[3]).addChild(n_nodes[1]);

        // List of entries and clear n for reuse
        LinkedList<T> cc = new LinkedList<>(n.getItem());
        n.getItem().clear();

        n.neighbours = new RTreeNode[] {null, null, null, (RTreeNode<T>) n.neighbours[3]}; // Clear the neighbours

        // Select the first elements to add
        ArrayList<T> ss = pickLeafSeeds(cc);
        n_nodes[0].addEntry(ss.get(0));
        n_nodes[1].addEntry(ss.get(1));

        // While there are still stuff to add
        while ( !cc.isEmpty() ) {
            if ((n_nodes[0].getItem().size() >= minEntries) &&
                (n_nodes[1].getItem().size() + cc.size() == minEntries)) {
                // Case 1: Dump everything into the right node to meet min
                n_nodes[1].addEntries(cc);
                cc.clear();
                return n_nodes;
            }

            else if ((n_nodes[1].getItem().size() >= minEntries) &&
                     (n_nodes[0].getItem().size() + cc.size() == minEntries)) {
                // Case 2: Dump everything into the left node to meet min
                n_nodes[0].addEntries(cc);
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
                n_nodes[e0 < e1 ? 0 : 1].addEntry(c);
                continue;
            }


            // Factor 2: Select smaller node //
            double a0 = getArea(n_nodes[0]); // Calculates the Initial Area
            double a1 = getArea(n_nodes[1]); // Calculates the Initial Area
            // If factor differentiates, insert and move on
            if (a0 != a1) {
                n_nodes[a0 < a1 ? 0 : 1].addEntry(c);
                continue;
            }


            // Factor 3: Decide on number of entries //
            if (n_nodes[0].getItem().size() < n_nodes[1].getItem().size())
                n_nodes[0].addEntry(c);
            else n_nodes[1].addEntry(c);
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
    private ArrayList<T> pickLeafSeeds(LinkedList<T> entries) {
        // Collect the Param stuff
        Double[][] elements = Arrays.copyOf(entries.stream().map(RTreeEntry::getParamValues).toArray(), entries.size(), Double[][].class);

        // keeps track of the best separation between the center 2 nodes
        double bestSep = 0.0f;

        // The best pair of entries to split by
        ArrayList<T> bestPair = new ArrayList<>(2);
        bestPair.add(null); bestPair.add(null); // this is so inelegant but java doesn't like generic arrays

        for ( int dim = 0; dim < numDims; dim++ ) { // For each dimension

            // Many variables to keep track of range of min and max in the dimension
            double dimLb = Double.MAX_VALUE, dimMinUb = Double.MAX_VALUE;
            double dimUb = -1.0f * Double.MAX_VALUE, dimMaxLb = -1.0f * Double.MAX_VALUE;

            // Keeps track of entries with largest Min and smallest Max
            T nMaxLb = null, nMinUb = null;

            // For each entry
            for (int i = 0; i < elements.length; ++i) {
                // Get the parameters from precomp
                Double[] params = elements[i];

                // Comparing and updating the min max, etc.
                if ( params[dim] < dimLb ) dimLb = params[dim]; // The minimum
                if ( params[dim] > dimUb ) dimUb = params[dim]; // The maximum

                // The Largest lower bound
                if ( params[dim] > dimMaxLb ) {
                    dimMaxLb = params[dim];
                    nMaxLb = entries.get(i);
                }

                // The lowest upper bound
                if ( params[dim] < dimMinUb ) {
                    dimMinUb = params[dim];
                    nMinUb = entries.get(i);
                }
            }

            // Calculate the pairs separation value
            double sep = Math.abs((dimMinUb - dimMaxLb) / (dimUb - dimLb));

            // Check if this splits the array "more"
            if ( sep >= bestSep ) {
                // Maximises the split and replaces the smaller one
                bestPair.set(0, nMaxLb);
                bestPair.set(1, nMinUb);
                bestSep = sep;
            }
        }

        // Removes from list and returns the picked Seeds
        entries.remove(bestPair.get(0));
        entries.remove(bestPair.get(1));
        return bestPair;
    }

    public void clear() { root = buildRoot(true); } // Garbage Collector will clear the rest

}