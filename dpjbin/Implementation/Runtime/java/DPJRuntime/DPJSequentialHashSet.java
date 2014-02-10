package DPJRuntime;

import java.util.*;

public class DPJSequentialHashSet<E> extends HashSet<E> implements DPJSequentialSet<E> {
    
    public DPJSequentialHashSet() {
        super();
    }
    
    @Override()
    public boolean add(E e) {
        return super.add(e);
    }
    
    @Override()
    public boolean addAll(Collection<? extends E> c) {
        return super.addAll(c);
    }
    
    @Override()
    public void clear() {
        super.clear();
    }
    
    @Override()
    public boolean contains(Object o) {
        return super.contains(o);
    }
    
    @Override()
    public boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }
    
    @Override()
    public boolean equals(Object o) {
        return super.equals(o);
    }
    
    @Override()
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override()
    public boolean isEmpty() {
        return super.isEmpty();
    }
    
    @Override()
    public Iterator<E> iterator() {
        return super.iterator();
    }
    
    @Override()
    public boolean remove(Object o) {
        return super.remove(o);
    }
    
    @Override()
    public boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }
    
    @Override()
    public boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }
    
    @Override()
    public int size() {
        return super.size();
    }
    
    @Override()
    public Object[] toArray() {
        return super.toArray();
    }
    
    @Override()
    public <T>T[] toArray(T[] a) {
        return super.toArray(a);
    }
}
