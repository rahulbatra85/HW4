import java.util.concurrent.locks.ReentrantLock;

public class CoarseGrainedListSet<T> implements ListSet<T> {

     public class Link {

        private T obj;
        public Link next;
        public int id;

        public Link(T obj) {
            this.obj = obj;
            this.id = obj.hashCode();
            this.next = new Link(null);
        }
    }

    private Link head;
    private ReentrantLock coarseLock;

    public CoarseGrainedListSet() {
        this.coarseLock = new ReentrantLock();
        this.head = new Link(null);
        this.head.id = Integer.MIN_VALUE;
        this.head.next.id = Integer.MAX_VALUE;
    }

    public boolean add(T obj) {
        Link prev, curr;
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
                Link this_obj = new Link(obj);
                this_obj.next = curr;
                prev.next = this_obj;
                return true;
            }
        }
        finally {
            coarseLock.unlock();
        }
    }

    public boolean remove(T obj) {
        Link prev, curr;
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
        Link prev, curr;
        int obj_id = obj.hashCode();
        coarseLock.lock();
        try {
            prev = head;
            curr = prev.next;

            while (curr.next != null && obj_id != curr.id) {
                System.out.println(curr.id + " " + obj_id);
                curr = curr.next;
            }

            if (obj_id == curr.id) {
                System.out.println("Found: " + obj);
                return true;
            }
        }
        finally {
            coarseLock.unlock();
        }
        System.out.println("Not Found: " + obj);
        return false;
    }
}
