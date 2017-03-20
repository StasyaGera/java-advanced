package ru.ifmo.ctddev.gera.arrayset;

import java.util.*;

/**
 * Created by penguinni on 16.02.17
 * penguinni hopes it will work.
 */
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private List<E> data;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this((Comparator<E>) null);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(SortedSet<E> sortedSet) {
        this(sortedSet, sortedSet.comparator());
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this.comparator = comparator;

        data = new ArrayList<>();
        List<E> temp = new ArrayList<>(collection);
        temp.sort(comparator);

        for (E element : temp) {
            if (data.isEmpty() || !equal(data.get(data.size() - 1), element)) {
                data.add(element);
            }
        }

        data = Collections.unmodifiableList(data);
    }

    private ArraySet(List<E> data, Comparator<? super E> comparator) {
        this.data = data;
        this.data = Collections.unmodifiableList(data);
        this.comparator = comparator;
    }

    private boolean equal(E e1, E e2) {
        if ((e1 == null) || (e2 == null)) {
            return (e1 == null) && (e2 == null);
        }

        if (comparator == null) {
            return e1.equals(e2);
        } else {
            return comparator.compare(e1, e2) == 0;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    private int getIndex(E e, boolean including) {
        int i = Collections.binarySearch(data, e, comparator);
        if (i < 0) {
            return -i - 1;
        }
        if (!including) {
            i++;
        }
        return i;
    }

    @Override
    public E lower(E e) {
        int i = getIndex(e, true);
        return i != 0 ? data.get(i - 1) : null;
    }

    @Override
    public E floor(E e) {
        int i = getIndex(e, false);
        return i != 0 ? data.get(i - 1) : null;
    }

    @Override
    public E ceiling(E e) {
        int i = getIndex(e, true);
        return i != size() ? data.get(i) : null;
    }

    @Override
    public E higher(E e) {
        int i = getIndex(e, false);
        return i != size() ? data.get(i) : null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("pollFirst");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("pollLast");
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversableList<>(data, true), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E e1, boolean including1, E e2, boolean including2) {
        return headSet(e2, including2).tailSet(e1, including1);
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean including) {
        return new ArraySet<>(data.subList(getIndex(e, including), size()), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean including) {
        return new ArraySet<>(data.subList(0, getIndex(e, !including)), comparator);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E e, E e1) {
        return subSet(e, true, e1, false);
    }

    @Override
    public SortedSet<E> headSet(E e) {
        return headSet(e, false);
    }

    @Override
    public SortedSet<E> tailSet(E e) {
        return tailSet(e, true);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }
        return data.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }
        return data.get(size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    private class ReversableList<T> extends AbstractList<T> implements RandomAccess {
        private boolean reversed = false;
        private List<T> data;

        ReversableList(List<T> other, boolean reversed) {
            if (other instanceof ReversableList) {
                this.data = ((ReversableList<T>) other).data;
                this.reversed = ((ReversableList) other).reversed;
            } else {
                this.data = other;
            }

            if (reversed) {
                this.reversed = !this.reversed;
            }
        }

        ReversableList(Collection<? extends T> collection) {
            this.data = new ArrayList<>(collection);
        }

        @Override
        public T get(int i) {
            if (reversed) {
                return this.data.get(size() - 1 - i);
            }
            return this.data.get(i);
        }

        @Override
        public T set(int i, T t) {
            return data.set(i, t);
        }

        @Override
        public int size() {
            return this.data.size();
        }
    }
}