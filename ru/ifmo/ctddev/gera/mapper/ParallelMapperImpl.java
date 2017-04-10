package ru.ifmo.ctddev.gera.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Created by penguinni on 22.03.17.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<MapTask<?, ?>> tasks = new LinkedList<>();
    private final Thread managerThread;
    private final Manager manager;

    public ParallelMapperImpl(int threads) throws InterruptedException {
        manager = new Manager(tasks, threads);
        managerThread = new Thread(manager);
        managerThread.start();
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        MapTask<T, R> curr = new MapTask<>(f, args);
        synchronized (tasks) {
            tasks.add(curr);
            tasks.notify();
        }
        return curr.getResult();
    }

    public void close() throws InterruptedException {
        managerThread.interrupt();
        manager.close();
    }
}
