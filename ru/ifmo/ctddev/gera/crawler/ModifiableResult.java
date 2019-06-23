package ru.ifmo.ctddev.gera.crawler;

import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by penguinni on 08.05.17.
 */
public class ModifiableResult {
    public ModifiableResult() {
        this.downloaded = Collections.synchronizedList(new ArrayList<>());
        this.errors = new ConcurrentHashMap<>();
    }

    ModifiableResult(Result result) {
        this.downloaded = Collections.synchronizedList(result.getDownloaded());
        this.errors = new ConcurrentHashMap<>(result.getErrors());
    }

    private final Set<String> visited = Collections.synchronizedSet(new TreeSet<>());
    private final List<String> downloaded;
    private final Map<String, IOException> errors;

    public List<String> getDownloaded() {
        return downloaded;
    }

    public Map<String, IOException> getErrors() {
        return errors;
    }

    Set<String> getVisited() {
        return visited;
    }
}
