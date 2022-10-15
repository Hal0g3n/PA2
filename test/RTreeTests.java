import model.*;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ranges.Range;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class RTreeTests {

    @Test
    void SampleTest1() {
        RTree<Entry> tree = new RTree<>(2, 1, 2);
        tree.insert(new Entry(new Double[]{5.0, 5.0}));
        tree.insert(new Entry(new Double[]{9.0, 9.0}));
        tree.insert(new Entry(new Double[]{7.0, 10.0}));
        tree.insert(new Entry(new Double[]{5.0, 11.0}));
        System.out.println();
        System.out.println(displayRTree(tree.getRoot(), 6));
        model.Range[] deleteRanges = new model.Range[2];
        deleteRanges[0] = new model.Range<>(5.0, 10.0);
        deleteRanges[1] = new model.Range<>(7.0, 12.0);
        tree.delete(deleteRanges, new Entry(new Double[]{7.0, 10.0}));
        System.out.println(displayRTree(tree.getRoot(), 3));
        assertEquals(10, 10);
    }
    public static String displayRTree(RTreeNode root, int count) {
        String result = "";
        LinkedList<Object> queue = new LinkedList<>();
        int level = 1;
        int remainingNodes = count;
        int num = 0;
        queue.add(root);
        LinkedList<String> entrylist = new LinkedList<>();
        while (remainingNodes > 0) {
            Object obj = queue.remove();
            if (obj != null) {
                RTreeNode node = (RTreeNode) obj;
                // this node contains something
                result += String.format("%-12s ", Arrays.toString(node.getRanges()) + node.isLeaf());
                if (node.isLeaf()) {
                    entrylist.add(node.getItem().toString());
                } else {
                    queue.add(node.neighbours[0]);
                    queue.add(node.neighbours[1]);
                }
                --remainingNodes;
            } else {
                result += "            ";
                queue.add(null);
                queue.add(null);
            }
            ++num;
            // check for breakline
            if (num >= Math.pow(2, level) - 1) {
                result += "\n";
                ++level;
            }
        }
        result += "\n";
        // entries
        for (String e: entrylist)
            result += e + " ";
        return result;
    }
}

class Entry implements RTreeEntry {
    Double[] coords;

    public Entry(Double[] coords) {
        this.coords = coords;
    }

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
        System.out.println(coords.length);
        for (int i = 0; i < coords.length; ++i) {
            if (!Objects.equals(coords[i], ((Entry) o).coords[i])) return false;
        }
        return true;
    }
}
