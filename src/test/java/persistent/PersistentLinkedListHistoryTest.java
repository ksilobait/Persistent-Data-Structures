package persistent;

import static org.junit.Assert.*;

import org.junit.Test;

public class PersistentLinkedListHistoryTest {

    @Test
    public void undo() {
        PersistentLinkedListHistory<String> init = new PersistentLinkedListHistory<>(2);
        PersistentLinkedListHistory<String> a = init.addFirst("y");
        PersistentLinkedListHistory<String> b = a.addLast("z");
        PersistentLinkedListHistory<String> c = b.addFirst("x");
        PersistentLinkedListHistory<String> d = c.removeLast();

        PersistentLinkedListHistory<String> cc = d.undo();
        PersistentLinkedListHistory<String> bb = d.undo().undo();
        PersistentLinkedListHistory<String> aa = d.undo().undo().undo();
        assertEquals("[y]", aa.toString());
        assertEquals("[y, z]", bb.toString());
        assertEquals("[x, y, z]", cc.toString());
        assertEquals("[x, y]", d.toString());
    }

    @Test
    public void redo() {
        PersistentLinkedListHistory<String> v0 = new PersistentLinkedListHistory<>(2);
        PersistentLinkedListHistory<String> v1 = v0.addLast("a");
        PersistentLinkedListHistory<String> v2 = v1.addLast("b");
        PersistentLinkedListHistory<String> v3 = v1.addLast("c");

        PersistentLinkedListHistory<String> v2Restored = v2.undo().redo();
        PersistentLinkedListHistory<String> v3Restored = v3.undo().redo();

        assertEquals("[a]", v2.undo().toString());
        assertEquals("[a]", v3.undo().toString());
        assertEquals("[]", v2.undo().undo().toString());

        assertEquals("[a, b]", v2Restored.toString());
        assertEquals("[a, c]", v3Restored.toString());

        v3Restored = v3.undo().redo();
        v2Restored = v2.undo().redo();
        assertEquals("[a, b]", v2Restored.toString());
        assertEquals("[a, c]", v3Restored.toString());

    }
}
