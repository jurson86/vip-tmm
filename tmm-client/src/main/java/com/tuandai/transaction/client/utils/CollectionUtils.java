package com.tuandai.transaction.client.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CollectionUtils {

    // 比较两个map的key是否相同
    public static boolean isMapEqual(Map map1, Map map2) {
        if (map1 == null && map2 == null) {
            return true;
        }
        Set set1 = map1 == null ? null : map1.keySet();
        Set set2 = map2 == null ? null : map2.keySet();
        return isSetEqual(set1, set2);
    }

    // 比较两个set的值是否相同
    public static boolean isSetEqual(Set set1, Set set2) {

        if (org.springframework.util.CollectionUtils.isEmpty(set1) && org.springframework.util.CollectionUtils.isEmpty(set2)) {
            return true;
        }

        if (set1 == null || set2 == null || set1.size() != set2.size()) {
            return false;
        }

        Iterator ite2 = set2.iterator();

        boolean isFullEqual = true;

        while (ite2.hasNext()) {
            if (!set1.contains(ite2.next())) {
                isFullEqual = false;
            }
        }

        return isFullEqual;
    }

}
