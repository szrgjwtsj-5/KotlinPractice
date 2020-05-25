package test;

import java.util.*;

public class ProducerCustomerTest {

    public static void main(String[] args) {
        ProducerCustomerTest test = new ProducerCustomerTest();

        for (int i = 0; i < 5; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    test.produce();
                }
            };
            t.start();
        }
        for (int i = 0; i < 5; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    test.custom();
                }
            };
            t.start();
        }
    }

    private Queue<String> things = new ArrayDeque<>();
    private int size = 5;

    public synchronized void produce() {
        while (true) {
            if (things.size() < size) {
                System.out.println("produce");
                things.offer("hhh " + (new Random().nextInt()));
            } else {
                try {
                    this.wait();
                    this.notifyAll();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public synchronized void custom() {
        while (things.size() > 0) {
            System.out.println(things.poll());
        }
        try {
            this.wait();
            this.notifyAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
