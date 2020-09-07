package test;

public class ListTest {
    static class LinkNode {
        public int val;
        public LinkNode next;

        public LinkNode(int val) {
            this.val = val;
        }
    }

    public static void main(String[] args) {
        LinkNode head = new LinkNode(1);
        LinkNode l1 = new LinkNode(2);
        LinkNode l2 = new LinkNode(3);
        LinkNode l3 = new LinkNode(5);
        LinkNode l4 = new LinkNode(5);
        LinkNode l5 = new LinkNode(6);
        head.next = l1; l1.next = l2; l2.next = l3; l3.next = l4; l4.next = l5;

        LinkNode head1 = new LinkNode(0);
        LinkNode n1 = new LinkNode(2);
        head1.next = n1; n1.next = l3;

//        printLinkList(deleteDup(head));
//        printLinkList(head);
//        printLinkList(reverseList(head));

        System.out.println(findCommonNode(head, head1).val);
    }
    private static void printLinkList(LinkNode head) {
        LinkNode h = head;
        while (h != null) {
            System.out.print(h.val + " -> ");
            h = h.next;
        }
        System.out.println("null");
    }

    //删除链表重复节点
    public static LinkNode deleteDup(LinkNode head) {
        LinkNode h = new LinkNode(-1);
        h.next = head;
        LinkNode pre = h, cur = head;

        while (cur != null) {
            if (cur.next != null && cur.next.val == cur.val) {
                // 找到当前重复节点的最后一个节点
                while (cur.next != null && cur.next.val == cur.val) {
                    cur = cur.next;
                }
                // 让pre 的next 指向重复节点的下一个节点
                pre.next = cur.next;

                if (cur.next == null) {     // 这个情况说明链表是以重复节点结尾的，可以直接返回
                    return h.next;
                }
            } else {
                // 当节点不重复时，pre 后移
                pre = cur;
            }
            cur = cur.next;
        }
        return h.next;
    }

    // 翻转链表
    public static LinkNode reverseList(LinkNode head) {
        LinkNode pre = null, cur = head, next;
        while (cur != null) {
            next = cur.next;
            cur.next = pre;
            pre = cur;
            cur = next;
        }
        return pre;
    }

    public static LinkNode findCommonNode(LinkNode head1, LinkNode head2) {
        if (head1 == null || head2 == null) return null;
        LinkNode p1 = head1, p2 = head2;

        while (p1 != p2) {
            p1 = p1 == null ? head2 : p1.next;
            p2 = p2 == null ? head1 : p2.next;
        }
        return p1;
    }
}
