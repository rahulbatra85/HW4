import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

public class LockQueue<T> implements MyQueue<T> {

  //Node class
  class Node<T>{
    public Node<T> next;
    T value;

    Node(T value){
      this.value = value;
    }
  }

  ReentrantLock enqLock, deqLock;
  Condition deqQ ;
  Node<T> head;
  Node<T> tail; 
  AtomicInteger count;

  //Constructor
  public LockQueue(){
    head = new Node<T>(null);
    tail = head;
    count = new AtomicInteger(0);
    enqLock = new ReentrantLock(); 
    deqLock = new ReentrantLock(); 
    deqQ = deqLock.newCondition();
  }

  //Enqueue
  public boolean enq(T value) {
    enqLock.lock();
      try {
        Node<T> node = new Node<T>(value);
        tail.next = node;
        tail = node;
        int cnt = count.incrementAndGet();
        if(cnt == 1){
          deqLock.lock();
          deqQ.signalAll();
          deqLock.unlock();
        }
        return true;
      
      } finally {
        enqLock.unlock();
        return false;
      }
  }

  //Dequeue
  public T deq() {
    T result=null;
    deqLock.lock();
    try {
      try{
        while(count.get() == 0){
          deqQ.await();
        }
        count.decrementAndGet();
        result = head.next.value;
        head = head.next;
      } catch(InterruptedException e) {};
    } finally {
      deqLock.unlock();
    }

    return result;
  }



}
