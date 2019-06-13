package com.tuandai.transaction.client.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CollectionUtilsTest {

    @Test
    public void isMapEqualTest() {

        Map<String, String > map1 = null;
        Map<String, String > map2 =  null;
        assertEquals(CollectionUtils.isMapEqual(map1, map2), true);
        map1 = new HashMap<>();
        assertEquals(CollectionUtils.isMapEqual(map1, map2), true);
        map2 = new HashMap<>();
        assertEquals(CollectionUtils.isMapEqual(map1, map2), true);
        map1.put("a", "a");
        map1.put("b", "b");
        map1.put("c", "c");

        map2.put("a", "a");
        map2.put("b", "b");
        map2.put("c", "c");
        assertEquals(CollectionUtils.isMapEqual(map1, map2), true);
        map1.put("d", "a");
        assertEquals(CollectionUtils.isMapEqual(map1, map2), false);
    }


}
