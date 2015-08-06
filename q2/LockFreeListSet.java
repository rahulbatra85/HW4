import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeListSet<T> implements ListSet<T> {

    public class Link {
        private T obj;
        public int id;
        public AtomicMarkableReference<Link> next;

        public Link(T obj) {
            this.obj = obj;
            this.id = obj.hashCode();
            this.next = new AtomicMarkableReference<>(null, false);
        }
    }

    public class Snapshot {
        public Link prev, curr;

        public Snapshot(Link lPrev, Link lCurr) {
            prev = lPrev;
            curr = lCurr;
        }
    }

    private Link head;
    private Link tail;
    final private Integer HEAD = Integer.valueOf(0);
    final private Integer TAIL = Integer.valueOf(1);


    public LockFreeListSet() {
        this.head = new Link((T) HEAD);
        this.tail = new Link((T) TAIL);

        this.head.id = Integer.MIN_VALUE;
        this.tail.id = Integer.MAX_VALUE;

        head.next.set(tail, false);
    }

    
    public Snapshot find(Link head, int this_id) {
    // Helper function to find insertion/sub point
       
        Link prev = null, curr = null, ok = null;
        boolean[] marked = {false};
        boolean check;
        
        retry: 
        while (true) {
            prev = head;
            curr = prev.next.getReference();
            while (true) {
                ok = curr.next.get(marked);
                while (marked[0]) {
                    check = prev.next.compareAndSet(curr, ok, false, false);
                    if (!check)
                        continue retry;
                    curr = ok;
                    ok = curr.next.get(marked);
                }
                if (curr.id >= this_id)
                    return new Snapshot(prev, curr);
                prev = curr;
                curr = ok;
            }
        }
    }

    public boolean add(T obj) {

        int this_id = obj.hashCode();
        while (true) {

            Snapshot frame = find(head, this_id);
            Link prev = frame.prev;
            Link curr = frame.curr;

            if (curr.id == this_id) {
                return false;
            }
            else {
                Link Link = new Link(obj);
                Link.next = new AtomicMarkableReference(curr, false);
                if (prev.next.compareAndSet(curr, Link, false, false)) {
                    return true;
                }
            }
        }
    }

    public boolean remove(T obj) {

        int this_id = obj.hashCode();
        boolean check;
        while (true) {

            Snapshot frame = find(head, this_id);
            Link prev = frame.prev;
            Link curr = frame.curr;

            if (curr.id != this_id) {
                return false;
            }
            else {
                Link ok = curr.next.getReference();
                check = curr.next.compareAndSet(ok, ok, false, true);
                if (!check)
                    continue;
                prev.next.compareAndSet(curr, ok, false, false);
                return true;
            }
        }
    }

    public boolean contains(T obj) {
        boolean[] marked = {false};
        int this_id = obj.hashCode();
        Link curr = head;

        while (curr.id < this_id) {
            curr = curr.next.getReference();
            curr.next.get(marked);
        }
        return (curr.id == this_id && !marked[0]);
    }


    public static void main (String args []) {

        final ListSet set = new LockFreeListSet();
        final int type = args.length < 1 ? 0 : new Integer(args[0]);

        final boolean[] added = new boolean[2];
        final boolean[] taken = new boolean[2];

        Thread mythread1 = new Thread() {
            @Override
            public void run() {
                boolean add7 = set.add(new Integer(7));
                boolean add5 = set.add(new Integer(5));
                added[0] = add5;
                added[1] = add7;
            }
        };

        Thread mythread2 = new Thread() {
            @Override
            public void run() {
                boolean take7 = set.remove(new Integer(7));
                boolean take5 = set.remove(new Integer(5));
                taken[0] = take5;
                taken[1] = take7;
            }
        };

        try {
            mythread1.start();
            mythread2.start();
        }
        catch (Exception e) {
            System.err.println("thread error: "+e);
        }

        System.out.println(added[0] + " " + added[1]);
        System.out.println(taken[0] + " " + taken[1]);
    }

}
