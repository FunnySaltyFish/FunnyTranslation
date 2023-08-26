package com.funny.translation.translate;

public class PrintNumbers {
    private static final Object lock = new Object();
    private static int currentNumber = 1;
    private static final int LIMIT = 100;

    public static void main(String[] args) {
        Thread thread1 = new Thread(new PrintNumberRunnable(1));
        Thread thread2 = new Thread(new PrintNumberRunnable(2));
        Thread thread3 = new Thread(new PrintNumberRunnable(0));

        thread1.start();
        thread2.start();
        thread3.start();
    }

    static class PrintNumberRunnable implements Runnable {
        private int targetMod = 0;
        PrintNumberRunnable(int targetMod) {
            this.targetMod = targetMod;
        }

        @Override
        public void run() {
            while (currentNumber <= LIMIT) {
                synchronized (lock) {
                    int mod = currentNumber % 3;
                    if (mod == targetMod) {
                        print();
                        currentNumber += 1;
                        lock.notifyAll();
                    } else {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        private void print() {
            System.out.println("Thread " + targetMod + ", currentNumber = " + currentNumber);
        }
    }
}
