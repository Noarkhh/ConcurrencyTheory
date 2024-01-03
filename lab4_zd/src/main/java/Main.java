import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class Fork {
    public final int id;
    public final ReentrantLock lock = new ReentrantLock();
    public Fork(int id) {
        this.id = id;
    }
}

class WaitTimeTracker {
    static private long totalWaitTime = 0;
    static private int waits = 0;

    synchronized public static void recordWait(long waitTime) {
        totalWaitTime += waitTime;
        waits++;
    }

    public static void reset() {
        totalWaitTime = 0;
        waits = 0;
    }

    public static double getAverageWaitTime() {
        return (double) totalWaitTime / waits;
    }
}

abstract class Philosopher extends Thread {
    public static Random RNG = new Random();
    public static Semaphore semaphore;
    public static boolean logging = true;
    public static boolean sleeping = true;

    public final int id;
    public final Fork leftFork;
    public final Fork rightFork;

    public Philosopher(int id, Fork leftFork, Fork rightFork) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    public static Philosopher createPhilosopher(int taskNumber, int id, Fork leftFork, Fork rightFork) {
        return switch (taskNumber) {
            case 1 -> new Philosopher1(id, leftFork, rightFork);
            case 2 -> new Philosopher2(id, leftFork, rightFork);
            case 3 -> new Philosopher3(id, leftFork, rightFork);
            case 4 -> new Philosopher4(id, leftFork, rightFork);
            case 5 -> new Philosopher5(id, leftFork, rightFork);
            case 6 -> new Philosopher6(id, leftFork, rightFork);
            default -> null;
        };
    }

    abstract public void run();

    protected void think() throws InterruptedException {
        log("thinking...");
        if (Philosopher.sleeping) TimeUnit.MILLISECONDS.sleep(RNG.nextInt(100, 300));
        log("finished thinking");
    }

    protected void eat() throws InterruptedException {
        log("eating...");
        if (Philosopher.sleeping) TimeUnit.MILLISECONDS.sleep(RNG.nextInt(10, 30));
        log("finished eating");
        log("putting down forks " + leftFork.id + " and " + rightFork.id);
    }

    protected void log(String message) {
        if (Philosopher.logging) System.out.println("Philosopher " + id + ": " + message);
    }
}

abstract class SynchronizedPhilosopher extends Philosopher {
    protected Fork firstFork;
    protected Fork secondFork;

    public SynchronizedPhilosopher(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
    }

