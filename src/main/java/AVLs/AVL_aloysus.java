package AVLs;

import model.Node;

import java.util.ArrayList;

public class AVL_aloysus<T extends Comparable<? super T>> {
    private Node<T> root;

    // Constructor to construct the AVL tree from a generic array of items
    public AVL_aloysus(T[] items){
        if (items.length == 0) throw new IllegalArgumentException("Empty item array!");
        root = new Node(items[0], 2);
        for (int i = 1; i < items.length; i++){
            insert(items[i]);
        }
    }

    // Method to perform the right rotation of a subtree with node as the root
    private Node<T> rightRotate(Node<T> node){
        Node<T> root = node.neighbours[0];
        node.neighbours[0] = root.neighbours[1];
        root.neighbours[1] = node;
        return root;
    }

    // Method to perform the left rotation of a subtree with node as the root
    private Node<T> leftRotate(Node<T> node){
        Node<T> root = node.neighbours[1];
        node.neighbours[1] = root.neighbours[0];
        root.neighbours[0] = node;
        return root;
    }

    private Node<T> fixTree(Node<T> root) {
        int balance = getBalance(root);

        // left left
        if (balance > 1 && getBalance(root.neighbours[0]) >= 0) return rightRotate(root);
        // right right
        if (balance < -1 && getBalance(root.neighbours[1]) <= 0) return leftRotate(root);
        // left right
        if (balance > 1 && getBalance(root.neighbours[0]) < 0) {
            root.neighbours[0] = leftRotate(root.neighbours[0]);
            return rightRotate(root);
        }
        // right left
        if (balance < -1 && getBalance(root.neighbours[1]) > 0) {
            root.neighbours[1] = rightRotate(root.neighbours[1]);
            return leftRotate(root);
        }

        return root;
    }

    // Method to insert an item into an AVL tree
    // Recursive auxiliary method utilised to help return the result
    public void insert(T item){ root = insert(item, root); }
    private Node<T> insert(T item, Node<T> curr) {
        if (curr == null) return new Node<T>(item, 2);

        if (item.compareTo(curr.getItem()) < 0) {
            curr.neighbours[0] = insert(item, curr.neighbours[0]);
        }
        else if (item.compareTo(curr.getItem()) > 0){
            curr.neighbours[1] = insert(item,curr.neighbours[1]);
        }

        return fixTree(curr);
    }

    // Method to delete an item from an AVL tree
    // Recursive auxiliary method utilised to help return the result
    public void delete(T item){ root = delete(item, root); }
    private Node<T> delete(T item, Node<T> curr){
        if (curr == null) throw new ElementNotFoundException("AVL Tree");

        Node<T> left = curr.neighbours[0];
        Node<T> right = curr.neighbours[1];

        if (item.compareTo(curr.getItem()) < 0) curr.neighbours[0] = delete(item, left);
        else if (item.compareTo(curr.getItem()) > 0) curr.neighbours[1] = delete(item, right);
        else {
            // one or no children
            if (left == null || right == null) {
                int temp;
                temp = (left != null) ? 0 : 1;
                // temp will be null if both children are null
                // if one child exists, temp will become that children.

                curr.setItem(curr.neighbours[temp].getItem());
                curr.neighbours[temp] = null;
            }
            else {
                // replace curr with inorder successor which is the smallest in the right subtree which always exists
                Node<T> temp = getMinNode(right);
                curr.setItem(temp.getItem());
                curr.neighbours[1] = delete(temp.getItem(), right);
            }
        }

        return fixTree(curr);
    }

    private Node<T> getMinNode(Node<T> root) {
        while (root.neighbours[0] != null) {
            root = root.neighbours[0];
        }

        return root;
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
