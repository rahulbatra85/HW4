import java.util.Random;
import java.util.concurrent.locks.*;

class QueueTest implements Runnable{
    int enqSum, deqSum, iter; 
    boolean enqOnly;
    LockQueue<Integer> q;

    QueueTest(LockQueue<Integer> q, int iter, boolean enqOnly){
      enqSum = 0;
      deqSum = 0;
      this.iter = iter;
      this.q = q;
      this.enqOnly = enqOnly;
    }

    public void run(){
      Random rn = new Random();
      int val,turn,i=iter;
      if(enqOnly){
        while(i>0){
          val = rn.nextInt(100000) + 1;
          enqSum+=val;
          q.enq(val); 
          i--;     
        }
      } else{
        while(i>0){
           turn = rn.nextInt(10) + 1;
          if(turn < 7){
            val = rn.nextInt(100000) + 1;
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

  public static void main(String args[]){
   LockQueue<Integer> q = new LockQueue<Integer>();

    int numT = 100;
    QueueTest tt[] = new QueueTest[numT];
    Thread t[]      = new Thread[numT];
    for(int n=0; n<numT; n++){
      if(n==0){
        tt[n] = new QueueTest(q,2500,true);
      } else {
        tt[n] = new QueueTest(q,50,false);
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
    int deqSum = 0, enqSum = 0, remSum = 0;
    for(int n=0; n<numT; n++){
        deqSum+= tt[n].getDeqSum();
        enqSum+= tt[n].getEnqSum();
    }

    //Remaining items on the queue
    LockQueue<Integer>.Node<Integer> n = q.head.next;
    while(n != null){
        enqSum+= n.value;
        n = n.next;
    }
   
    if(deqSum != enqSum){
      System.out.println("TEST FAILED");
    } else{
      System.out.println("TEST PASSED");
    }
    System.out.println("deqSum: " + deqSum);
    System.out.println("enqSum: " + enqSum);

    
  }

}

