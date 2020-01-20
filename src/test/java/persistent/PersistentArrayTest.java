package persistent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PersistentArrayTest {

    @Test
    public void add() {
        PersistentArray<Integer> a = new PersistentArray<>(0, 1);
        PersistentArray<Integer> b = a.add(1);
        PersistentArray<Integer> c = b.add(2);
        PersistentArray<Integer> d = c.add(3);
        PersistentArray<Integer> e = d.add(4);
        assertEquals("(0, _)", a.toString());
        assertEquals("(0, 1)", b.toString());
        assertEquals("((0, 1), (2, _))", c.toString());
        assertEquals("((0, 1), (2, 3))", d.toString());
        assertEquals("(((0, 1), (2, 3)), ((4, _), _))", e.toString());

        PersistentArray<Integer> aa = new PersistentArray<>(0, 2);
        PersistentArray<Integer> bb = aa.add(1);
        PersistentArray<Integer> cc = bb.add(2);
        PersistentArray<Integer> dd = cc.add(3);
        PersistentArray<Integer> ee = dd.add(4);
        assertEquals("(0, _, _, _)", aa.toString());
        assertEquals("(0, 1, _, _)", bb.toString());
        assertEquals("(0, 1, 2, _)", cc.toString());
        assertEquals("(0, 1, 2, 3)", dd.toString());
        assertEquals("((0, 1, 2, 3), (4, _, _, _), _, _)", ee.toString());
    }

    @Test
    public void set() {
        PersistentArray<Integer> a = new PersistentArray<>(0, 1).add(1).add(2).add(3);
        PersistentArray<Integer> b = a.set(1, 806);
        assertEquals("((0, 1), (2, 3))", a.toString());
        assertEquals("((0, 806), (2, 3))", b.toString());

        PersistentArray<Integer> aa = new PersistentArray<>(0, 2).add(1).add(2).add(3).add(4)
            .add(10);
        PersistentArray<Integer> bb = aa.set(5, 806);
        assertEquals("((0, 1, 2, 3), (4, 10, _, _), _, _)", aa.toString());
        assertEquals("((0, 1, 2, 3), (4, 806, _, _), _, _)", bb.toString());
    }

    @Test
    public void pop() {
        PersistentArray<Integer> cur1 = new PersistentArray<>(0, 1);
        PersistentArray<Integer> prev1 = cur1;
        PersistentArray<Integer> cur2 = new PersistentArray<>(0, 2);
        PersistentArray<Integer> prev2 = cur2;
        for (int i = 2; i <= 30; i++) {
            cur1 = prev1.add(i);
            cur2 = prev2.add(i);

            assertEquals(prev1.toString(), cur1.pop().toString());
            assertEquals(prev2.toString(), cur2.pop().toString());

            prev1 = cur1;
            prev2 = cur2;
        }
    }

    @Test
    public void get() {
        PersistentArray<Integer> a = new PersistentArray<>(0, 2);
        for (int i = 1; i < 100; i++) {
            a = a.add(i * i);
        }
        long expected = 75 * 75;
        long actual = a.get(75);
        assertEquals(expected, actual);
    }

    @Test
    public void toPersistentLinkedList() {
        PersistentArray<Integer> a = new PersistentArray<>(0, 1);
        PersistentArray<Integer> b = a.add(1).add(2).add(3).add(4);
        PersistentLinkedList<Integer> ll = b.toPersistentLinkedList();
        assertEquals("(((0, 1), (2, 3)), ((4, _), _))", ll.innerRepresentation());
        assertEquals("[0, 1, 2, 3, 4]", ll.toString());
    }
}
