package ProjectA;

import java.util.concurrent.atomic.AtomicBoolean;

public class MinHeapTest {

    private static final int NUM_THREADS = 9;
    private static final boolean USE_SAFE_VERSION = false;
    private static final AtomicBoolean passingMultiThreadTest = new AtomicBoolean(true);

    public static void SingleThreadedTests(MinHeapInterface heap) {
        System.out.println("Single-threaded tests...");
        heap.insert(5);
        heap.insert(3);
        heap.insert(7);
        heap.insert(1);

        assert heap.checkRepInvariant() : "Invariant failed after insertions";

        assert heap.contains(3);
        assert heap.contains(1);
        assert !heap.contains(9);

        int min = heap.removeMin();
        assert min == 1 : "Expected 1, got " + min;
        assert heap.checkRepInvariant() : "Invariant failed after removeMin";
        System.out.println("\tSingle-threaded tests passed.");
    }

    public static void InvariantCorruptionTest(MinHeapInterface heap) {
        System.out.println("Testing corruption...");
        heap.insert(2);
        heap.insert(4);
        heap.insert(6);
        assert heap.checkRepInvariant() : "Invariant should hold before corruption";

        heap.corrupt(0); // Break the heap
        assert !heap.checkRepInvariant() : "Invariant should FAIL after corruption";
        System.out.println("\tCorruption test passed.");
    }

    public static void invariantCheckOffTest(MinHeapInterface heap) {
        System.out.println("Toggling off rep invariant...");
        heap.setInvariantCheck(false);
        assert heap.checkRepInvariant() : "checkRepInvariant() should always return true when disabled";
        System.out.println("\tinvariantCheckOffTest test passed.");
    }

    public static void multiThreadTests(MinHeapInterface heap) {
        Runnable inserter = () -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    heap.insert((int) (Math.random() * 1000));
                    tryWait();
                }
            } catch (Throwable t) {
                passingMultiThreadTest.set(false);
                System.err.println("[Insert Thread] " + t);
            }
        };

        Runnable remover = () -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    heap.removeMin();
                    tryWait();
                }
            } catch (Throwable t) {
                passingMultiThreadTest.set(false);
                System.err.println("[Remove Thread] " + t);
            }
        };

        Runnable checker = () -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    heap.contains((int) (Math.random() * 1000));
                    tryWait();
                }
            } catch (Throwable t) {
                passingMultiThreadTest.set(false);
                System.err.println("[Check Thread] " + t);
            }
        };

        Thread[] threads = new Thread[NUM_THREADS];
        heap.setInvariantCheck(true);
        makeAndRunThreads(threads, inserter, inserter, inserter);
        makeAndRunThreads(threads, inserter, remover, checker);

        assert heap.checkRepInvariant() : "Invariant failed after multi-threaded operations";
        assert passingMultiThreadTest.get() : "Failed Multi-threaded test (rep invariant broken or thread exception).";

        System.out.println("Multi-threaded test passed. Total size: " + heap.size());
    }

    private static void makeAndRunThreads(Thread[] threads, Runnable r1, Runnable r2, Runnable r3) {
        for (int i = 0; i < threads.length; i++) {
            switch (i % 3) {
                case 0:
                    threads[i] = new Thread(r1);
                    break;
                case 1:
                    threads[i] = new Thread(r2);
                    break;
                default:
                    threads[i] = new Thread(r3);
                    break;
            }
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                passingMultiThreadTest.set(false);
                System.err.println("Interrupted during multiThreadTest");
            }
        }
    }

    private static void tryWait() {
        try {
            Thread.sleep((int) (Math.random() * 5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        MinHeapInterface heap;
        if (USE_SAFE_VERSION) {
            heap = new ConcurrentMinHeap();
        } else {
            heap = new MinHeap_unsafe();
        }

        System.out.println("Single threaded tests...\n");

        SingleThreadedTests(heap);
        InvariantCorruptionTest(heap);
        invariantCheckOffTest(heap);

        System.out.println("\nAll single threaded tests passed.\n\n");

        System.out.println("Multi-threaded test...\n");
        if (!USE_SAFE_VERSION) {
            System.out.println("[WARNING] Using the unsafe version of MinHeap. This should almost always fail the multiThreadTests.\n");
        }

        multiThreadTests(heap);

        if (passingMultiThreadTest.get()) {
            System.out.println("\nAll tests completed successfully.\n\n");
        } else {
            System.out.println("\n[FAILURE] Multi-threaded test did not pass.\n\n");
        }
        System.out.println("Done.");
    }
}
