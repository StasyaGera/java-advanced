package ru.ifmo.ctddev.gera.concurrent;

import java.util.Collections;
import java.util.List;

/**
 * Created by penguinni on 20.03.17.
 */
public class Join<T> extends SelfCollectible<T, String> {
    Join(List<? extends T> data) {
        super(data);
        result = "";
    }

    @Override
    public void run() {
        result = toStream(data).map(Object::toString).reduce(String::concat).orElse(null);
    }

    @Override
    String getFinalResult(List<Worker<T, String>> workers) {
        return getFinalResult(workers, new Join<>(Collections.emptyList()));
    }
}
