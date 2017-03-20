package ru.ifmo.ctddev.gera.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by penguinni on 17.03.17.
 */
public class IterativeParallelism implements ListIP {
    private <T, U> U runThem(int amount,
                             List<? extends T> values,
                             Function<List<? extends T>, Worker<T, U>> ctor) throws InterruptedException {
        List<Worker<T, U>> workers = new ArrayList<>();
        int pack = values.size() / amount,
            mod = values.size() % amount,
            start = 0, end;

        for (int i = 0; i < amount; i++) {
            if (start == values.size()) {
                break;
            }
            end = Math.min(start + pack, values.size()) + (mod-- > 0 ? 1 : 0);
            workers.add(ctor.apply(values.subList(start, end)));
            start = end;
        }

        List<Thread> threads = new ArrayList<>();
        for (Worker<T, U> w : workers) {
            Thread t = new Thread(w);
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        return ctor.apply(Collections.emptyList()).getFinalResult(workers);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return runThem(threads, values, (v) -> new Max<>(v, comparator));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return runThem(threads, values, (v) -> new All<>(v, predicate));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return runThem(threads, values, (v) -> new Join<>(v));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return runThem(threads, values, (v) -> new Filter<>(v, predicate));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return runThem(threads, values, (v) -> new Map<>(v, f));
    }
}
