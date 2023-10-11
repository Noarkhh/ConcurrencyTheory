import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class IdentityClass {
    public int val = 0;
    public int identityFunction() {
        synchronized (this) {
            val--;
            val++;
            return val;
        }
    }
}

class IdentityThread extends Thread {
    IdentityClass identityClass;
    int n;

    IdentityThread(IdentityClass identityClass, int n) {
        this.identityClass = identityClass;
        this.n = n;
    }

    public void run() {
        for (int i = 0; i < n; i++) {
            int result = identityClass.identityFunction();
            if (result != 0) {
                System.out.println("dupa: " + result + ", " + Thread.currentThread().getId());
                break;
            }
        }
//        System.out.println("dupa2: " + Thread.currentThread().getId());
    }
}

class Main0 {
    public static void main(String[] args) {
        System.out.println("Start");
        IdentityClass identityClass = new IdentityClass();
        Executor executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) {
            IdentityThread identityThread = new IdentityThread(identityClass, 1000);
//            new Thread(identityThread).start();
            executor.execute(identityThread);
        }
        System.out.println("End");
    }
}
