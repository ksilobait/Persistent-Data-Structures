package persistent;

import static org.junit.Assert.*;

import org.junit.Test;

public class PersistentTreeMapTest {

    private class Object1 {

        @Override
        public int hashCode() {
            return 10;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Object1);
        }
    }

    private class Object2 {

        @Override
        public int hashCode() {
            return 10;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Object2);
        }
    }

    private class NegativeObject {

        @Override
        public int hashCode() {
            return -10;
        }
    }


    @Test
    public void putAndGet() {
        PersistentTreeMap<Integer, String> a = new PersistentTreeMap<>(1);
        PersistentTreeMap<Integer, String> b = a.put(9999999, "hmm");
        PersistentTreeMap<Integer, String> c = b.put(9999998, "hee");
        assertEquals("hmm", c.get(9999999));
        assertEquals("hee", c.get(9999998));
    }

    @Test
    public void negativeHash() {
        PersistentTreeMap<Object, String> a = new PersistentTreeMap<>(1);
        NegativeObject negativeObject = new NegativeObject();
        a = a.put(negativeObject, "result");
        assertEquals("result", a.get(negativeObject));
    }

    @Test
    public void collisions() {
        Object1 object1 = new Object1();
        Object2 object2 = new Object2();
        Integer value1 = 100;
        Integer value2 = 200;

        PersistentTreeMap<Object, Integer> a = new PersistentTreeMap<>(1);
        PersistentTreeMap<Object, Integer> b = a.put(object1, value1);
        PersistentTreeMap<Object, Integer> c = b.put(object2, value2);
        assertEquals(value1, c.get(object1));
        assertEquals(value2, c.get(object2));

        PersistentTreeMap<Object, Integer> x = new PersistentTreeMap<>(1);
        PersistentTreeMap<Object, Integer> y = x.put(object2, value2);
        PersistentTreeMap<Object, Integer> z = y.put(object1, value1);
        assertEquals(value1, z.get(object1));
        assertEquals(value2, z.get(object2));
    }

    @Test
    public void collisionsCopy() {
        Object1 object1 = new Object1();
        Object2 object2 = new Object2();
        Integer value1 = 100;
        Integer value2 = 200;

        PersistentTreeMap<Object, Integer> a = new PersistentTreeMap<>(1);
        PersistentTreeMap<Object, Integer> a1 = a.put(object1, value1);
        PersistentTreeMap<Object, Integer> a12 = a1.put(object2, value2);

        assertFalse(a.containsKey(object1));
        assertFalse(a.containsKey(object2));

        assertTrue(a1.containsKey(object1));
        assertFalse(a1.containsKey(object2));

        assertTrue(a12.containsKey(object1));
        assertTrue(a12.containsKey(object2));
    }

    @Test
    public void remove() {
        Integer key6 = 6;
        Integer key9 = 9999998;
        PersistentTreeMap<Integer, String> a = new PersistentTreeMap<>(1);
        PersistentTreeMap<Integer, String> b = a.put(key6, "hmm");
        PersistentTreeMap<Integer, String> c = b.put(key9, "hee");
        PersistentTreeMap<Integer, String> bb = c.remove(key9);
        PersistentTreeMap<Integer, String> aa = bb.remove(key6);

        assertTrue(c.containsKey(key6));
        assertTrue(c.containsKey(key9));

        assertTrue(bb.containsKey(key6));
        assertFalse(bb.containsKey(key9));

        assertFalse(aa.containsKey(key6));
        assertFalse(aa.containsKey(key9));
    }
}
