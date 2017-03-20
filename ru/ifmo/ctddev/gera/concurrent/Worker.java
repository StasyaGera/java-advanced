package ru.ifmo.ctddev.gera.concurrent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by penguinni on 17.03.17.
 */
abstract class Worker<T, U> implements Runnable {
    protected U result;
    protected List<? extends T> data;

    Worker(List<? extends T> data) {
        this.data = data;
    }

    abstract U getFinalResult(List<Worker<T, U>> workers);

    protected U getResult() {
        return result;
    }
    protected List<U> collectResults(List<Worker<T, U>> workers) {
        return toStream(workers).map(Worker::getResult).collect(Collectors.toList());
    }

    protected <W> Stream<W> toStream(List<W> list) {
        return list.stream();
    }
}
