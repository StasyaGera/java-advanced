package ru.ifmo.ctddev.gera.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A udp client which sends packages in multiple threads and waits for the special response.
 */
public class HelloUDPClient implements HelloClient {
    /**
     * Starts the client.
     *
     * @param args  two strings and three integers in the same order as listed below:
     *              <ul>
     *                  <li><tt>host</tt> - a string naming a host to send requests to</li>
     *                  <li><tt>port</tt> - an integer specifying the port to send requests through</li>
     *                  <li><tt>prefix</tt> - a message to be sent</li>
     *                  <li><tt>requests</tt> - an integer specifying the number of requests performed by one thread</li>
     *                  <li><tt>threads</tt> - an integer specifying the number of threads performing requests</li>
     *              </ul>
     * */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Wrong argument number: expected 5, found 0");
            return;
        }
        if (args.length != 5) {
            System.err.println("Wrong argument number: expected 5, found " + args.length);
            return;
        }

        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                    Integer.parseInt(args[4]), Integer.parseInt(args[3]));
        } catch (NumberFormatException e) {
            System.err.println("Could not parse integer from your input: " + e.getMessage());
        }
    }

    /**
     * Starts the client.
     *
     * @param host          a string naming a host to send requests to
     * @param port          an integer specifying the port to send requests through
     * @param prefix        a message to be sent
     * @param requests      an integer specifying the number of requests performed by one thread
     * @param threadsNum    an integer specifying the number of threads performing requests
     * */
    @Override
    public void run(String host, int port, String prefix, int requests, int threadsNum) {
        InetSocketAddress address = new InetSocketAddress(host, port);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadsNum; i++) {
            Thread t = new Thread(new Listener(i, requests, prefix, address));
            threads.add(t);
            t.start();
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        });
    }

    private class Listener implements Runnable {
        private final int threadIndex, requests;
        private final String prefix;
        private final InetSocketAddress address;

        Listener(int threadIndex, int requests, String prefix, InetSocketAddress address) {
            this.threadIndex = threadIndex;
            this.requests = requests;
            this.prefix = prefix;
            this.address = address;
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(100);

                for (int requestIndex = 0; requestIndex < requests; requestIndex++) {
                    String message = String.format("%s%d_%d", prefix, threadIndex, requestIndex);
                    byte[] request = message.getBytes();
                    byte[] buffer = new byte[socket.getReceiveBufferSize()];

                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    socket.send(new DatagramPacket(request, request.length, address));
                    while (!Thread.interrupted()) {
                        try {
                            socket.receive(response);
                            String answer = new String(response.getData(),
                                    response.getOffset(), response.getLength(), Util.CHARSET);
                            if (answer.contains(message)) {
                                System.out.println("Request: " + message);
                                System.out.println("Response: " + answer);
                                break;
                            }
                        } catch (IOException e) {
                            socket.send(new DatagramPacket(request, request.length, address));
                        }
                    }
                }
            } catch (SocketException e) {
                System.err.println("Cannot connect to the specified port: " + e.getMessage());
            } catch (IOException ignored) {
            }
        }
    }
}
