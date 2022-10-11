package AVLs;

import java.util.ArrayList;

public class AVL_YX<T extends Comparable<? super T>> {
    private Node<T> root;

    // Constructor to construct the AVL tree from a generic array of items
    public AVL_YX(T[] items) {
        if (items.length == 0) throw new IllegalArgumentException("Empty item array!");
        root = new Node(items[0], 2);
        for (int i = 1; i < items.length; i++) {
            insert(items[i]);
        }
    }

    // Method to perform the right rotation of a subtree with node as the root

    private Node<T> rightRotate(Node<T> root){
        // Find the ejected node
        Node<T> ejected = root.neighbours[0].neighbours[1];

        // Re-root the tree, maintaining link to old root
        root.neighbours[0].neighbours[1] = root;
        root = root.neighbours[0];

        // Root.right now has ejected to the left of it
        root.neighbours[1].neighbours[0] = ejected;

        return root;
    }

    // Method to perform the left rotation of a subtree with node as the root
    private Node<T> leftRotate(Node<T> root) {
        // Find the ejected node
        Node<T> ejected = root.neighbours[1].neighbours[0];

        // Re-root the tree, maintaining link to old root
        root.neighbours[1].neighbours[0] = root;
        root = root.neighbours[1];

        // Root.left now has ejected to the right of it
        root.neighbours[0].neighbours[1] = ejected;

        return root;
    }

    // Method to get the minimum in a subtree with node as the root
    private T getMin(Node<T> node) {return node.neighbours[0] == null ? node.getItem() : getMin(node.neighbours[0]);}

    // Method to balance subtree about root (intellij complained about repeated code)
    // Only one rotation to the root will occur
    private Node<T> balanceSubTree(Node<T> root) {
        // Left subtree too tall
        if (getBalance(root) > 1) {
            // If Left-Right ==convert to=> Left-Left
            if (getBalance(root.neighbours[0]) < 0) root.neighbours[0] = leftRotate(root.neighbours[0]);

            // Left-Left => Balanced
            root = rightRotate(root);
        }

        // Right subtree to tall
        else if (getBalance(root) < -1) {
            // If Right-Left ==convert to=> Right-Right
            if (getBalance(root.neighbours[0]) > 0) root.neighbours[1] = rightRotate(root.neighbours[1]);

            // Right-Right => Balanced
            root = leftRotate(root);
        }

        return root;
    }

    // Method to insert an item into an AVL tree
    // Recursive auxiliary method utilised to help return the result
    public void insert(T item){ root = insert(new Node(item, 2), root);  }
    private Node<T> insert(Node<T> item, Node<T> root) {
        // Reached Insertion Point: Let root be item to trigger re-balancing
        if (root == null) root = item;

        // Recurse down to point of insertion
        if (item.getItem().compareTo(root.getItem()) < 0) root.neighbours[0] = insert(item, root.neighbours[0]);
        if (item.getItem().compareTo(root.getItem()) > 0) root.neighbours[1] = insert(item, root.neighbours[1]);

        // Balance Tree and return up
        return balanceSubTree(root);
    }

    // Method to delete an item from an AVL tree
    // Recursive auxiliary method utilised to help return the result
    public void delete(T item){ root = delete(item, root); }
    private Node<T> delete(T item, Node<T> root) {

        // Corner case: if item does not exist (will hit null node)
        if (root == null) return null;

        // Recurse down only if you haven't found yet
        if (root.getItem().compareTo(item) > 0) root.neighbours[0] = delete(item, root.neighbours[0]);
        if (root.getItem().compareTo(item) < 0) root.neighbours[1] = delete(item, root.neighbours[1]);

        // If it is node to remove
        if (root.getItem().compareTo(item) == 0) {
            // Right subtree does not exist, just attach left subtree
            if (root.neighbours[1] == null) root = root.neighbours[0];

            // Left subtree does not exist, just attach right subtree
            else if (root.neighbours[0] == null) root = root.neighbours[1];

            // Both subtrees exist replace with inorder successor
            else {
                // Substitute the in-order successor's value
                root.setItem(getMin(root.neighbours[1]));

                // Delete the actual in-order successor node
                root.neighbours[1] = delete(root.getItem(), root.neighbours[1]);
            }
        }

        // Balance Tree and return up
        return balanceSubTree(root);
    }

    //---------------- DO NOT EDIT THE FOLLOWING METHODS ---------------------
    // You may use the following methods as you see fit to help you out

    // Obtain the balance factor of a given node in the AVL tree
    public int getBalance(Node<T> node){
        if (node == null) return 0;
        return height(node.neighbours[0]) - height(node.neighbours[1]);
    }

    // Obtain the height the subtree from node (the subtree's root)
    public int height(Node<T> node){
        if (node == null) return 0;
        else{
            int leftDepth = height(node.neighbours[0]);
            int rightDepth = height(node.neighbours[1]);
            if (leftDepth > rightDepth)
                return (leftDepth + 1);
            else
                return (rightDepth + 1);
        }
    }
    // Accessor to return the root of the AVL tree
    public Node<T> getRoot(){ return root; }

    // Retrieve the item stored in the AVL tree
    // Recursive auxiliary method utilised to help return the result
    public T find(T item){ return find(item, root); }
    private T find(T item, Node<T> curr){
        if (curr == null) return null;
        int compareRes = item.compareTo(curr.getItem());
        if (compareRes < 0)
            return (T) find(item, curr.neighbours[0]);
        else if (compareRes > 0)
            return (T) find(item, curr.neighbours[1]);
        else
            return curr.getItem();
    }

    // Return the ArrayList of items ordered through in-order traversal
    // Recursive auxiliary method utilised to help return the result
    public ArrayList<T> inOrder(){
        ArrayList<T> result = new ArrayList<>();
        inOrder(root, result);
        return result;
    }
    private void inOrder(Node<T> curr, ArrayList<T> result){
        if (curr != null) {
            inOrder(curr.neighbours[0], result);
            result.add(curr.getItem());
            inOrder(curr.neighbours[1], result);
        }
    }

    // Return the ArrayList of items ordered through pre-order traversal
    // Recursive auxiliary method utilised to help return the result
    public ArrayList<T> preOrder(){
        ArrayList<T> result = new ArrayList<>();
        preOrder(root, result);
        return result;
    }
    private void preOrder(Node<T> curr, ArrayList<T> result){
        if (curr != null) {
            result.add(curr.getItem());
            preOrder(curr.neighbours[0], result);
            preOrder(curr.neighbours[1], result);
        }
    }

    // Return the ArrayList of items ordered through post-order traversal
    // Recursive auxiliary method utilised to help return the result
    public ArrayList<T> postOrder(){
        ArrayList<T> result = new ArrayList<>();
        postOrder(root, result);
        return result;
    }
    private void postOrder(Node<T> curr, ArrayList<T> result){
        if (curr != null) {
            postOrder(curr.neighbours[0], result);
            postOrder(curr.neighbours[1], result);
            result.add(curr.getItem());
        }
    }
}
