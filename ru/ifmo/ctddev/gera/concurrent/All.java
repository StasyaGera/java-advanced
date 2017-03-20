package ru.ifmo.ctddev.gera.concurrent;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by penguinni on 17.03.17.
 */
public class All<T> extends SelfCollectible<T, Boolean> {
    private Predicate<? super T> predicate;

    All(List<? extends T> data, Predicate<? super T> predicate) {
        super(data);
        this.predicate = predicate;
    }

    @Override
    public void run() {
        result = toStream(data).allMatch(predicate);
    }

    @Override
    Boolean getFinalResult(List<Worker<T, Boolean>> workers) {
        return getFinalResult(workers, new All<>(Collections.emptyList(), Predicate.isEqual(true)));
    }
}
