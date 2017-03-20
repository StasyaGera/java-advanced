package ru.ifmo.ctddev.gera.concurrent;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by penguinni on 20.03.17.
 */
public class Filter<T> extends ConcatCollectible<T, T> {
    private Predicate<? super T> predicate;

    Filter(List<? extends T> data, Predicate<? super T> predicate) {
        super(data);
        this.predicate = predicate;
    }

    @Override
    public void run() {
        result = toStream(data).filter(predicate).collect(Collectors.toList());
    }
}
