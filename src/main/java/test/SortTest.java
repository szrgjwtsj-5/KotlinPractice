package test;

import java.util.Arrays;

public class SortTest {
    public static void main(String[] args) {
        int[] arr = {2, 3, 5, 4, 1};
//        quickSort(arr, 0, arr.length - 1);
        mergeSort(arr, 0, arr.length - 1);
        printIntArr(arr);
    }
    private static void printIntArr(int[] arr) {
        Arrays.stream(arr).forEach(it -> System.out.print(it + " "));
    }

    public static void quickSort(int[] arr, int begin, int end) {
        if (arr == null || begin >= end || arr.length == 1) return;
        int base = arr[begin];
        int l = begin, r = end;

        while (l < r) {
            while (r > l && arr[r] > base) {
                r--;
            }
            if (l < r) {
                arr[l++] = arr[r];
            }
            while (l < r && arr[l] < base) {
                l++;
            }
            if (l < r) {
                arr[r--] = arr[l];
            }
        }
        arr[l] = base;
//        printIntArr(arr);
//        System.out.println();
        quickSort(arr, begin, l - 1);
        quickSort(arr, r + 1, end);
    }

    public static void mergeSort(int[] nums, int low, int high) {
        if (low == high) return;
        int mid = low + (high - low) / 2;
        mergeSort(nums, low, mid);
        mergeSort(nums, mid + 1, high);
        merge(nums, low, mid, high);
    }
    private static void merge(int[] nums, int low, int mid, int high) {
        int[] tmp = new int[high - low + 1];
        int i = 0, p1 = low, p2 = mid + 1;

        while (p1 <= mid && p2 <= high) {
            tmp[i++] = nums[p1] < nums[p2] ? nums[p1++] : nums[p2++];
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
