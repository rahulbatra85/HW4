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
    
    public FineGrainedListSet() {

        this.head = new fLink(null);      //HEAD
        this.head.id = Integer.MIN_VALUE;

        this.head.next  = new fLink(null);//TAIL
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
                System.out.println(curr.id + " " + this_id);
                curr = curr.next;
            }
            if (this_id == curr.id) {
                System.out.println("Found: " + obj);
                return true;
            }

        } finally {
            pred.local.unlock();
        }
        System.out.println("Not Found: " + obj);
        return false;
    }
}
