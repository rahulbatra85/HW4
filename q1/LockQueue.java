import java.lang.Override;
import java.lang.Thread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockQueue<T> implements MyQueue<T> {

    public class Node {

        public T value;
        public AtomicReference<Node> next;

        public Node(T value) {
            this.value = value;
            next = new AtomicReference<Node>(null);
        }
    }

    ReentrantLock enqLock, deqLock;
    Condition notEmptyCondition, notFullCondition;
    AtomicInteger count;
    volatile Node head, tail;

    public LockQueue() {
        head = new Node(null);
        tail = head;

        count = new AtomicInteger(0);

        enqLock = new ReentrantLock();
        //notFullCondition = enqLock.newCondition();

        deqLock = new ReentrantLock();
        //notEmptyCondition = deqLock.newCondition();
    }

    @Override
    public void enq(T value) {

        enqLock.lock();
        try {
            Node e = new Node(value);
            tail.next = e;
            tail = e;
            count++;
        }
        finally {
            enqLock.unlock();
        }
    }

    @Override
    public T deq() {

        T result;
        deqLock.lock();
        try {
            if (head.next == null) { //queue is empty
                Thread.yield();      //block per spec ... NOT SURE IF THIS IS CORRECT
            }
            result = head.next.value;
            head = head.next;
            count--;
        }
        finally {
            deqLock.unlock();
        }

        return result;
    }


}
