class Incrementor {
    static private int personalInt = 0;
    synchronized void increment() {
        personalInt++;
    }

    synchronized static void incrementStatically() {
        personalInt++;
    }

    public int getPersonalInt() {
        return personalInt;
    }
}

class IncrementorThread extends Thread {
    private final int calls;
    private final Incrementor incrementor;
    IncrementorThread(Incrementor incrementor, int calls) {
        this.calls = calls;
        this.incrementor = incrementor;
    }

    public void run() {
        for (int i = 0; i < calls; i++) {
            incrementor.increment();
            Incrementor.incrementStatically();
        }
        System.out.println(incrementor.getPersonalInt());
    }
}


class Main2 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start");
        Incrementor incrementor = new Incrementor();
        int calls = 10000;

        IncrementorThread incrementorThread1 = new IncrementorThread(incrementor, calls);
        IncrementorThread incrementorThread2 = new IncrementorThread(incrementor, calls);
        incrementorThread1.start();
        incrementorThread2.start();

        incrementorThread1.join();
        incrementorThread2.join();

        System.out.println("Result: " + incrementor.getPersonalInt());

        System.out.println("End");
    }
}