# CS356_Final_Project

Make sure to run with asserts enabled (include -ea when running from terminal)!!!

I worked on this project independently.

## Project Summary

This project implements a concurrent priority queue using a binary **Min-Heap** structure, allowing multiple threads to safely and efficiently add, remove, and inspect elements. The implementation supports concurrency using Java’s `ReentrantLock`, with full support for representation invariant checking and deterministic failure testing via an `unsafe` version.

## Data Structure

* **Type**: Binary Min-Heap
* **Storage**: Array-based, dynamic resizing
* **Supported Operations**:

  * `insert(int val)`
  * `removeMin()`
  * `contains(int val)`
  * `checkRepInvariant()`
  * `corrupt(int index)`
  * `setInvariantCheck(boolean)`

## Concurrency Strategy

* **Thread-Safe Version**: `ConcurrentMinHeap`

  * All public methods are guarded by a `ReentrantLock`
  * The lock ensures atomicity of heap operations and protects against race conditions
  * Representation invariant checks are also synchronized to avoid inconsistency during test conditions
* **Unsafe Version**: `MinHeap_unsafe`

  * Implements the same heap logic without any locking or synchronization
  * Used to intentionally expose concurrency errors during testing

## Invariant Checking

The representation invariant for the Min-Heap is:

* For all parent nodes `i`, `heap[i] <= heap[2i+1]` and `heap[i] <= heap[2i+2]` if the children exist.
* This invariant is checked after mutating operations or on-demand in tests.
* A `corrupt()` method allows deliberate heap corruption to test invariant-checking mechanisms.

## Test Design

* **Single-threaded tests** verify correctness under sequential use
* **Corruption test** ensures `checkRepInvariant()` detects malformed structures
* **Multi-threaded tests** simulate concurrent insertion, removal, and querying with multiple threads:

  * Threads catch all `Throwable` instances, including `AssertionError`
  * Shared `AtomicBoolean` tracks whether any thread encountered failure
  * Alternates between using safe and unsafe versions for comparative behavior

## Concurrency Guarantees

* **Deadlock-free**: Single lock is always acquired in well-scoped blocks
* **Starvation-free**: Uses Java's fair scheduler and no conditional waits
* **Race-free**: All shared state access is synchronized

## Known Limitations

* Unsafe version may not expose issues unless assertions are enabled via `-ea`

## Interesting Aspects

* The ability to flip between safe/unsafe versions to demonstrate races
* Catching thread failures cleanly with centralized control via `AtomicBoolean`
* Stress testing for concurrency with visible invariant failures in the unsafe version

## File Structure

* `ConcurrentMinHeap.java`: Safe heap with locking
* `MinHeap_unsafe.java`: Unsafe heap used for race condition demos
* `MinHeapTest.java`: Comprehensive test harness
* `MinHeapInterface.java`: Shared heap API

## Build Notes

Enable assertions at runtime to expose failure modes:

```bash
java -ea ProjectA.MinHeapTest
```

You can switch between safe and unsafe heaps by setting `USE_SAFE_VERSION` in `MinHeapTest.java`.

## Conclusion

This project highlights the importance of synchronization in concurrent data structures. By contrasting a correct and incorrect implementation, it not only demonstrates correct concurrent behavior but also educates on failure modes through deterministic test cases.
