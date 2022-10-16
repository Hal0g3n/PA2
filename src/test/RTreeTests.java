import model.RTree;
import model.RTreeEntry;
import model.RTreeNode;
import model.Range;
import org.junit.jupiter.api.Test;

import static java.lang.Math.signum;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;


public class RTreeTests {

    /**
     * Bad Deletion Test
     */
    @Test
    void badDeletion() {
        RTree<Entry> tree = new RTree<>(2, 1, 2);

        List<Entry> entries = new ArrayList<>(Arrays.stream(new Entry[]{
                new Entry(4.0, 3.0),
                new Entry(2.0, 1.0),
                new Entry(3.0, 4.0),
                new Entry(4.0, 1.0),
                new Entry(1.0, 5.0),
                new Entry(2.0, 6.0),
        }).toList());

        for (Entry e: entries) tree.insert(e);

        // Ensures deletion works here before the bad deletion test
        // Checks if a full query will return 5 instead of 6 elements
        tree.delete(entries.get(4));
        assertEquals(tree.search(new Range[]{new Range<>(0.0, 10.0), new Range<>(0.0, 10.1)}).size(), 5, "答案不对，你死定了");

        // Ensures Bad deletion is met with exception
        assertThrows(Exception.class, () -> tree.delete(new Entry(0.0, 0.0)));
    }

    /**
     * Should return everything as it covers all possible coordinates
     */
    @Test
    void searchNone() {
        RTree<Entry> tree = new RTree<>(2, 1, 2);

        // List of entries to insert
        List<Entry> entries = new ArrayList<>(Arrays.stream(new Entry[]{
            new Entry(4.0, 3.0),
            new Entry(2.0, 1.0),
            new Entry(3.0, 4.0),
            new Entry(4.0, 1.0),
            new Entry(1.0, 5.0),
            new Entry(2.0, 6.0),
        }).toList());
        for (Entry e: entries) tree.insert(e);

        Double[] inputs = new Double[]{0.052930, 0.375359, 0.238791, 0.619998};

        Range[] query = new Range[]{
                new Range(inputs[0], inputs[1]),
                new Range(inputs[2], inputs[3])
        };


        List<Entry> result = tree.search(query);

        entries.removeIf(e -> !RTreeNode.isInRange(query, e.getParamValues()));

        // Sorting both of them using the same function to make comparison fair
        result.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));
        entries.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));

        // Assert checker
        assertArrayEquals(result.toArray(), entries.toArray(), "答案不对，你死定了");
    }


    /**
     * Should return everything as it covers all possible coordinates
     */
    @Test
    void searchAll() {
        RTree<Entry> tree = new RTree<>(2, 1, 2);

        // List of entries to insert
        List<Entry> entries = new ArrayList<>(Arrays.stream(new Entry[]{
                new Entry(4.0, 6.0),
                new Entry(7.0, 2.0),
                new Entry(3.0, 4.0),
                new Entry(4.0, 8.0),
                new Entry(9.0, 5.0),
                new Entry(2.0, 7.0),
        }).toList());
        for (Entry e: entries) tree.insert(e);

        // Full root query
        Range[] query = tree.getRoot().getRanges();

        // Get result from query
        List<Entry> result = tree.search(query);

        // Sorting both of them using the same function to make comparison fair
        result.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));
        entries.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));

        // Assert checker
        assertArrayEquals(result.toArray(), entries.toArray(), "答案不对，你死定了");
    }

    /**
     * A generic testing framework with input parameters below
     */
    @Test
    void stressTest() { // A test to confirm that insertion, deletion and search works in general
        int T = 1000; // Number of Trials
        int N = 1000; // Number of random entries
        double P_d = 0.5; // Probability of deletion

        // Values to calculate
        int D = 0; // Number of deletions
        double time_sum = 0.0; // Total time taken

        for (int t = 0; t < T; ++t) { // For each trial

            long start = System.currentTimeMillis();

            // Create a new tree
            int max = (int) (Math.random() * 8) + 2;
            RTree<Entry> tree = new RTree<>(max, 1, 2);

            // Randomly Generate Entries
            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < N; ++i) entries.add(new Entry(Math.random(), Math.random()));

            // Insert all the entries
            for (Entry e : (ArrayList<Entry>) entries.clone()) {
                tree.insert(e);

                // Random chance for deletion
                if (Math.random() < P_d) {
                    // Only inserted entries can be deleted
                    int to_delete = (int)(Math.random() * entries.indexOf(e));
                    tree.delete(entries.get(to_delete));
                    entries.remove(to_delete);

                    // Keep track of deletions
                    ++D;
                }
            }

            // Randomly generate inputs for searching
            Double[] inputs = new Double[]{Math.random(), Math.random(), Math.random(), Math.random()};
            Arrays.sort(inputs);

            // Create Query
            Range[] query = new Range[]{
                    new Range(inputs[0], inputs[2]),
                    new Range(inputs[1], inputs[3])
            };

            // Obtain search result
            List<Entry> result = tree.search(query);

            // Manually filter out the entries not in range
            entries.removeIf(e -> !RTreeNode.isInRange(query, e.getParamValues()));

            // Sorting both of them using the same function to make comparison fair
            result.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));
            entries.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));

            // Assert checker
            assertTrue(isValid(tree.getRoot()), "树有问题");
            assertArrayEquals(entries.toArray(), result.toArray(), "答案不对，你死定了");

            // Trial Finished
            time_sum += System.currentTimeMillis() - start;
        }

        System.out.printf("Average Time taken to perform 1 search, %d insertions, ~%d deletions: %f s\n",
                N,
                D / T,
                time_sum / T / 1000
        );
    }

    /**
     * Asserts that the parent domain covers the child's domain
     */
    boolean isValid(RTreeNode<Entry> root) {
        for (int i = 0; i < 3; ++i) if (root.neighbours[i] != null) {
            Range<Double>[] ranges = ((RTreeNode<Entry>) root.neighbours[i]).getRanges();

            for (int j = 0; j < root.getRanges().length; ++j) {
                if (root.getRanges()[j].getMin() <= ranges[j].getMin() && ranges[j].getMax() <= root.getRanges()[j].getMax()) continue;
                return false;
            }

            if (isValid((RTreeNode<Entry>) root.neighbours[i])) continue;
            return false;
        }
        return true;
    }

}

/**
 * Entry Class to interact with the tree
 */
class Entry implements RTreeEntry {
    Double[] coords;

    public Entry(Double... coords) {this.coords = coords;}

    @Override
    public Double[] getParamValues() {
        return coords;
    }

    @Override
    public String toString() {
        return Arrays.toString(coords);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Entry) || ((Entry) o).coords.length != this.coords.length) return false;
        for (int i = 0; i < coords.length; ++i) {
            if (!Objects.equals(coords[i], ((Entry) o).coords[i])) return false;
        }
        return true;
    }
}
