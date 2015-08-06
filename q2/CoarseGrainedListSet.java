import java.util.concurrent.locks.ReentrantLock;

public class CoarseGrainedListSet<T> implements ListSet<T> {

     public class cLink {

        private T obj;
        public cLink next;
        public int id;

        public cLink(T obj) {
            this.obj = obj;
            this.id = obj.hashCode();
            this.next =null;
        }
    }

    private cLink head;
    private ReentrantLock coarseLock;
    final private Integer HEAD = Integer.valueOf(0);
    final private Integer TAIL = Integer.valueOf(1);

    public CoarseGrainedListSet() {
        this.coarseLock = new ReentrantLock();

        this.head = new cLink((T) HEAD);      //HEAD
        this.head.id = Integer.MIN_VALUE;

        this.head.next  = new cLink((T) TAIL);//TAIL
        this.head.next.id = Integer.MAX_VALUE;
    }

    public boolean add(T obj) {
        cLink prev, curr;
        int obj_id = obj.hashCode();
        coarseLock.lock();

        try {
            prev = head;
            curr = prev.next;

            while (curr.id < obj_id) {
                prev = curr;
                curr = curr.next;
            }

            if (obj_id == curr.id) {
                return false;
            }
            else {
                
                cLink new_obj = new cLink(obj);
                new_obj.next = curr;
                prev.next = new_obj;
                return true;
            }
        }
        finally {
            coarseLock.unlock();
        }
    }

    public boolean remove(T obj) {
        cLink prev, curr;
        int obj_id = obj.hashCode();
        coarseLock.lock();
        try {
            prev = head;
            curr = prev.next;

            while (curr.id < obj_id) {
                prev = curr;
                curr = curr.next;
            }

            if (obj_id == curr.id) {
                prev.next = curr.next;
                return true;
            }
            else {
                return false;
            }
        }
        finally {
            coarseLock.unlock();
        }
    }


    public boolean contains(T obj) {
        cLink prev, curr;
        int obj_id = obj.hashCode();
        coarseLock.lock();
        try {
            prev = head;
            curr = prev.next;

            while (curr.next != null && obj_id != curr.id) {
                //DEBUG System.out.println(curr.id + " " + obj_id);
                curr = curr.next;
            }

            if (obj_id == curr.id) {
                //DEBUG System.out.println("Found: " + obj);
                return true;
            }
        }
        finally {
            coarseLock.unlock();
        }
        //DEBUG System.out.println("Not Found: " + obj);
        return false;
    }

    public static void main (String args []) {

        final ListSet<Integer> set = new CoarseGrainedListSet<>();
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
