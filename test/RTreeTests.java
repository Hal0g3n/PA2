import model.RTree;
import model.RTreeEntry;
import model.RTreeNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class RTreeTests {

    @Test
    void SampleTest1() {
        RTree<Entry> tree = new RTree<>(3, 1, 2);
        tree.insert(new Entry(new Double[]{5.0, 5.0}));
        tree.insert(new Entry(new Double[]{9.0, 9.0}));
        tree.insert(new Entry(new Double[]{7.0, 10.0}));
        tree.insert(new Entry(new Double[]{5.0, 12.0}));
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
        LinkedList<RTreeEntry> entrylist = new LinkedList<>();
        while (remainingNodes > 0) {
            Object obj = queue.remove();
            if (obj != null) {
                RTreeNode node = (RTreeNode) obj;
                // this node contains something
                result += String.format("%-12s ", Arrays.toString(node.getRanges()));
                if (node.isLeaf()) {
                    entrylist.addAll((Collection<? extends RTreeEntry>) node.getItem());
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
        // entries
        for (RTreeEntry e: entrylist)
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
}
