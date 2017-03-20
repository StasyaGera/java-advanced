package ru.ifmo.ctddev.gera.concurrent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by penguinni on 20.03.17.
 */
abstract class ConcatCollectible<T, U> extends Worker<T, List<U>> {
    ConcatCollectible(List<? extends T> data) {
        super(data);
    }

    @Override
    List<U> getFinalResult(List<Worker<T, List<U>>> workers) {
        Stream<Stream<U>> results = toStream(collectResults(workers)).map(List::stream);
        return results.reduce(Stream::concat).orElse(null).collect(Collectors.toList());
    }
}
