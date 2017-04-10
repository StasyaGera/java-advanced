package ru.ifmo.ctddev.gera.mapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Created by penguinni on 24.03.17.
 */
class MapTask<T, R> {
    private final Function<? super T, ? extends R> f;
    private final List<? extends T> task;
    private final List<R> result = new ArrayList<>();

    private final Object waiter = new Object();
    private final int taskSize;
    private Integer counter = 0;
//    private final CountDownLatch counter;

    MapTask(Function<? super T, ? extends R> f, List<? extends T> task) {
        this.f = f;
        this.task = task;
        taskSize = task.size();
//        counter = new CountDownLatch(taskSize);
        for (int i = 0; i < taskSize; i++) {
            result.add(null);
        }
    }

    List<R> getResult() throws InterruptedException {
//        counter.await();
        synchronized (waiter) {
            while (counter != taskSize) {
                waiter.wait();
            }
        }
        return result;
    }


    List<Runnable> split() {
        List<Runnable> res = new LinkedList<>();
        for (int i = 0; i < taskSize; i++) {
            final int j = i;
            res.add(() -> {
                result.set(j, f.apply(task.get(j)));
                synchronized (counter) {
                    counter++;
                }
//                System.err.println("counter:" + counter);
                if (counter == taskSize) {
                    synchronized (waiter) {
                        waiter.notify();
                    }
                }
//                counter.countDown();
            });
        }
        return res;
    }
}
