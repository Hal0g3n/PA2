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
        BitSet b = new BitSet();
        b.set(0);
        b.set(1);
        System.out.println(b.size() + " " + b.length());
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
        RTree<Entry> tree = new RTree<>(3, 1, 2);
        List<Entry> entries = new ArrayList<>(Arrays.stream(new Entry[]{
                new Entry(125.41922129152239, 43.514730998242015),
                new Entry(56.12201545778988, 14.154840149180226),
                new Entry(28.098961338919317, 4.680292860949331),
                new Entry(88.70213270698586, 1.7895029743391966),
                new Entry(34.26605552077395, 123.83928817436494),
                new Entry(128.70232515169283, 51.67062093482044),
        }).toList());

        for (Entry e: entries) tree.insert(e);

//            Double[] inputs = new Double[]{Math.random(), Math.random(),Math.random(), Math.random()};
        Double[] inputs = new Double[]{Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE};

        Range[] query = new Range[]{
                new Range(inputs[0], inputs[1]),
                new Range(inputs[2], inputs[3])
        };

        List<Entry> result = tree.search(query);

        entries.removeIf(e -> !RTreeNode.isInRange(query, e.getParamValues()));

        System.out.println(displayRTree(tree.getRoot()));
        // Sorting both of them using the same function to make comparison fair
        result.sort((a, b) -> (int) (!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));
        entries.sort((a, b) -> (int) (!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));

        System.out.println(result);
        System.out.println(entries);

        // Assert checker
        assertArrayEquals(result.toArray(), entries.toArray());
    }

    /**
     * Should return everything as it covers all possible coordinates
     */
    @Test
    void GenericTest() { // A test to confirm that insertion, deletion and search works in general
        int T = 100; // Number of Trials
        int N = 7; // Number of random entries test

        while (T-- > 0) { // For each trial

            // Create a new tree
            int max = (int) (Math.random() * 8) + 2;
            RTree<Entry> tree = new RTree<>(max, 1, 2);

            ArrayList<Entry> entries = new ArrayList<>();
            for (int i = 0; i < N; ++i) entries.add(new Entry(Math.random(), Math.random()));

            for (Entry e : entries) {
                System.out.println(e.toString());
                tree.insert(e);
            }

            Double[] inputs = new Double[]{Math.random(), Math.random(), Math.random(), Math.random()};
            Arrays.sort(inputs);

            Range[] query = new Range[]{
                    new Range(inputs[0], inputs[2]),
                    new Range(inputs[1], inputs[3])
            };

            List<Entry> result = tree.search(query);

            System.out.println("===========================================================");
            System.out.printf("(%f %f) (%f %f)\n", inputs[0], inputs[2], inputs[1], inputs[3]);

            System.out.println(max);
            // Manually filter out the entries not in range
            entries.removeIf(e -> !RTreeNode.isInRange(query, e.getParamValues()));

            System.out.println(entries);

            // Sorting both of them using the same function to make comparison fair
            result.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));
            entries.sort((a, b) -> (int) signum(!Objects.equals(a.coords[0], b.coords[0]) ? a.coords[0] - b.coords[0] : a.coords[1] - b.coords[1]));

            System.out.println(result);

            if (!entries.equals(result)) {
                System.out.println();
                System.out.println(displayRTree(tree.getRoot()));
                tree.search(query);
            }

            // Assert checker
            assertArrayEquals(entries.toArray(), result.toArray());
        }
    }

    @Test
    void test() {
        RTree<Entry> tree = new RTree<>(2, 1, 2);

        Entry[] entries = new Entry[]{new Entry(14.331492525120403, 101.79081038448486), new Entry(37.78513474119687, 135.19716702477984), new Entry(55.60313755770421, 114.83302285323091), new Entry(94.83076232997962, 82.81084758601448), new Entry(134.54380553478188, 98.71128097470205), new Entry(136.68742810762413, 50.57049866534822), new Entry(169.1266474492845, 101.64010354234323)};
        for (Entry e : entries) tree.insert(e);

        System.out.println(displayRTree(tree.getRoot()));
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
                result.append(String.format("%-12s ", Arrays.toString(node.getRanges()) + node.isLeaf() + node.getId().length));
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

//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof Entry) || ((Entry) o).coords.length != this.coords.length) return false;
//        for (int i = 0; i < coords.length; ++i) {
//            if (!Objects.equals(coords[i], ((Entry) o).coords[i])) return false;
//        }
//        return true;
//    }
}
