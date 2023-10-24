import com.sun.jdi.ThreadGroupReference;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Processor extends Thread {
    public static Random RNG = new Random();
    private final PizzaBuffer inPizzaBuffer;
    private final PizzaBuffer outPizzaBuffer;
    private final int id;

    public Processor(int id, PizzaBuffer inPizzaBuffer, PizzaBuffer outPizzaBuffer) {
        this.id = id;
        this.inPizzaBuffer = inPizzaBuffer;
        this.outPizzaBuffer = outPizzaBuffer;
    }
    public void run() {
        while (true) {
            try {
                synchronized (inPizzaBuffer) {
                    while (inPizzaBuffer.isEmpty()) inPizzaBuffer.wait();
                    Pizza pizza = inPizzaBuffer.pop();
                    System.out.println("Processor " + id + ": popping pizza no. " + pizza.id + " from buffer no. " + inPizzaBuffer.id);
                    inPizzaBuffer.notifyAll();
                }
                synchronized (outPizzaBuffer) {
                    while (outPizzaBuffer.isFull()) outPizzaBuffer.wait();
                    Pizza newPizza = new Pizza(outPizzaBuffer.getTotalPizzas());
                    outPizzaBuffer.add(newPizza);
                    System.out.println("Processor " + id + ": adding pizza no. " + newPizza.id + " to buffer no. " + outPizzaBuffer.id);
                    outPizzaBuffer.notifyAll();
                }
                TimeUnit.MILLISECONDS.sleep(RNG.nextInt(100, 300));
            } catch (InterruptedException e) {
                System.out.println("Processor " + id + ": ending...");
                break;
            }
        }
    }
}


class Consumer extends Thread {
    public static Random RNG = new Random();
    private final PizzaBuffer pizzaBuffer;
    private final int id;

    public Consumer(int id, PizzaBuffer pizzaBuffer) {
        this.id = id;
        this.pizzaBuffer = pizzaBuffer;
    }

    public void run() {
        while (true) {
            try {
                synchronized (pizzaBuffer) {
                    while (pizzaBuffer.isEmpty()) pizzaBuffer.wait();
                    Pizza pizza = pizzaBuffer.pop();
                    System.out.println("Consumer " + id + ": eating pizza no. " + pizza.id + ".");
                    pizzaBuffer.notifyAll();
                }
                TimeUnit.MILLISECONDS.sleep(RNG.nextInt(100, 300));
            } catch (InterruptedException e) {
                System.out.println("Consumer " + id + ": ending...");
                break;
            }
        }

    }
}

class Producer extends Thread {
    public static Random RNG = new Random();
    private final PizzaBuffer pizzaBuffer;
    private final int id;

    public Producer(int id, PizzaBuffer pizzaBuffer) {
        this.id = id;
        this.pizzaBuffer = pizzaBuffer;
    }

    public void run() {
        while (true) {
            try {
                synchronized (pizzaBuffer) {

                    while (pizzaBuffer.isFull()) pizzaBuffer.wait();
                    Pizza newPizza = new Pizza(pizzaBuffer.getTotalPizzas());
                    pizzaBuffer.add(newPizza);
                    System.out.println("Producer " + id + ": produced pizza no. " + newPizza.id + ".");
                    pizzaBuffer.notifyAll();
                }
                TimeUnit.MILLISECONDS.sleep(RNG.nextInt(100, 300));
            } catch (InterruptedException e) {
                System.out.println("Producer " + id + ": ending...");
                break;
            }
        }
    }
}

class Pizza {
    public final int id;

    public Pizza(int id) {
        this.id = id;
    }
}

class PizzaBuffer {
    private final LinkedList<Pizza> pizzaBuffer = new LinkedList<>();
    public static final int bufferSize = 5;
    public final int id;
    private int totalPizzas = 0;

    public PizzaBuffer(int id) {
        this.id = id;
    }

    public void add(Pizza pizza) {
        pizzaBuffer.add(pizza);
        totalPizzas++;
    }

    public Pizza pop() {
        return pizzaBuffer.pop();
    }

    public boolean isEmpty() {
        return pizzaBuffer.isEmpty();
    }

    public boolean isFull() {
        return pizzaBuffer.size() >= bufferSize;
    }

    public int getTotalPizzas() {
        return totalPizzas;
    }
}

class Main {
    private static void task1() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        PizzaBuffer pizzaBuffer = new PizzaBuffer(0);

        for (int i = 0; i < 5; i++) {
            executor.execute(new Producer(i, pizzaBuffer));
        }
        for (int i = 0; i < 5; i++) {
            executor.execute(new Consumer(i, pizzaBuffer));
        }
        Thread.sleep(10000);
        executor.shutdownNow();
    }

    private static void task2() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        
        PizzaBuffer previousBuffer = new PizzaBuffer(0);
        PizzaBuffer currentBuffer = null;
        executor.execute(new Producer(0, previousBuffer));
        for (int i = 1; i < 9; i++) {
            currentBuffer = new PizzaBuffer(i);
            executor.execute(new Processor(i, previousBuffer, currentBuffer));
            previousBuffer = currentBuffer;
        }
        executor.execute(new Consumer(9, currentBuffer));
        
        Thread.sleep(10000);
        executor.shutdownNow();
    }

    public static void main(String[] args) throws InterruptedException {
//        task1();
        task2();
    }
}
