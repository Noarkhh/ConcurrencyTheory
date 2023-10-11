
class DoubleWriter extends Thread {
    public static double myDouble = 0.0;
    public double doubleToAdd;

    DoubleWriter(double doubleToAdd) {
        this.doubleToAdd = doubleToAdd;
    }

    public void run() {
        myDouble = doubleToAdd;
    }
}

class Main3 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start");

        DoubleWriter bigDoubleWriter = new DoubleWriter(Double.MAX_VALUE);
        DoubleWriter smallDoubleWriter = new DoubleWriter(Double.MIN_NORMAL);
        bigDoubleWriter.start();
        smallDoubleWriter.start();
        bigDoubleWriter.join();
        smallDoubleWriter.join();

        System.out.println(getBinary(Double.MAX_VALUE));
        System.out.println(getBinary(Double.MIN_NORMAL));
        System.out.println(getBinary(DoubleWriter.myDouble));

        System.out.println("End");
    }

    public static String getBinary(double doubleToConvert) {
        long bits = Double.doubleToLongBits(doubleToConvert);
        return String.format("%64s", Long.toBinaryString(bits)).replace(" ", "0");
    }
}