import java.util.Random;

public class SetTest implements Runnable{

    ListSet list;
    int id;
    int min = 0, max = 100;

    public SetTest(ListSet list, int id) {
        this.list = list;
        this.id =id;
    }

    public void run() {
        Integer  tNum = Integer.valueOf(new Random().nextInt((max - min) + 1) + min);
        System.out.println("Thread:" + id + " Num:" + tNum + " ADD:" + list.add(tNum));
        System.out.println("Thread:" + id + " Num:" + tNum + " CON:" + list.contains(tNum));
        System.out.println("Thread:" + id + " Num:" + tNum + " REM:" + list.remove(tNum));
    }

    public static void main(String[] args) {


        final int type = args.length < 1 ? 0 : new Integer(args[0]);
        ListSet<Integer> set;

        switch (type) {
            case 0 :
                set = new CoarseGrainedListSet();
                System.out.println("Testing CoarseGrainedListSet");
                break;
            case 1 :
                set = new FineGrainedListSet();
                System.out.println("Testing FineGrainedListSet");
                break;
            case 2 :
                set = new LockFreeListSet();
                System.out.println("Testing LockFreeListSet");
                break;
            default:
                System.err.println("Choose and integer between 0 and 2");
                return;
        }

        int numThreads = 1000;
        Thread test_array[] = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            test_array[i] = new Thread(new SetTest(set, i));
        }

        for (Thread check : test_array)
            check.start();

        for (Thread check : test_array) {
            try {
                check.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}