package mutation.sim;

public class Test {
    public static void main(String[] args) {
//        Log.activate();
        Mutagen m1 = new Mutagen(), m2 = new Mutagen();
        m1.add("a;c", "gt9");
        m1.add("c;c", "gt9");
        m2.add("ac;c", "gt9");
        Listagen l1 = new Listagen(m1);
        Listagen l2 = new Listagen(m2);
        System.out.println(l1.equals(l2));
    }
}