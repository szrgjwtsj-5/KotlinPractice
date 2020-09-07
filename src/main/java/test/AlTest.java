package test;

import java.util.*;

public class AlTest {
    public static void main(String[] args) {
        int[] nums = {2, 3, 4, 2, 6, 2, 5, 1};
        int[][] mulNums = {{1, 2, 3, 4}, {3, 4, 5, 6}, {5, 6, 7, 8}, {7, 8, 9, 10}};
        int[][] n = {{1, 2}, {3, 4}, {5, 6}, {7, 8}};
        int[] sn = {6,-3,-2,7,-15,19,2,2};

        AlTest test = new AlTest();
//        test.maxInWindows(nums, 8).forEach(it -> System.out.print(it + ", "));
//        System.out.println(test.find(mulNums, 11));

//        test.findMinK(nums, 3).forEach(it -> System.out.print(it + ", "));

//        System.out.println(test.findKthMax(nums, 2));

//        test.rotatePrint(n).forEach(it -> System.out.print(it + ", "));
//        System.out.println(test.findMaxSubArray(sn));

        System.out.println(test.inversePair(nums));
    }

    public ArrayList<Integer> maxInWindows(int[] nums, int size) {

        ArrayList<Integer> res = new ArrayList<>();
        if (nums == null || size <= 0 || size > nums.length) return res;

        for (int i = 0; i <= nums.length - size; i++) {
            res.add(findMax(nums, i, i + size));
        }
        return res;
    }

    private int findMax(int[] nums, int begin, int end) {
        int res = Integer.MIN_VALUE;
        for (int i = begin; i < end; i++) {
            if (nums[i] > res) {
                res = nums[i];
            }
        }
        return res;
    }

    // 从左到右有序、从上到下有序的二维数组，查找
    // 这种数组类似二叉搜索树
    private boolean find(int[][] nums, int target) {
        if (nums == null) return false;
        int rows = nums.length;
        int cols = nums[0].length;

        int r = 0, c = cols - 1;
        while (r < rows && c >= 0) {
            if (nums[r][c] < target) {
                r++;
            } else if (nums[r][c] > target) {
                c--;
            } else {
                return true;
            }
        }
        return false;
    }

    public List<Integer> findMinK(int[] nums, int k) {
        List<Integer> res = new ArrayList<>(k);
        if (nums == null || k <= 0 || k > nums.length) return res;
        PriorityQueue<Integer> heap = new PriorityQueue<>((n1, n2) -> n2 - n1);

        for (int i = 0; i < k; i++) {
            heap.offer(nums[i]);
        }
        for (int i = k; i < nums.length; i++) {
            if (heap.peek() > nums[i]) {
                heap.poll();
                heap.offer(nums[i]);
            }
        }
        res.addAll(heap);
        return res;
    }

    public int findKthMax(int[] nums, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>();

        for (int i = 0; i < k; i++) {
            heap.offer(nums[i]);
        }
        for (int i = k; i < nums.length; i++) {
            if (heap.peek() < nums[i]) {
                heap.poll();
                heap.offer(nums[i]);
            }
        }
        return heap.peek();
    }

    // 旋转打印二维数组
    public List<Integer> rotatePrint(int[][] nums) {
        List<Integer> res = new ArrayList<>();

        int up = 0, down = nums.length - 1, left = 0, right = nums[0].length - 1;

        while (true) {
            for (int i = left; i <= right; i++) {
                res.add(nums[up][i]);
            }
            up++;
            if (up > down) break;

            for (int j = up; j <= down; j++) {
                res.add(nums[j][right]);
            }
            right--;
            if (right < left) break;

            for (int i = right; i >= left; i--) {
                res.add(nums[down][i]);
            }
            down--;
            if (down < up) break;

            for (int j = down; j >= up; j--) {
                res.add(nums[j][left]);
            }
            left++;
            if (left > right) break;
        }
        return res;
    }

    // 一个正整数数组，把数组里所有数字拼接起来排成一个数，打印能拼接出的所有数字中最小的一个。
    // 例如输入数组{3，32，321}，则打印出这三个数字能排成的最小数字为321323
    public String printMinNum(int[] nums) {
        List<String> str = new ArrayList<>();
        for (int i : nums) {
            str.add(String.valueOf(i));
        }
        str.sort((o1, o2) -> (o1 + o2).compareTo(o2 + o1));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.size(); i++) {
            sb.append(str.get(i));
        }
        return sb.toString();
    }

    public int findMaxSubArray(int[] nums) {
        int res = nums[0];
        for (int i = 1; i < nums.length; i++) {
            nums[i] = nums[i - 1] > 0 ? nums[i - 1] + nums[i] : nums[i];
            res = Math.max(res, nums[i]);
        }
        return res;
    }

    private int count = 0;
    public int inversePair(int[] nums) {            // 查找数组中的逆序对，利用归并排序思路，在归并过程中计算逆序对数
        if (nums == null || nums.length <= 1)
            return 0;
        mergeSort(nums, 0, nums.length - 1);
        return count;
    }
    private void mergeSort(int[] nums, int low, int high) {
        if (low >= high) return;
        int mid = low + (high - low) / 2;
        mergeSort(nums, low, mid);
        mergeSort(nums, mid + 1, high);
        merge(nums, low, mid, high);
    }
    private void merge(int[] nums, int low, int mid, int high) {
        int[] tmp = new int[high - low + 1];
        int i = 0, p1 = low, p2 = mid + 1;

        while (p1 <= mid && p2 <= high) {
            if (nums[p1] <= nums[p2]) {     // 如果前面的元素小于后面的不能构成逆序对
                tmp[i++] = nums[p1++];
            } else {        // 如果前面的元素大于后面的，那么在前面元素之后的元素都能和后面的元素构成逆序对
                count = (count + (mid - p1 + 1)) % 1000000007;
                tmp[i++] = nums[p2++];
            }
        }
        while (p1 <= mid) {
            tmp[i++] = nums[p1++];
        }
        while (p2 <= high) {
            tmp[i++] = nums[p2++];
        }
        for (i = 0; i < tmp.length; i++) {
            nums[low + i] = tmp[i];
        }
    }
}
