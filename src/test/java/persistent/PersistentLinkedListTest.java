package persistent;

import static org.junit.Assert.*;

import persistent.PersistentLinkedList;
import org.junit.Before;
import org.junit.Test;

public class PersistentLinkedListTest {
    private PersistentLinkedList<String> a;
    private PersistentLinkedList<String> ba;
    private PersistentLinkedList<String> bac;
    private PersistentLinkedList<String> dbac;
    private PersistentLinkedList<String> dbeac;
    private PersistentLinkedList<String> deac;
    private PersistentLinkedList<String> deacf;
    private PersistentLinkedList<String> deac2;
    private PersistentLinkedList<String> eac;
    private PersistentLinkedList<String> ac;
    private PersistentLinkedList<String> gac;

    @Before
    public void setUp() throws Exception {
        a = new PersistentLinkedList<>("a", 1); // (a,_)
        ba = a.addFirst("b"); // (a,b)
        bac = ba.addLast("c"); // ((a,b),(c,_))
        dbac = bac.addFirst("d"); // ((a,b),(c,d))
        dbeac = dbac.add(2, "e"); // (((a,b),(c,d)), ((e,_),_))
        deac = dbeac.remove(1); // (((a,_),(c,d)), ((e,_),_))
        deacf = deac.addLast("f"); // (((a,f),(c,d)), ((e,_),_))
        deac2 = deacf.removeLast(); // (((a,_),(c,d)), ((e,_),_))
        eac = deac2.removeFirst(); // (((a,_),(c,_)), ((e,_),_))
        ac = eac.removeFirst(); // ((a,_),(c,_))
        gac = ac.addFirst("g"); // ((a,g),(c,_))
    }

    @Test
    public void getFirst() {
        assertEquals("a", a.getFirst());
        assertEquals("b", ba.getFirst());
        assertEquals("b", bac.getFirst());
        assertEquals("d", dbeac.getFirst());
    }

    @Test
    public void getLast() {
        assertEquals("a", a.getLast());
        assertEquals("a", ba.getLast());
        assertEquals("c", bac.getLast());
        assertEquals("c", dbeac.getLast());
    }

    @Test
    public void get() {
        assertEquals("d", dbeac.get(0));
        assertEquals("b", dbeac.get(1));
        assertEquals("e", dbeac.get(2));
        assertEquals("c", dbeac.get(4));
    }

    @Test
    public void add() {
        assertEquals("hmm", dbeac.add(3, "hmm").get(3));
        assertEquals("a", dbeac.get(3));
    }

    @Test
    public void addFirst() {
        assertEquals("hmm", dbeac.addFirst("hmm").get(0));
        assertEquals("d", dbeac.addFirst("hmm").get(1));
        assertEquals("d", dbeac.get(0));
    }

    @Test
    public void addLast() {
        assertEquals("hmm", dbeac.addLast("hmm").getLast());
        assertEquals("c", dbeac.getLast());
    }

    @Test
    public void remove() {
        assertEquals("b", dbeac.get(1));
        assertEquals("e", deac.get(1));
    }

    @Test
    public void removeFirst() {
        assertEquals("e", eac.getFirst());
    }

    @Test
    public void removeLast() {
        assertEquals("d", deac2.getFirst());
        assertEquals("c", deac2.getLast());
    }


    @Test
    public void testToString() {
        assertEquals("[a]", a.toString());
        assertEquals("[g, a, c]", gac.toString());
    }

    @Test
    public void innerRepresentation() {
        assertEquals("(((a, _), (c, _)), ((e, _), _))", eac.innerRepresentation());
        assertEquals("((a, _), (c, _))", ac.innerRepresentation());
    }

    @Test
    public void size() {
        assertEquals(5, deacf.size());
        assertEquals(4, deac2.size());
        assertEquals(3, gac.size());
    }

    @Test
    public void toPersistentArray() {
        assertEquals("(((a, _), (c, _)), ((e, wow), _))", eac.toPersistentArray().add("wow").toString());
    }
}
