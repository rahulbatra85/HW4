import java.util.concurrent.atomic.*;

public class LockFreeQueue<T> implements MyQueue<T> {


  //Node class
  class Node<T>{
    T value;
    AtomicStampedReference<Node<T>> next;

    Node(T value){
      this.value = value;
      this.next = new AtomicStampedReference<Node<T>>(null, 0);
    }
  }

  AtomicStampedReference<Node<T>> mHead; //Queue Head
  AtomicStampedReference<Node<T>> mTail; //Queue Tail

  //Constructor
  LockFreeQueue(){
    Node<T> node = new Node<T>(null); //Sentinel Node

    //Tail and head point to sentinel node
    mHead = new AtomicStampedReference<Node<T>>(node, 0);
    mTail = new AtomicStampedReference<Node<T>>(node, 0);
  }

  //Enqueue
  public boolean enq(T value) {

    //Create new node
    Node<T> node = new Node<T>(value);
    int[] tailStamp = new int[1];
    int[] nextStamp = new int[1];
    Node<T> tail;
    while(true){
      tail = mTail.get(tailStamp); //Read tail
      Node<T> next = tail.next.get(nextStamp); //Read next after tail

      if(tail == mTail.getReference()){
        if(next == null){
          if(tail.next.compareAndSet(next,node,nextStamp[0],nextStamp[0] + 1)){
              break;
          }
        } else{
            mTail.compareAndSet(tail,next,tailStamp[0],tailStamp[0] + 1);
        }
      }
    }
    mTail.compareAndSet(tail,node,tailStamp[0],tailStamp[0] + 1);

    return true;
  }

  //Dequeue
  public T deq() {
    int[] tailStamp = new int[1];
    int[] headStamp = new int[1];
    int[] nextStamp = new int[1];

    while(true){
      Node<T> head = mHead.get(headStamp);
      Node<T> tail = mTail.get(tailStamp);
      Node<T> next = head.next.get(nextStamp);
      
      if(head == mHead.getReference()){
        if(head == tail){
          if(next == null){
            return null;
          }
          mTail.compareAndSet(tail, next, tailStamp[0], tailStamp[0]+1);
        } else{
          T value = next.value;
          if(mHead.compareAndSet(head, next, headStamp[0], headStamp[0]+1)){
            return value; 
          }
        }
      }
    }
  }
}
