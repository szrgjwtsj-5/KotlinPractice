package test;

import java.util.*;

public class TreeTest {
    public static class TreeNode {
        public int val;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }
    }

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        TreeNode l = new TreeNode(2);
        TreeNode r = new TreeNode(3);
        TreeNode ll = new TreeNode(4);
        TreeNode rl = new TreeNode(5);
        TreeNode rr = new TreeNode(6);
        TreeNode llr = new TreeNode(7);
        TreeNode llrr = new TreeNode(8);

        root.left = l;
        root.right = r;
        l.left = ll;
        r.left = rl;
        r.right = rr;
        ll.right = llr;
        llr.right = llrr;

//        layerVisit(root).forEach(layer -> {
//            layer.forEach(item -> System.out.print(item + " "));
//            System.out.println();
//        });
//
//        zhiPrintTree(root).forEach(layer -> {
//            layer.forEach(item -> System.out.print(item + " "));
//            System.out.println();
//        });
        infixVisit(root);
        System.out.println(findDepth2(root));
    }

    private static int findDepth(TreeNode root) {       // 递归计算二叉树深度
        if (root == null) return 0;
        if (root.left == null && root.right == null) return 1;

        return Math.max(findDepth(root.left), findDepth(root.right)) + 1;
    }

    private static int findDepth2(TreeNode root) {
        if (root == null) return 0;
        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        int res = 0;
        while (!queue.isEmpty()) {
            int layerSize = queue.size();
            for (int i = 0; i < layerSize; i++) {
                TreeNode tmp = queue.poll();
                if (tmp.left != null) {
                    queue.offer(tmp.left);
                }
                if (tmp.right != null) {
                    queue.offer(tmp.right);
                }
            }
            res++;
        }
        return res;
    }

    private static List<List<Integer>> layerVisit(TreeNode root) {          // 层次遍历并将每一层保存成list
        if (root == null) return null;
        List<List<Integer>> res = new ArrayList<>();
        Queue<TreeNode> queue = new ArrayDeque<>();

        queue.offer(root);

        while (!queue.isEmpty()) {
            List<Integer> layer = new ArrayList<>();
            int layerSize = queue.size();
            for (int i = 0; i < layerSize; i++) {
                TreeNode tmp = queue.poll();
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

    // 之字形打印二叉树，其实就是在上面层次遍历基础上的改造
    public static List<List<Integer>> zhiPrintTree(TreeNode root) {
        List<List<Integer>> layers = new ArrayList<>();
        if (root == null) return layers;

        Queue<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        boolean rev = true;

        while (!queue.isEmpty()) {
            int layerSize = queue.size();
            List<Integer> layer = new ArrayList<>();
            for (int i = 0; i < layerSize; i++) {
                TreeNode tmp = queue.poll();
                if (tmp == null) continue;
                if (rev) {
                    layer.add(tmp.val);
                } else {
                    layer.add(0, tmp.val);
                }
                if (tmp.left != null) {
                    queue.offer(tmp.left);
                }
                if (tmp.right != null) {
                    queue.offer(tmp.right);
                }
            }
            layers.add(layer);
            rev = !rev;
        }
        return layers;
    }

    // 二叉树中序遍历
    private static void infixVisit(TreeNode root) {
        if (root == null) return;
        Stack<TreeNode> st = new Stack<>();

        TreeNode cur = root;

        while (!st.isEmpty() || cur != null) {
            if (cur != null) {
                st.push(cur);
                cur = cur.left;
            } else {
                cur = st.pop();
                System.out.print(cur.val + " ");
                cur = cur.right;
            }
        }
        System.out.println();
    }
}
