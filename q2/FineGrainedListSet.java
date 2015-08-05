import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineGrainedListSet<T> implements ListSet<T> {

    public class Link {

        private T obj;
        private Lock;
        public Link next;
        public int id;
        
        public Link(T obj) {
            this.obj = obj;
            this.id = obj.hashCode();
            this.next = new Link(null);
        }
    }
    
    private Link head;
    
    public FineGrainedListSet() {
        this.head = new Link(null);
        this.head.id = Integer.MIN_VALUE;
        this.head.next.id = Integer.MAX_VALUE;
    }

    public boolean add(T obj) {
        int this_id = obj.hashCode();
        head.lock();
        Link pred = head;
        try {
            Link curr = pred.next;
            curr.lock();
            try {
                while (curr.id < this_id) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.id == this_id) {
                    return false;
                }
                Link newNode = new Link(obj);
                newNode.next = curr;
                pred.next = newNode;
                return true;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    public boolean remove(T obj) {
        Link pred = null, curr = null;
        int this_id = obj.hashCode();
        head.lock();
        try {
            pred = head;
            curr = pred.next;
            curr.lock();
            try {
                while (curr.id < this_id) {
                    pred.unlock();
                    pred = curr;
                    curr = curr.next;
                    curr.lock();
                }
                if (curr.id == this_id) {
                    pred.next = curr.next;
                    return true;
                }
                return false;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    public boolean contains(T value) {
    return false;
  }
}
