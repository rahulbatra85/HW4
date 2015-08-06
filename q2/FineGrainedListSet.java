import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedListSet<T> implements ListSet<T> {

    public class fLink {

        private T obj;
        protected ReentrantLock local;
        public fLink next;
        public int id;
        
        public fLink(T obj) {

            this.obj = obj;
            this.id = obj.hashCode();
            this.local = new ReentrantLock();
            this.next = null;
        }
    }
    
    private fLink head;
    final private Integer HEAD = Integer.valueOf(0);
    final private Integer TAIL = Integer.valueOf(1);

    public FineGrainedListSet() {

        this.head = new fLink((T) HEAD);      //HEAD
        this.head.id = Integer.MIN_VALUE;

        this.head.next  = new fLink((T) TAIL);//TAIL
        this.head.next.id = Integer.MAX_VALUE;
    }

    public boolean add(T obj) {

        int this_id = obj.hashCode();
        head.local.lock();
        fLink prev = head;

        try {
            fLink curr = prev.next;
            curr.local.lock();

            try {

                while (curr.id < this_id) {
                    prev.local.unlock();
                    prev = curr;
                    curr = curr.next;
                    curr.local.lock();
                }

                if (curr.id == this_id) {
                    return false;
                }

                fLink new_obj = new fLink(obj);
                new_obj.next = curr;
                prev.next = new_obj;
                return true;
            }
            finally {
                curr.local.unlock();
            }
        }
        finally {
            prev.local.unlock();
        }
    }

    public boolean remove(T obj) {
        fLink prev = null;
        fLink curr;
        int this_id = obj.hashCode();
        head.local.lock();
        try {
            prev = head;
            curr = prev.next;
            curr.local.lock();

            try {
                while (curr.id < this_id) {
                    prev.local.unlock();
                    prev = curr;
                    curr = curr.next;
                    curr.local.lock();
                }
                if (curr.id == this_id) {
                    prev.next = curr.next;
                    return true;
                }
                return false;
            }
            finally {
                curr.local.unlock();
            }
        }
        finally {
            prev.local.unlock();
        }
    }

    public boolean contains(T obj) {

        fLink pred = null;
        fLink curr;
        int this_id = obj.hashCode();
        head.local.lock();

        try {
            pred = head;
            curr = pred.next;
            while (curr.next != null && this_id != curr.id) {
                //DEBUG System.out.println(curr.id + " " + this_id);
                curr = curr.next;
            }
            if (this_id == curr.id) {
                //DEBUG System.out.println("Found: " + obj);
                return true;
            }

        } finally {
            pred.local.unlock();
        }
        //DEBUG System.out.println("Not Found: " + obj);
        return false;
    }

    public static void main (String args []) { //QUICK TEST

        final ListSet set = new FineGrainedListSet();
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
