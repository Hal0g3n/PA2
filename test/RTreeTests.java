import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import model.RTree;
import model.RTreeEntry;
import model.RTreeNode;
import model.Range;
import org.junit.jupiter.api.Test;

public class RTreeTests {

  @Test
  void SampleTest1() {
    RTree<Entry> tree = new RTree<>(2, 1, 2);
    tree.insert(new Entry(new Double[] { 5.0, 5.0 }));
    tree.insert(new Entry(new Double[] { 9.0, 9.0 }));
    tree.insert(new Entry(new Double[] { 7.0, 10.0 }));
    tree.insert(new Entry(new Double[] { 5.0, 11.0 }));
    System.out.println();
    System.out.println(displayRTree(tree.getRoot(), 6));
    tree.delete(new Entry(new Double[] { 7.0, 10.0 }));
    System.out.println(displayRTree(tree.getRoot(), 3));
  }

  /**
   * Should return everything as it covers all possible coordinates
   */
  @Test
  void SearchTest() { // Test 3 in our report btw
    RTree<Entry> tree = new RTree<>(2, 1, 2);
    Entry[] entries = new Entry[] {
      new Entry(new Double[] { 5.0, 5.0 }),
      new Entry(new Double[] { 9.0, 9.0 }),
      new Entry(new Double[] { 7.0, 10.0 }),
      new Entry(new Double[] { 5.0, 11.0 }),
    };

    for (Entry e : entries) tree.insert(e);

    List<Entry> result = tree.search(
      new Range[] {
        new Range(Double.MIN_VALUE, Double.MAX_VALUE),
        new Range(Double.MIN_VALUE, Double.MAX_VALUE),
      }
    );

    // Sorting both of them using the same function to make comparison fair
    result.sort((a, b) ->
      (int) (
        !Objects.equals(a.coords[0], b.coords[0])
          ? a.coords[0] - b.coords[0]
          : a.coords[1] - b.coords[1]
      )
    );
    Arrays.sort(
      entries,
      (a, b) ->
        (int) (
          !Objects.equals(a.coords[0], b.coords[0])
            ? a.coords[0] - b.coords[0]
            : a.coords[1] - b.coords[1]
        )
    );

    // Assert checker
    assertArrayEquals(result.toArray(), entries);
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
        result +=
          String.format(
            "%-12s ",
            Arrays.toString(node.getRanges()) + node.isLeaf()
          );
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
    for (String e : entrylist) result += e + " ";
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
    if (
      !(o instanceof Entry) || ((Entry) o).coords.length != this.coords.length
    ) return false;
    for (int i = 0; i < coords.length; ++i) {
      if (!Objects.equals(coords[i], ((Entry) o).coords[i])) return false;
    }
    return true;
  }
}
