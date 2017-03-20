package ru.ifmo.ctddev.gera.concurrent;

import java.util.Comparator;
import java.util.List;

/**
 * Created by penguinni on 17.03.17.
 */
public class Max<T> extends SelfCollectible<T, T> {
    private Comparator<? super T> comparator;

    Max(List<? extends T> data, Comparator<? super T> comparator) {
        super(data);
        this.comparator = comparator;
    }

    @Override
    public void run() {
        result = toStream(data).max(comparator).orElse(null);
    }

    @Override
    T getFinalResult(List<Worker<T, T>> workers) {
        return getFinalResult(workers, this);
    }
}
