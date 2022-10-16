package model;

import io.netty.util.concurrent.Promise;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

// This class will relay information from the RTree to related RTrees
public class GhostNode<T extends RTreeEntry> extends RTreeNode<T> {

  BitSet owner_id;

  public GhostNode(List<T> item, Range<Double>[] ranges) {
    super(item, ranges);
  }

  public Promise<List<T>> propoSearch(
    RTreeNode<T> n,
    Range<Double>[] ranges,
    LinkedList<T> results
  ) {
    // TODO: propagate the search here
    return null;
  }

  public void propoInsert(GhostNode<T> inserted) {
    // TODO: propagate inserted node here
  }

  public void propoDelete(GhostNode<T> n) {
    // TODO: propagate the deletion here
  }

  public Promise<RTreeNode<T>> retrieveSubtree() {
    // TODO: Retrieve the entire subtree rooted at (this)
    return null;
  }
}
