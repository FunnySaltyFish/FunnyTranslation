package com.funny.translation.translate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Test {


    public static void main(String[] args) {
        ArrayList<Integer> l = new ArrayList<>();
        l.add(3);
        l.sort((Integer o1, Integer o2) -> o1 - o2);
    }
}
