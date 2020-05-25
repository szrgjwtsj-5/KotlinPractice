package test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class AlTest {

    public static class Node {
        public int val;
        public Node left;
        public Node right;

        public Node(int val) {
            this.val = val;
        }
    }

    public static void main(String[] args) {
        Node root = new Node(1);
        Node l = new Node(2);
        Node r = new Node(3);
        Node ll = new Node(4);
        Node rl = new Node(5);
        Node rr = new Node(6);
        Node llr = new Node(7);
        Node llrr = new Node(8);

        root.left = l; root.right = r;
        l.left = ll; r.left = rl; r.right = rr;
        ll.right = llr; llr.right = llrr;

//        System.out.println(findDepth2(root));

        layerVisit(root).forEach(layer -> {
            layer.forEach(item -> System.out.print(item + " "));
            System.out.println();
        });
    }

    private static int findDepth(Node root) {       // 递归计算二叉树深度
        if (root == null) return 0;
        if (root.left == null && root.right == null) return 1;

        return Math.max(findDepth(root.left), findDepth(root.right)) + 1;
    }

    private static int findDepth2(Node root) {
        return layerVisit(root).size();
    }

    private static List<List<Integer>> layerVisit(Node root) {          // 层次遍历并将每一层保存成list
        if (root == null) return null;
        List<List<Integer>> res = new ArrayList<>();
        Queue<Node> queue = new ArrayDeque<>();

        queue.offer(root);

        while (!queue.isEmpty()) {
            List<Integer> layer = new ArrayList<>();
            int layerSize = queue.size();
            for (int i = 0; i < layerSize; i++) {
                Node tmp = queue.poll();
                layer.add(tmp.val);

                if (tmp.left != null) {
                    queue.offer(tmp.left);
                }
                if (tmp.right != null) {
                    queue.offer(tmp.right);
                }
            }
            res.add(layer);
        }
        return res;
    }
}
