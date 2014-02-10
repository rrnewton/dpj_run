package DPJRuntime;

import java.util.*;

public interface DPJSequentialSet<E> extends Set<E> {
    
    @Override()
    boolean add(E e);
    
    @Override()
    boolean addAll(Collection<? extends E> c);
    
    @Override()
    void clear();
    
    @Override()
    boolean contains(Object o);
    
    @Override()
    boolean containsAll(Collection<?> c);
    
    @Override()
    boolean equals(Object o);
    
    @Override()
    int hashCode();
    
    @Override()
    boolean isEmpty();
    
    @Override()
    Iterator<E> iterator();
    
    @Override()
    boolean remove(Object o);
    
    @Override()
    boolean removeAll(Collection<?> c);
    
    @Override()
    boolean retainAll(Collection<?> c);
    
    @Override()
    int size();
    
    @Override()
    Object[] toArray();
    
    @Override()
    <T>T[] toArray(T[] a);
}
