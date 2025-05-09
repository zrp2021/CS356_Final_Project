package ProjectA;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentMinHeap implements MinHeapInterface {

    private int[] heap;
    private int size;
    private final ReentrantLock lock = new ReentrantLock();
    private static final int INITIAL_CAPACITY = 16;
    private boolean checkRepInvariant = true;

    public ConcurrentMinHeap() {
        heap = new int[INITIAL_CAPACITY];
        size = 0;
    }

    @Override
    public void insert(int val) {
        lock.lock();
        try {
            if (size == heap.length) {
                resize();
            }
            heap[size] = val;
            heapifyUp(size);
            size++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int removeMin() {
        lock.lock();
        try {
            if (size == 0) {
                throw new IllegalStateException("Heap is empty");
            }
            int min = heap[0];
            heap[0] = heap[size - 1];
            size--;
            heapifyDown(0);
            return min;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(int val) {
        lock.lock();
        try {
            for (int i = 0; i < size; i++) {
                if (heap[i] == val) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean checkRepInvariant() {
        lock.lock();
        try {
            if (!checkRepInvariant) {
                return true;
            }
            for (int i = 0; i < size / 2; i++) {
                int left = 2 * i + 1;
                int right = 2 * i + 2;
                if (left < size && heap[i] > heap[left]) {
                    return false;
                }
                if (right < size && heap[i] > heap[right]) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void corrupt(int index) {
        lock.lock();
        try {
            if (index < size) {
                heap[index] = new Random().nextInt(100000); // potentially breaks min-heap
            }
        } finally {
            lock.unlock();
        }
    }

    private void heapifyUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap[i] < heap[parent]) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    private void heapifyDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;
            if (left < size && heap[left] < heap[smallest]) {
                smallest = left;
            }
            if (right < size && heap[right] < heap[smallest]) {
                smallest = right;
            }
            if (smallest != i) {
                swap(i, smallest);
                i = smallest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        int temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void resize() {
        heap = Arrays.copyOf(heap, heap.length * 2);
    }

    @Override
    public void setInvariantCheck(boolean on) {
        this.checkRepInvariant = on;
    }

    @Override
    public int size() {
        return size;
    }
}
