package com.github.unldenis.util;

import java.util.Arrays;
import java.util.List;

public class StuffUtils {

    public static boolean equal(final boolean[][] arr1, final boolean[][] arr2) {
        if (arr1 == null)
            return (arr2 == null);
        if (arr2 == null)
            return false;
        if (arr1.length != arr2.length)
            return false;
        for (int i = 0; i < arr1.length; i++)
            if (!Arrays.equals(arr1[i], arr2[i]))
                return false;
        return true;
    }


    public static boolean[][] listTo2DArray(final List<List<Boolean>> list) {
        boolean[][] array2D = new boolean[list.size()][list.get(0).size()];
        for(int j=0; j<array2D.length; j++)
            for(int k=0; k<list.get(j).size(); k++)
                array2D[j][k] = list.get(j).get(k);
        return array2D;
    }
}
