import persistent.PersistentArrayHistory;

public class Main {
    public static void main(String[] args) {
        PersistentArrayHistory<String> a = new PersistentArrayHistory<>("a", 2);
        PersistentArrayHistory<String> b = a.add("b");
        PersistentArrayHistory<String> c = a.add("c");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        PersistentArrayHistory<String> bb = b.undo();
        System.out.println(bb);
        System.out.println(bb);
        System.out.println(bb.redo());
        PersistentArrayHistory<String> cc = c.undo();
        System.out.println(cc);
    }

}

