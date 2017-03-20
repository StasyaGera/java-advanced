package ru.ifmo.ctddev.gera.concurrent;

import java.util.List;

/**
 * Created by penguinni on 20.03.17.
 */
abstract class SelfCollectible<T, U> extends Worker<T, U> {
    SelfCollectible(List<? extends T> data) {
        super(data);
    }

    U getFinalResult(List<Worker<T, U>> workers, Worker<U, U> collector) {
        collector.data = collectResults(workers);
        collector.run();
        return collector.getResult();
    }
}
