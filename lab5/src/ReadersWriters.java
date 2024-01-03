import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class LetterLinkedList {
    public Node head;
    public Node tail;
    public static Random RNG = new Random();
    public static String alphabet = "abcdefghijklmnop";

    public LetterLinkedList(int size) {
        Node currentNode = null;

        for (int i = 0; i < size; i++) {
            Node newNode = new Node(null, currentNode, randomLetter());
            if (currentNode != null) currentNode.next = newNode;
            else head = newNode;
            currentNode = newNode;
        }
        tail = currentNode;
    }

    public static char randomLetter() {
        return alphabet.charAt(RNG.nextInt(0, alphabet.length() - 1));
    }

    public String toString() {
        Node current = head;
        StringBuilder s = new StringBuilder("head");
        while (current != null) {
            s.append(" -> ").append(current.letter);
            current = current.next;
        }
        return s.toString();
    }

    public Boolean contains(char letter) throws InterruptedException {
        Reader reader = new Reader(this, letter);
        reader.start();
//        reader.join();
        return reader.result;
    }

    public Boolean remove(char letter) throws InterruptedException {
        Remover remover = new Remover(this, letter);
        remover.start();
//        remover.join();
        return remover.result;
    }

    public void add(char letter) throws InterruptedException {
        Adder adder = new Adder(this, letter);
        adder.start();
//        adder.join();
    }
}

class Node {
    public Node next;
    public Node previous;
    public char letter;
    public ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Node(Node next, Node previous, char letter) {
        this.next = next;
        this.previous = previous;
        this.letter = letter;
    }
}

class Adder extends Thread {
    public Node current;
    public final LetterLinkedList list;
    public char letterToAdd;

    public Adder(LetterLinkedList list, char letterToAdd) {
        this.list = list;
        current = list.head;
        this.letterToAdd = letterToAdd;
    }

    public void run() {
        current = list.head;
        current.lock.readLock().lock();
        while (current.next != null) {
            current.next.lock.readLock().lock();
            System.out.println("stepping..");
            current = current.next;
            current.previous.lock.readLock().unlock();
        }
        current.next = new Node(null, current, letterToAdd);
        current.lock.readLock().unlock();
    }
}

class Remover extends Thread {
    public Node current;
    public final LetterLinkedList list;
    public char searchedLetter;

    public Boolean result;

    public Remover(LetterLinkedList list, char searchedLetter) {
        this.list = list;
        current = list.head;
        this.searchedLetter = searchedLetter;
    }

    public void run() {
        result = null;
        current = list.head;
        current.lock.readLock().lock();
        while (current.next != null) {
            current.next.lock.readLock().lock();
            System.out.println("stepping..");
            current = current.next;
            if (current.letter == searchedLetter) {
                current.previous.lock.writeLock().lock();
                current.previous.lock.readLock().unlock();
                current.lock.writeLock().lock();
                current.next.lock.writeLock().lock();
                current.previous.next = current.next;
                current.next.previous = current.previous;
                current.next.lock.writeLock().unlock();
                current.lock.writeLock().unlock();
                current.previous.lock.writeLock().unlock();
                result = true;
                return;
            }
            current.previous.lock.readLock().unlock();
        }
        result = false;
    }
}

class Reader extends Thread {
    public Node current;
    public final LetterLinkedList list;
    public char searchedLetter;

    public Boolean result;

    public Reader(LetterLinkedList list, char searchedLetter) {
        this.searchedLetter = searchedLetter;
        this.list = list;
    }

    public void run() {
        result = null;
        current = list.head;
        current.lock.readLock().lock();
        while (current.next != null) {
            current.next.lock.readLock().lock();
            System.out.println("stepping..");
            current = current.next;
            current.previous.lock.readLock().unlock();
            if (current.letter == searchedLetter) {
                current.lock.readLock().unlock();
                result = true;
                return;
            }
        }
        if (current.letter == searchedLetter) result = true;
        current.lock.readLock().unlock();
        result = false;
    }
}

class Main {
    public static void main(String[] args) throws InterruptedException {
        LetterLinkedList letterLinkedList = new LetterLinkedList(10);
        System.out.println(letterLinkedList);
        for (int i = 0; i < 10; i++) {
//            switch (LetterLinkedList.RNG.nextInt(3)) {
//                case 0 -> letterLinkedList.contains(LetterLinkedList.randomLetter());
//                case 1 -> letterLinkedList.remove(LetterLinkedList.randomLetter());
//                case 2 -> letterLinkedList.add(LetterLinkedList.randomLetter());
//            }
            letterLinkedList.add(LetterLinkedList.randomLetter());
        }

        Thread.sleep(400);
        System.out.println(letterLinkedList);

    }
}


