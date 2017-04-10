package ru.ifmo.ctddev.gera.mapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by penguinni on 26.03.17.
 */
class Manager implements Runnable {
    private final Queue<MapTask<?, ?>> globalTasks;
    private final Queue<Runnable> subTasks = new LinkedList<>();
    private final List<Thread> threads = new ArrayList<>();

    public Manager(Queue<MapTask<?, ?>> globalTasks, int threads) {
        for (int i = 0; i < threads; i++) {
            this.threads.add(new WorkerThread(subTasks));
        }
        this.globalTasks = globalTasks;
    }

    static <T> T waitForTasks(Queue<T> tasks) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            return tasks.poll();
        }
    }

    @Override
    public void run() {
        threads.forEach(Thread::start);

        while (true) {
            MapTask<?, ?> next;
            try {
                next = waitForTasks(globalTasks);
            } catch (InterruptedException e) {
                break;
            }

            synchronized (subTasks) {
                subTasks.addAll(next.split());
                subTasks.notifyAll();
            }
        }
    }

    public void close() {
        threads.forEach(Thread::interrupt);
    }

    private class WorkerThread extends Thread {
        private final Queue<Runnable> subTasks;

        WorkerThread(Queue<Runnable> subTasks) {
            this.subTasks = subTasks;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    waitForTasks(subTasks).run();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