    @Override
    public void run() {
        while (true) {
            try {
                think();
                long waitStart = System.currentTimeMillis();
                log("trying to pick up left fork " + firstFork.id);
                synchronized (firstFork) {
                    log("picked up left fork " + firstFork.id);
                    log("trying to pick up right fork " + secondFork.id);
                    synchronized (secondFork) {
                        log("picked up right fork " + secondFork.id);
                        long waitFinish = System.currentTimeMillis();
                        WaitTimeTracker.recordWait(waitFinish - waitStart);
                        eat();
                    }
                }

            } catch (InterruptedException e) {
                log("finishing...");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

class Philosopher1 extends SynchronizedPhilosopher {

    public Philosopher1(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
        firstFork = leftFork;
        secondFork = rightFork;
    }
}

class Philosopher2 extends Philosopher {
    public Philosopher2(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
    }

    @Override
    public void run() {
        while (true) {
            try {
                think();
                long waitStart = System.currentTimeMillis();
                leftFork.lock.lock();
                log("locked left fork " + leftFork.id);
                rightFork.lock.lock();
                log("locked right fork " + leftFork.id);
                long waitFinish = System.currentTimeMillis();
                WaitTimeTracker.recordWait(waitFinish - waitStart);
                eat();
                leftFork.lock.unlock();
                rightFork.lock.unlock();
                log("unlocked both forks");
            } catch (InterruptedException e) {
                log("finishing...");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

class Philosopher3 extends SynchronizedPhilosopher {
    public Philosopher3(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
        if (id % 2 == 0) {
            firstFork = leftFork;
            secondFork = rightFork;
        } else {
            firstFork = rightFork;
            secondFork = leftFork;
        }
    }
}

class Philosopher4 extends SynchronizedPhilosopher {
    public Philosopher4(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
        if (RNG.nextInt(0, 1) == 0) {
            firstFork = leftFork;
            secondFork = rightFork;
        } else {
            firstFork = rightFork;
            secondFork = leftFork;
        }
    }
}

class Philosopher5 extends Philosopher {

    public Philosopher5(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
    }

    @Override
    public void run() {
        while (true) {
            try {
                think();
                long waitStart = System.currentTimeMillis();
                semaphore.acquire();
                log("acquired semaphore");
                log("trying to pick up left fork " + leftFork.id);
                synchronized (leftFork) {
                    log("picked up left fork " + leftFork.id);
                    log("trying to pick up right fork " + rightFork.id);
                    synchronized (rightFork) {
                        log("picked up right fork " + rightFork.id);
                        long waitFinish = System.currentTimeMillis();
                        WaitTimeTracker.recordWait(waitFinish - waitStart);
                        eat();
                        semaphore.release();
                        log("released semaphore");
                    }
                }

            } catch (InterruptedException e) {
                log("finishing...");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

class Philosopher6 extends Philosopher {

    public Philosopher6(int id, Fork leftFork, Fork rightFork) {
        super(id, leftFork, rightFork);
    }

    @Override
    public void run() {
        while (true) {
            try {
                think();
                long waitStart = System.currentTimeMillis();
                if (semaphore.tryAcquire()) {
                    log("acquired semaphore");
                    log("trying to pick up left fork " + leftFork.id);
                    synchronized (leftFork) {
                        log("picked up left fork " + leftFork.id);
                        log("trying to pick up right fork " + rightFork.id);
                        synchronized (rightFork) {
                            log("picked up right fork " + rightFork.id);
                            long waitFinish = System.currentTimeMillis();
                            WaitTimeTracker.recordWait(waitFinish - waitStart);
                            eat();
                            semaphore.release();
                            log("released semaphore");
                        }
                    }
                } else {
                    log("didn't acquire semaphore");
                    log("trying to pick up right fork " + rightFork.id);
                    synchronized (rightFork) {
                        log("picked up right fork " + rightFork.id);
                        log("trying to pick up left fork " + leftFork.id);
                        synchronized (leftFork) {
                            log("picked up left fork " + leftFork.id);
                            long waitFinish = System.currentTimeMillis();
                            WaitTimeTracker.recordWait(waitFinish - waitStart);
                            eat();
                        }
                    }
                }

            } catch (InterruptedException e) {
                log("finishing...");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}

class Main {
    static public void task(int taskNumber, int n) throws InterruptedException{
        ExecutorService executor = Executors.newCachedThreadPool();
        Philosopher.semaphore = new Semaphore(n - 1);

        Fork leftFork = new Fork(0);
        Fork firstFork = leftFork;

        for (int i = 0; i < n; i++) {
            Fork rightFork = new Fork(i + 1);
            if (i == n - 1) {
                rightFork = firstFork;
            }
            Philosopher philosopher = Philosopher.createPhilosopher(taskNumber, i, leftFork, rightFork);
            executor.execute(philosopher);
            leftFork = rightFork;
        }
        Thread.sleep(500);
        executor.shutdownNow();
    }

    public static void main(String[] args) {
        Philosopher.logging = false;
        Philosopher.sleeping = false;
        int n = 150;
        int sims = 5;
        try {
            for (int i = 1; i <= 6; i++) {
                double averageWaitTime = 0;
                for (int j = 0; j < sims; j++) {
                    task(i, n);
                    averageWaitTime += WaitTimeTracker.getAverageWaitTime();
                    WaitTimeTracker.reset();
                }
                averageWaitTime = averageWaitTime / sims;
                System.out.println("Task " + i + ": Average wait time over " + sims + " simulations: " + averageWaitTime + " [ms]");
            }
        } catch (InterruptedException ignored) {}
        System.out.println("Finished simulations");
        System.exit(0);
    }
}