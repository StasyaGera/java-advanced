package ru.ifmo.ctddev.gera.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * Recursively walks web pages starting with the given one.
 *
 * This class downloads the page, extracts all the links in it and then
 * does the same for each of the extracted links.
 * Downloads and extractions are performed in parallel.
 * You can specify how deep to go, how many downloader and extractor threads to use
 * and the maximum amount of simultaneous downloads from one host.
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadService, extractService;
    private final int perHost;

    private final Map<String, Protector> cached = new ConcurrentHashMap<>();
    private final Map<String, Semaphore> hosts = new ConcurrentHashMap<>();

    private static final int SIZE = Integer.MAX_VALUE;
    private final Semaphore semaphore = new Semaphore(SIZE);

    /**
     * Recursively downloads the pages using {@link CachingDownloader}.
     *
     * @param args  a string of a word and three integers in the same order as listed below:
     *              <ul>
     *                  <li><tt>link</tt> - a URL address of the link to start from</li>
     *                  <li><tt>downloaders</tt> - maximum amount of downloading threads</li>
     *                  <li><tt>extractors</tt> - maximum amount of extracting threads</li>
     *                  <li><tt>perHost</tt> - maximum amount of simultaneous requests for one host</li>
     *              </ul>
     */
    public static void main(String[] args) {
        int downloads = 1, extractors = 1, perHost = 1;
        try {
            switch (args.length) {
                case 4:
                    perHost = Integer.parseInt(args[3]);
                case 3:
                    extractors = Integer.parseInt(args[2]);
                case 2:
                    downloads = Integer.parseInt(args[1]);
                case 1:
                    break;
                default:
                    System.err.println("Wrong number of arguments: expected from 1 to 4, found " + args.length);
                    return;
            }
        } catch (NumberFormatException e) {
            System.err.println("Could not parse integer from your input: " + e.getMessage());
        }

        try (Crawler crawler = new WebCrawler(new CachingDownloader(
                Paths.get("loader")), downloads, extractors, perHost)) {
            crawler.download(args[0], 1);
        } catch (IOException e) {
            System.err.println("CachingDownloader failed to init: " + e.getMessage());
        }
    }

    /**
     * Creates an instance of {@link WebCrawler}.
     *
     * @param downloader    a {@link Downloader} to use for loading pages
     * @param downloaders   maximum amount of downloading threads
     * @param extractors    maximum amount of extracting threads
     * @param perHost       maximum amount of simultaneous requests for one host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloadService = Executors.newFixedThreadPool(downloaders);
        extractService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    /**
     * Downloads the specified page and performs recursion on it.
     * Starts the recursion process and waits until it is ended.
     *
     * @param url   a link for a page to download
     * @param depth recursion level
     * @return  a {@link Result} made of successfully loaded pages and errors which occurred during the work
     */
    @Override
    public Result download(String url, int depth) {
        ModifiableResult result = new ModifiableResult();
        try {
            semaphore.acquire();
            downloadService.submit(() -> load(url, depth, result));
        } catch (InterruptedException ignored) {
        } finally {
            try {
                semaphore.acquire(SIZE);
            } catch (InterruptedException ignored) {
            } finally {
                semaphore.release(SIZE);
            }
        }

        return new Result(result.getDownloaded(), result.getErrors());
    }

    private void extract(Document page, int depth, ModifiableResult result) {
        try {
            if (depth > 1) {
                for (String link : page.extractLinks()) {
                    semaphore.acquire();
                    downloadService.submit(() -> load(link, depth - 1, result));
                }
            }
        } catch (IOException | InterruptedException ignored) {
        } finally {
            semaphore.release();
        }
    }

    private void load(String url, int depth, ModifiableResult result) {
        if (result.getVisited().add(url)) {
            try {
                cached.putIfAbsent(url, new Protector(url));
                Document page = cached.get(url).get();
                semaphore.acquire();
                if (page == null) {
                    result.getVisited().remove(url);
                    downloadService.submit(() -> load(url, depth, result));
                } else {
                    result.getDownloaded().add(url);
                    extractService.submit(() -> extract(page, depth, result));
                }
            } catch (InterruptedException ignored) {
            } catch (IOException e) {
                cached.remove(url);
                result.getErrors().put(url, e);
            }
        }
        semaphore.release();
    }

    /**
     * Closes all helper processes.
     */
    @Override
    public void close() {
        extractService.shutdown();
        downloadService.shutdown();
    }

    private class Protector {
        private String url;
        private Document document = null;

        Protector(String url) {
            this.url = url;
        }

        Document get() throws IOException {
            if (document == null) {
                synchronized (this) {
                    if (document == null) {
                        String host = URLUtils.getHost(url);
                        hosts.putIfAbsent(host, new Semaphore(perHost));
                        try {
                            if (hosts.get(host).tryAcquire(100, TimeUnit.MILLISECONDS)) {
                                try {
                                    document = downloader.download(url);
                                    hosts.get(URLUtils.getHost(url)).release();
                                } catch (IOException e) {
                                    hosts.get(URLUtils.getHost(url)).release();
                                    throw e;
                                }
                            } else {
                                return null;
                            }
                        } catch (InterruptedException e) {
                            return null;
                        }
                    }
                }
            }

            return document;
        }
    }
}
