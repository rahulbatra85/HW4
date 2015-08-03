public class LockFreeQueue<T> implements MyQueue<T> {
  public boolean enq(T value) {
    return false;
  }
  public T deq() {
    return null;
  }
}
