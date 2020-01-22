package persistent;

import static org.junit.Assert.*;

import org.junit.Test;

public class PersistentArrayHistoryTest {

    @Test
    public void undo() {
        PersistentArrayHistory<String> init = new PersistentArrayHistory<>(2);
        PersistentArrayHistory<String> a = init.add("a");
        PersistentArrayHistory<String> b = a.add("b");
        PersistentArrayHistory<String> c = b.add("c");
        PersistentArrayHistory<String> bb = c.undo();
        PersistentArrayHistory<String> aa = c.undo().undo();
        assertEquals("(a, _, _, _)", aa.toString());
        assertEquals("(a, b, _, _)", bb.toString());
        assertEquals("(a, b, c, _)", c.toString());
    }

    @Test
    public void redo() {
        PersistentArrayHistory<String> v0 = new PersistentArrayHistory<>(2);
        PersistentArrayHistory<String> v1 = v0.add("a");
        PersistentArrayHistory<String> v2 = v1.add("b");
        PersistentArrayHistory<String> v3 = v1.add("c");

        PersistentArrayHistory<String> v2Restored = v2.undo().redo();
        PersistentArrayHistory<String> v3Restored = v3.undo().redo();

        assertEquals("(a, _, _, _)", v2.undo().toString());
        assertEquals("(a, _, _, _)", v3.undo().toString());
        assertEquals("(_, _, _, _)", v2.undo().undo().toString());

        assertEquals("(a, b, _, _)", v2Restored.toString());
        assertEquals("(a, c, _, _)", v3Restored.toString());

        v3Restored = v3.undo().redo();
        v2Restored = v2.undo().redo();
        assertEquals("(a, b, _, _)", v2Restored.toString());
        assertEquals("(a, c, _, _)", v3Restored.toString());
    }

    @Test
    public void undoNested() {
        PersistentArray<Integer> inside = new PersistentArray<Integer>(1).add(100);
        PersistentArrayHistory<PersistentArray<Integer>> outer1 = new PersistentArrayHistory<PersistentArray<Integer>>(
            2).add(inside);
        PersistentArrayHistory<PersistentArray<Integer>> outer2 = outer1.set(0, outer1.get(0).add(110));

        assertEquals("((100, 110), _, _, _)", outer2.toString());
        assertEquals("((100, _), _, _, _)", outer2.undo().toString());
        assertEquals("((100, _), _, _, _)", outer1.toString());
        assertEquals("(_, _, _, _)", outer1.undo().toString());

    }
}
