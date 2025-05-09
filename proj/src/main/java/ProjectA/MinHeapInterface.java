package ProjectA;

public interface MinHeapInterface {
    public void insert(int val);
    public int removeMin();
    public boolean contains(int val);
    public boolean checkRepInvariant();
    public void corrupt(int index);
    public void setInvariantCheck(boolean on);
    public int size();
}
