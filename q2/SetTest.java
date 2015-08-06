import java.util.Random;

public class SetTest {

    ListSet list;
    Random rand = new Random();
    int  min = 0, max = 100;

    SetTest(ListSet list) {
        this.list = list;
    }

    public void run() {
        int  someNum = rand.nextInt((max - min) + 1) + min;


    }

    public static void main(String[] args) {


        final int type = args.length < 1 ? 0 : new Integer(args[0]);
        ListSet set;


        switch (type%3) {
            case 0 :  set = new CoarseGrainedListSet(); break;
            case 1 :  set = new FineGrainedListSet(); break;
            case 2 :  set = new LockFreeListSet(); break;
        }

        int numThreads = 1000;
        Thread test_array[] = new Thread[numThreads];
        SetTest test = new SetTest(set);

        for (int i = 0; i < numThreads; i++) {

            test_array[i] =
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