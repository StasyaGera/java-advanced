package ru.ifmo.ctddev.gera.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by penguinni on 19.03.17.
 */
public class Map<T, U> extends ConcatCollectible<T, U> {
    private Function<? super T, ? extends U> f;

    Map(List<? extends T> data, Function<? super T, ? extends U> f) {
        super(data);
        this.f = f;
        result = new ArrayList<>();
    }

    @Override
    public void run() {
        toStream(data).forEachOrdered((t) -> result.add(f.apply(t)));
    }
}