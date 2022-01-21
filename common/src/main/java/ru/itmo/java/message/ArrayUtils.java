package ru.itmo.java.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArrayUtils {
    public static List<Integer> makeSort(List<Integer> arr) {

        boolean isSorted = false;
        int buf;
        while (!isSorted) {
            isSorted = true;
            for (int i = 0; i < arr.size() - 1; i++) {
                if (arr.get(i) > arr.get(i + 1)) {
                    isSorted = false;
                    buf = arr.get(i);
//                    try {
                        int f = arr.get(i + 1);
                        arr.set(i, f);
//                    } catch (Exception e) {
//                        System.out.println("dfsdfsd");
//                        System.out.println(e.getMessage());
//                        e.printStackTrace();
//                    }
                    arr.set(i + 1, buf);
                }
            }
        }
        return arr;
    }

    public static List<Integer> createRandomArray(int countElements) {
        Random random = new Random();
        List<Integer> arr = new ArrayList<>();
        for (int i = 0; i < countElements; i++) {
            arr.add(random.nextInt());
        }
        return arr;
    }
}
