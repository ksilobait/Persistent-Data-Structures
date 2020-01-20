package persistent;

import static org.junit.Assert.*;

import org.junit.Test;

public class PersistentLinkedListHistoryTest {

    @Test
    public void undo() {
        PersistentLinkedListHistory<String> a = new PersistentLinkedListHistory<>("y", 2);
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
}
