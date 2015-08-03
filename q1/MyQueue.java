// You do NOT need to modify this file
// The interface of the MyQueue
// You should implement LockQueue and LockFreeQueue by extending this class

public interface MyQueue<T> {
    public boolean enq(T value);
    public T deq();
}
