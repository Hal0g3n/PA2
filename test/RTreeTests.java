import model.RTree;
import model.RTreeEntry;
import model.RTreeNode;
import model.Range;
import org.junit.jupiter.api.Test;

import static java.lang.Math.signum;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;


public class RTreeTests {

    @Test
    void SampleTest1() {
        RTree<Entry> tree = new RTree<>(2, 1, 2);
        tree.insert(new Entry(5.0, 5.0));
        tree.insert(new Entry(9.0, 9.0));
        tree.insert(new Entry(7.0, 10.0));
        tree.insert(new Entry(5.0, 11.0));
        System.out.println();
        System.out.println(displayRTree(tree.getRoot()));
        tree.delete(new Entry(7.0, 10.0));
        System.out.println(displayRTree(tree.getRoot()));
    }

    /**
     * Should return everything as it covers all possible coordinates
     */
    @Test
    void SearchTest() { // Test 3 in our report btw
        RTree<Entry> tree = new RTree<>(2, 1, 2);
        Entry[] entries = new Entry[]{
                new Entry(5.0, 5.0),
                new Entry(9.0, 9.0),
                new Entry(7.0, 10.0),
                new Entry(5.0, 11.0)
        };

        for (Entry e: entries) tree.insert(e);

        List<Entry> result = tree.search(new Range[]{
                new Range(Double.MIN_VALUE, Double.MAX_VALUE),
                new Range(Double.MIN_VALUE, Double.MAX_VALUE)
        });

        System.out.println(displayRTree(tree.getRoot()));
        // Sorting both of them using the same function to make comparison fair
        result.sort((a, b) -> (int) (!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));
        Arrays.sort(entries, (a, b) -> (int) (!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));

        // Assert checker
        assertArrayEquals(result.toArray(), entries);
    }

    /**
     * Should return everything as it covers all possible coordinates
     */
    @Test
    void GenericTest() { // A test to confirm that insertion, deletion and search works in general
        int T = 100; // Number of Trials
        int N = 10; // Number of random entries test

        while (T-- > 0) { // For each trial

            // Create a new tree
            RTree<Entry> tree = new RTree<>((int) (Math.random() * 8) + 2, 1, 2);

            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < N; ++i) entries.add(new Entry(Math.random(), Math.random()));

            for (Entry e : entries) tree.insert(e);

            Double[] inputs = new Double[]{Math.random(), Math.random(),Math.random(), Math.random()};
            Arrays.sort(inputs);

            Range[] query = new Range[]{
                    new Range(inputs[0], inputs[2]),
                    new Range(inputs[1], inputs[3])
            };

            List<Entry> result = tree.search(query);

            // Manually filter out the entries not in range
            entries.removeIf(e -> !RTreeNode.isInRange(query, e.getParamValues()));

            // Sorting both of them using the same function to make comparison fair
            result.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));
            entries.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));

            System.out.println("===========================================================");
            System.out.printf("(%f %f) (%f %f)\n", inputs[0], inputs[2], inputs[1], inputs[3]);
            System.out.println(entries);
            System.out.println(result);

            if (result.size() != entries.size()) {
                System.out.println();
                System.out.println(displayRTree(tree.getRoot()));
                tree.search(query);
            }

            // Assert checker
            assertArrayEquals(entries.toArray(), result.toArray());
        }
    }

    public static String displayRTree(RTreeNode root) {
        StringBuilder result = new StringBuilder();
        LinkedList<Object> queue = new LinkedList<>();
        int level = 1;
        int inNodes = 1;
        int num = 0;
        queue.add(root);
        LinkedList<String> entrylist = new LinkedList<>();
        while (inNodes > 0) {
            Object obj = queue.remove();
            if (obj != null) {
                --inNodes;
                RTreeNode node = (RTreeNode) obj;
                // this node contains something
                result.append(String.format("%-12s ", Arrays.toString(node.getRanges()) + node.isLeaf() + node.getId().size()));
                if (node.isLeaf()) {
                    entrylist.add(node.getItem().toString());
                } else {
                    queue.add(node.neighbours[0]);
                    queue.add(node.neighbours[1]);

                    inNodes += node.neighbours[0] == null ? 0 : 1;
                    inNodes += node.neighbours[1] == null ? 0 : 1;
                }
            } else {
                result.append("            ");

                queue.add(null);
                queue.add(null);
            }
            ++num;
            // check for breakline
            if (num >= Math.pow(2, level) - 1) {
                result.append("\n");
                ++level;
            }
        }
        result.append("\n");

        // entries
        for (String e: entrylist)
            result.append(e).append(" ");

        return result.toString();
    }
}

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
