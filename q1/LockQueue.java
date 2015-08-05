import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.util.Random;

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

  public static void main(String args[]){
   LockQueue<Integer> q = new LockQueue<Integer>();

    int numT = 2;
    testThread tt[] = new testThread[numT];
    Thread t[]      = new Thread[numT];
    for(int n=0; n<numT; n++){
      if(n==0){
        tt[n] = new testThread(q,10,true);
      } else {
        tt[n] = new testThread(q,10,false);
      }
      t[n] = new Thread(tt[n]);
    }

    for(int n=0; n<numT; n++){
      t[n].start() ;
    }

    try {
      for(int n=0; n<numT; n++){
        t[n].join() ;
      }
    } catch (InterruptedException e) {}

    //Compare results
    int DeqSum = 0, EnqSum = 0;
    for(int n=0; n<numT; n++){
        DeqSum+= t[n].getDeqSum();
        EnqSum+= t[n].getEnqSum();
    }

    //Remaining items on the queue
    while(q.head.next != null){
        Node<Integer> n = q.head.next
    }
    
  }

}


class testThread implements Runnable{
    int enqSum, deqSum, iter; 
    boolean enqOnly;
    LockQueue<Integer> q;
    testThread(LockQueue<Integer> q, int iter, boolean enqOnly){
      enqSum = 0;
      deqSum = 0;
      this.iter = iter;
      this.q = q;
      this.enqOnly = enqOnly;
      System.out.println("enqSum: " + enqSum + "deqSum: " + deqSum + "iter: " + iter + "enqOnly: " + enqOnly);
    }

    public void run(){
      Random rn = new Random();
      int val,turn,i=iter;
      if(enqOnly){
        while(i>0){
          val = rn.nextInt(1000000) + 1;
          enqSum+=val;
          q.enq(val); 
          i--;     
        }
      } else{
        while(i>0){
           turn = rn.nextInt(10) + 1;
          if(turn < 5){
            val = rn.nextInt(1000000) + 1;
            enqSum+=val;
            q.enq(val); 
          } else{
            deqSum+=q.deq().intValue();
          }
          i--;     
        }
      }
    }

    public int getDeqSum(){
      return enqSum;
    }

    public int getEnqSum(){
      return deqSum;
    }
}
