import java.util.Random;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;

class LockFreeQueueTest implements Runnable{
    int enqSum, deqSum, iter, tid; 
    boolean enqOnly, deqOnly;
    LockFreeQueue<Integer> q;

    LockFreeQueueTest(LockFreeQueue<Integer> q, int iter, boolean enqOnly, boolean deqOnly, int tid){
      enqSum = 0;
      deqSum = 0;
      this.iter = iter;
      this.q = q;
      this.enqOnly = enqOnly;
      this.deqOnly = deqOnly;
      this.tid = tid;
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
          System.out.println("Tid_" + tid + " EnqOnly, iter=" + i + "Enq("+val+")");
        }
      } else if(deqOnly){
          while(i>0){
            Integer v = q.deq();
            if(v != null){
              val = v.intValue();
              deqSum+=val;
              System.out.println("Tid_" + tid + " DeqOnly, iter=" + i + "Deq("+val+")");
            } else{
              System.out.println("Tid_" + tid + " DeqOnly, iter=" + i + "Deq(null)");
            }
            i--;     
          }
      }
      else{
        while(i>0){
           turn = rn.nextInt(10) + 1;
          if(turn < 7){
            val = rn.nextInt(100000) + 1;
            enqSum+=val;
            q.enq(val); 
              System.out.println("Tid_" + tid + " EnqDeq, iter=" + i + "Enq("+val+")");
          } else{
            Integer v = q.deq();
            if(v != null){
              val = v.intValue();
              deqSum+=val;
              System.out.println("Tid_" + tid + " EnqDeq, iter=" + i + "Deq("+val+")");
            } else{
              System.out.println("Tid_" + tid + " EnqDeq, iter=" + i + "Deq(null)");
            }
          }
          i--;     
        }
      }
    }

    public int getDeqSum(){
      return deqSum;
    }

    public int getEnqSum(){
      return enqSum;
    }

  public static void main(String args[]){
   LockFreeQueue<Integer> q = new LockFreeQueue<Integer>();

    if(args.length != 6){
      System.out.println("./LockFreeQueue numEn numDe NumEnDe iter");
      System.exit(-1);
    }
    
    int numEnqT = Integer.parseInt(args[0]);
    int numDeqT = Integer.parseInt(args[1]);
    int numEnqDeqT = Integer.parseInt(args[2]);
    int iter1 = Integer.parseInt(args[3]);
    int iter2 = Integer.parseInt(args[4]);
    int iter3 = Integer.parseInt(args[5]);
    int tid = 0;

    System.out.println("numEnq:" + numEnqT +" numDeq: " + numDeqT + " NumEnDe: "+ numEnqDeqT + "iter1: " + iter1 + 
            "iter2: " + iter2 + "iter3: " + iter3);

    LockFreeQueueTest enqTT[] = new LockFreeQueueTest[numEnqT];
    LockFreeQueueTest deqTT[] = new LockFreeQueueTest[numDeqT];
    LockFreeQueueTest enqDeqTT[] = new LockFreeQueueTest[numEnqDeqT];
    Thread enqT[]      = new Thread[numEnqT];
    Thread deqT[]      = new Thread[numDeqT];
    Thread enqDeqT[]      = new Thread[numEnqDeqT];

    for(int n=0; n<numEnqT; n++){
      tid++;
      enqTT[n] = new LockFreeQueueTest(q,iter1,true,false,tid);
      enqT[n] = new Thread(enqTT[n]);
    }

    for(int n=0; n<numDeqT; n++){
      tid++;
      deqTT[n] = new LockFreeQueueTest(q,iter2,false,true,tid);
      deqT[n] = new Thread(deqTT[n]);
    }

    for(int n=0; n<numEnqDeqT; n++){
      tid++;
      enqDeqTT[n] = new LockFreeQueueTest(q,iter3,false,false,tid);
      enqDeqT[n] = new Thread(enqDeqTT[n]);
    }

    for(int n=0; n<numEnqT; n++){
      enqT[n].start() ;
    }

    for(int n=0; n<numDeqT; n++){
      deqT[n].start() ;
    }

    for(int n=0; n<numEnqDeqT; n++){
      enqDeqT[n].start() ;
    }

    try {
      for(int n=0; n<numEnqT; n++){
        enqT[n].join() ;
      }

      for(int n=0; n<numDeqT; n++){
        deqT[n].join() ;
      }

      for(int n=0; n<numEnqDeqT; n++){
        enqDeqT[n].join() ;
      }

    } catch (InterruptedException e) {}

    //Compare results
    int deqSum = 0, enqSum = 0, remSum = 0;
    for(int n=0; n<numEnqT; n++){
        enqSum+= enqTT[n].getEnqSum();
       // System.out.println("EnqOnlyT: " + n + " EnqSum="+enqSum);
    }
    for(int n=0; n<numDeqT; n++){
        deqSum+= deqTT[n].getDeqSum();
    }
    for(int n=0; n<numEnqDeqT; n++){
        enqSum+= enqDeqTT[n].getEnqSum();
        deqSum+= enqDeqTT[n].getDeqSum();
    }

    //Remaining items on the queue
    LockFreeQueue<Integer>.Node<Integer> n = q.mHead.getReference().next.getReference();
    while(n != null){
        remSum+= n.value;
        //System.out.println("Found Remaining Entry in Queue. Value: " + n.value);
        n = n.next.getReference();
    }
   
    if((enqSum - deqSum) != remSum){
      System.out.println("TEST FAILED");
    } else{
      System.out.println("TEST PASSED");
    }
    System.out.println("deqSum: " + deqSum);
    System.out.println("enqSum: " + enqSum);
    System.out.println("remSum: " + remSum);

    
  }

}

