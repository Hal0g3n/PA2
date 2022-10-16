package AVLs;

import java.util.ArrayList;
import model.Node;

public class AVL_PC<T extends Comparable<? super T>> {

  private Node<T> root;

  // Constructor to construct the AVL tree from a generic array of items
  public AVL_PC(T[] items) {
    if (items.length == 0) throw new IllegalArgumentException(
      "Empty item array!"
    );
    root = new Node(items[0], 2);
    for (int i = 1; i < items.length; i++) {
      insert(items[i]);
    }
  }

  // Method to perform the right rotation of a subtree with node as the root
  private Node<T> rightRotate(Node<T> node) {
    // Complete the code below this comment
    Node<T> leftChild = node.neighbours[0];
    node.neighbours[0] = leftChild.neighbours[1];
    leftChild.neighbours[1] = node;
    return leftChild;
  }

  // Method to perform the left rotation of a subtree with node as the root
  private Node<T> leftRotate(Node<T> node) {
    // Complete the code below this comment
    Node<T> rightChild = node.neighbours[1];
    node.neighbours[1] = rightChild.neighbours[0];
    rightChild.neighbours[0] = node;
    return rightChild;
  }

  // Method to insert an item into an AVL tree
  // Recursive auxiliary method utilised to help return the result
  public void insert(T item) {
    root = insert(item, root);
  }

  private Node<T> insert(T item, Node<T> curr) {
    // corner case for empty tree
    if (curr == null) return new Node<T>(item, 2);
    // determine insert on left or right
    int insertSide = 0;
    if (((Comparable) item).compareTo(curr.getItem()) > 0) {
      insertSide = 1;
    }

    // base case: no nodes on that side
    if (curr.neighbours[insertSide] == null) {
      curr.neighbours[insertSide] = new Node<T>(item, 2);
    } else {
      curr.neighbours[insertSide] = insert(item, curr.neighbours[insertSide]);

      // rebalancing
      if (getBalance(curr) < -1 || getBalance(curr) > 1) {
        if (insertSide == 0) {
          if (((Comparable) item).compareTo(curr.neighbours[0].getItem()) > 0) {
            // left-right case
            curr.neighbours[0] = leftRotate(curr.neighbours[0]);
          }
          // left-left case or second rotation of left-right case
          curr = rightRotate(curr);
        } else {
          if (
            ((Comparable) item).compareTo(curr.neighbours[0].getItem()) <= 0
          ) {
            // right-left case
            curr.neighbours[1] = rightRotate(curr.neighbours[1]);
          }
          // right-right case or second rotation of right-left case
          curr = leftRotate(curr);
        }
      }
    }

    return curr;
  }

  // Method to delete an item from an AVL tree
  // Recursive auxiliary method utilised to help return the result
  public void delete(T item) {
    root = delete(item, root);
  }

  private Node<T> delete(T item, Node<T> curr) {
    // corner case for empty tree
    if (curr == null) return null;
    // Complete the recursive code for deletion below this comment
    if (curr.getItem() == item) {
      // the current node has less than 2 children
      if (curr.neighbours[0] == null) return curr.neighbours[1]; else if (
        curr.neighbours[1] == null
      ) return curr.neighbours[0]; // if they are both null then null will be returned

      // the current node has 2 children: find inorder successor
      Node<T> inOrderSuccessor = curr.neighbours[1];
      Node<T> parent = curr;
      while (inOrderSuccessor.neighbours[0] != null) {
        parent = inOrderSuccessor;
        inOrderSuccessor = inOrderSuccessor.neighbours[0];
      }

      // replace the current value with the inOrderSuccessor
      curr.setItem(inOrderSuccessor.getItem());
      // delete the inorder successor
      if (parent == curr) {
        parent.neighbours[1] = null;
      } else parent.neighbours[0] = null;

      // balance check: here it is always either left-right or left-left
      if (getBalance(curr) > 1) {
        if (getBalance(curr.neighbours[1]) < 0) curr.neighbours[1] =
          leftRotate(curr.neighbours[1]);
        // left-left or second rotation of left-right
        curr = rightRotate(curr);
      }
    } else {
      // recursively search depending on which side
      if (((Comparable) item).compareTo(curr.getItem()) < 0) {
        curr.neighbours[0] = delete(item, curr.neighbours[0]);
      } else {
        curr.neighbours[1] = delete(item, curr.neighbours[1]);
      }

      // balance check
      if (getBalance(curr) > 1) {
        if (getBalance(curr.neighbours[1]) < 0) curr.neighbours[1] =
          leftRotate(curr.neighbours[1]);
        // left-left or second rotation of left-right
        curr = rightRotate(curr);
      } else if (getBalance(curr) < 1) {
        if (getBalance(curr.neighbours[0]) > 0) curr.neighbours[0] =
          rightRotate(curr.neighbours[0]);
        curr = leftRotate(curr);
      }
    }
    return curr;
  }

  //---------------- DO NOT EDIT THE FOLLOWING METHODS ---------------------
  // You may use the following methods as you see fit to help you out

  // Obtain the balance factor of a given node in the AVL tree
  public int getBalance(Node<T> node) {
    if (node == null) return 0;
    return height(node.neighbours[0]) - height(node.neighbours[1]);
  }

  // Obtain the height the subtree from node (the subtree's root)
  public int height(Node<T> node) {
    if (node == null) return 0; else {
      int leftDepth = height(node.neighbours[0]);
      int rightDepth = height(node.neighbours[1]);
      if (leftDepth > rightDepth) return (leftDepth + 1); else return (
        rightDepth + 1
      );
    }
  }

  // Accessor to return the root of the AVL tree
  public Node<T> getRoot() {
    return root;
  }

  // Retrieve the item stored in the AVL tree
  // Recursive auxiliary method utilised to help return the result
  public T find(T item) {
    return find(item, root);
  }

  private T find(T item, Node<T> curr) {
    if (curr == null) return null;
    int compareRes = item.compareTo(curr.getItem());
    if (compareRes < 0) return (T) find(item, curr.neighbours[0]); else if (
      compareRes > 0
    ) return (T) find(item, curr.neighbours[1]); else return curr.getItem();
  }

  // Return the ArrayList of items ordered through in-order traversal
  // Recursive auxiliary method utilised to help return the result
  public ArrayList<T> inOrder() {
    ArrayList<T> result = new ArrayList<>();
    inOrder(root, result);
    return result;
  }

  private void inOrder(Node<T> curr, ArrayList<T> result) {
    if (curr != null) {
      inOrder(curr.neighbours[0], result);
      result.add(curr.getItem());
      inOrder(curr.neighbours[1], result);
    }
  }

  // Return the ArrayList of items ordered through pre-order traversal
  // Recursive auxiliary method utilised to help return the result
  public ArrayList<T> preOrder() {
    ArrayList<T> result = new ArrayList<>();
    preOrder(root, result);
    return result;
  }

  private void preOrder(Node<T> curr, ArrayList<T> result) {
    if (curr != null) {
      result.add(curr.getItem());
      preOrder(curr.neighbours[0], result);
      preOrder(curr.neighbours[1], result);
    }
  }

  // Return the ArrayList of items ordered through post-order traversal
  // Recursive auxiliary method utilised to help return the result
  public ArrayList<T> postOrder() {
    ArrayList<T> result = new ArrayList<>();
    postOrder(root, result);
    return result;
  }

  private void postOrder(Node<T> curr, ArrayList<T> result) {
    if (curr != null) {
      postOrder(curr.neighbours[0], result);
      postOrder(curr.neighbours[1], result);
      result.add(curr.getItem());
    }
  }
}
