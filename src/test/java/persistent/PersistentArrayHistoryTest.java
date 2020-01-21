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
}
