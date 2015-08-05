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

    public class Window {
        public Link prev, curr;

        public Window(Link wPrev, Link wCurr) {
            prev = wPrev; curr = wCurr;
        }
    }

    private Link head;
    private Link tail;
    
    public LockFreeListSet() {
        tail = new Link(null);
        head = new Link(null);
        head.next.set(tail, false);
    }

    
    public Window find(Link head, int this_id) {
       
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
                    if (!check) continue retry;
                    curr = ok;
                    ok = curr.next.get(marked);
                }
                if (curr.id >= this_id)
                    return new Window(prev, curr);
                prev = curr;
                curr = ok;
            }
        }
    }

    public boolean add(T obj) {
        int this_id = obj.hashCode();
        while (true) {
            Window window = find(head, this_id);
            Link prev = window.prev, curr = window.curr;
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
            Window window = find(head, this_id);
            Link prev = window.prev, curr = window.curr;
            if (curr.id != this_id) {
                return false;
            }
            else {
                Link ok = curr.next.getReference();
                check = curr.next.compareAndSet(ok, ok, false, true);
                if (!check) continue;
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
            Link ok = curr.next.get(marked);
        }
        return (curr.id == this_id && !marked[0]);
    }
}
