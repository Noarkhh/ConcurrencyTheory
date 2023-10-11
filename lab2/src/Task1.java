class RevolverUser extends Thread {
    private static boolean wasFirstShotShot = false;
    public int countdown;
    RevolverUser(int countdown) {
        this.countdown = countdown;
    }

    public void run() {
        for (int i = countdown; i > 0; i--) {
            if (wasFirstShotShot) return;
            System.out.println("id: " + Thread.currentThread().threadId() + ", count: " + i);
        }
//        synchronized (this) {
        if (!wasFirstShotShot) {
            wasFirstShotShot = true;
            System.out.println("id: " + Thread.currentThread().threadId() + ", Pif! Paf!");
        }
//        }
    }
}

class Main1 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start");
        for (int i = 0; i < 10; i++) {
            RevolverUser revolverUser = new RevolverUser(5);
            revolverUser.start();
        }
        System.out.println("End");
    }
}