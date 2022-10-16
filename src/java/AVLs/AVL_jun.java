package AVLs;

import model.Node;

import java.util.ArrayList;

public class AVL_jun<T extends Comparable<? super T>> {
    private Node<T> root;

    // Constructor to construct the AVL tree from a generic array of items
    public AVL_jun(T[] items){
        if (items.length == 0) throw new IllegalArgumentException("Empty item array!");
        root = new Node(items[0], 2);
        for (int i = 1; i < items.length; i++){
            insert(items[i]);
        }
    }

    // Method to perform the right rotation of a subtree with node as the root
    private Node<T> rightRotate(Node<T> node){
        // Complete the code below this comment
        Node<T> left = node.neighbours[0]; // this is the new root
        node.neighbours[0] = left.neighbours[1]; // right subtree is now left of original
        left.neighbours[1] = node; // setting left as the new root
        return left;
    }

    // Method to perform the left rotation of a subtree with node as the root
    private Node<T> leftRotate(Node<T> node){
        // Complete the code below this comment
        Node<T> right = node.neighbours[1]; // this is the new root
        node.neighbours[1] = right.neighbours[0]; // left subtree is now right of original
        right.neighbours[0] = node; // setting right as the new root
        return right;
    }

    // Method to insert an item into an AVL tree
    // Recursive auxiliary method utilised to help return the result
    public void insert(T item){ root = insert(item, root);  }
    private Node<T> insert(T item, Node<T> curr) {
        // Complete the recursive code for insertion below this comment
        if (curr == null) return new Node<>(item, 2); // new node to insert item
        // Now we do the regular BST insertion logic where you check left and right
        int neighbourSide = item.compareTo(curr.getItem()) > 0 ? 1 : 0; // get left or right index
        curr.neighbours[neighbourSide] = insert(item, curr.neighbours[neighbourSide]); // insert on whatever side
        // When we call insert we should always give the root of the balanced subtree
        // So if after insertion we get some imbalance we should balance it then the root will propagate up :))
        return balanceTree(curr);
    }

    // Method to delete an item from an AVL tree
    // Recursive auxiliary method utilised to help return the result
    public void delete(T item){ root = delete(item, root); }
    private Node<T> delete(T item, Node<T> curr){
        // Complete the recursive code for deletion below this comment
        // We first recursively propagate down to find the node to KILL
        if(curr == null) return curr; // when the tree is blank
        if(curr.getItem().compareTo(item) == 0){ // we found the node
            // we have to check if the node is a leaf
            int childSwitch = 0;
            if (curr.neighbours[1] != null) childSwitch++;
            if (curr.neighbours[0] != null) childSwitch+=2;
            switch (childSwitch) {
                case 0 -> {
                    return null; // leaf node momentus
                }
                // it has a subtree on the right only
                case 1 -> curr = curr.neighbours[1]; // just replace it lol
                // it has a subtree on the left only
                case 2 -> curr = curr.neighbours[0]; // just replace it lol
                // it has two children!!! real... so we need the SUCC of curr
                case 3 -> {
                    Node<T> inOrderSucc = curr.neighbours[1];
                    while (inOrderSucc.neighbours[0] != null)
                        inOrderSucc = inOrderSucc.neighbours[0]; // keep going keep going
                    curr.setItem(inOrderSucc.getItem());
                    delete(inOrderSucc.getItem(), curr.neighbours[1]);
                }
            }
        } else {
            int neighbourSide = item.compareTo(curr.getItem()) > 0 ? 1 : 0; // get left or right index
            curr.neighbours[neighbourSide] = delete(item, curr.neighbours[neighbourSide]); // insert on whatever side
        }
        return balanceTree(curr);
    }

    private Node<T> balanceTree(Node<T> curr) {
        int balanceSwitch = getBalance(curr);
        balanceSwitch = Integer.signum( balanceSwitch >= -1 && balanceSwitch <= 1 ? 0 : balanceSwitch); // uhuh
        if(balanceSwitch == 0) return curr; // no balance needed
        if(balanceSwitch > 0) { // base is left
            if(getBalance(curr.neighbours[0]) < 0) curr.neighbours[0] = leftRotate(curr.neighbours[0]); // turn it into a left-left
            return rightRotate(curr);
        } else {
            if(getBalance(curr.neighbours[0]) > 0) curr.neighbours[1] = rightRotate(curr.neighbours[1]); // turn it into a right-right
            return leftRotate(curr);
        }
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
            return find(item, curr.neighbours[0]);
        else if (compareRes > 0)
            return find(item, curr.neighbours[1]);
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
