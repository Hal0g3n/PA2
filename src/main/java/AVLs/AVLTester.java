package AVLs;

import AVLs.AVL_YX;

public class AVLTester {
    public static void main(String[] args) {
        Integer[] arr = {9, 5, 10, 0, 6, 11, -1, 1, 2};
//        Integer[] arr = {1,2,3,4,5,6,10};
        AVL_aloysus<Integer> avl = new AVL_aloysus<>(arr);
        System.out.println("Height of BST: " + avl.height(avl.getRoot()));
        System.out.println(avl.preOrder());

        avl.delete(10);
        System.out.println(avl.preOrder());
    }
}
