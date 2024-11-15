package com.muyuanjin.feel;

import java.util.Objects;
import java.util.List;
import java.util.Collections;
import java.util.Map;

public class MyClass {

    //[1, 2, 3]
    private static final List<Integer> S1_list_ = List.of(1, 2, 3);

    //[1, 2, 3]
    private static final List<List<Integer>> S1_list_1 = Collections.singletonList(S1_list_);

    @SuppressWarnings("SuspiciousMethodCalls")
    public static boolean expr(Object testInput, Map<String, Object> input) {
        //[1, 2, 3]
        boolean unaryTests = Objects.equals(testInput, S1_list_1);
        if (!unaryTests && S1_list_1.contains(testInput)) {
            unaryTests = true;
        }
        if (!unaryTests && S1_list_1.get(0) != null && S1_list_1.get(0).contains(testInput)) {
            unaryTests = true;
        }
        return unaryTests;
    }
}
