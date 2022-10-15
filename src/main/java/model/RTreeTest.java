package model;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;

public class RTreeTest {

    public static void main(String[] args) {
        RTree<Entry> tree = new RTree(5, 2, 2);
        tree.insert(new Entry(new double[]{50, 50}));
        displayRTree(tree, 1);
    }

    public static void displayRTree(RTree root, int count) {
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
                result += String.format("%-12s ", node.getRanges().toString());
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
            result += e.getParamValues().toString() + " ";
        System.out.println(result);
    }
}

class Entry implements RTreeEntry {
    double[] coords;

    public Entry(double[] coords) {
        this.coords = coords;
    }

    @Override
    public double[] getParamValues() {
        return coords;
    }
}
