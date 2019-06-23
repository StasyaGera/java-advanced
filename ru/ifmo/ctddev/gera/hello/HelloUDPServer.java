package ru.ifmo.ctddev.gera.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple server.
 *
 * Accepts requests and sends them back, adding "Hello, " to the beginning of request text.
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService threads;
    private Thread mainT;

    /**
     * Starts the server.
     *
     * @param args  a string of two integers in the same order as listed below:
     *              <ul>
     *                  <li><tt>port</tt> - a port to get requests through</li>
     *                  <li><tt>threads</tt> - maximum amount of threads responding the incoming requests</li>
     *              </ul>
     * */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Wrong argument number: expected 2, found " + args.length);
            return;
        }

        try {
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Could not parse integer from your input");
        }
    }

    private void respond(DatagramPacket query) {
        byte[] response = ("Hello, " +
                new String(query.getData(), query.getOffset(), query.getLength())).getBytes(Util.CHARSET);
        try {
            socket.send(new DatagramPacket(response, response.length, query.getAddress(), query.getPort()));
        } catch (IOException e) {
            System.err.println("Error sending message \"" +
                    new String(response, Util.CHARSET) + "\": " + e.getMessage());
        }
    }

    /**
     * Starts the server.
     *
     * @param port  a port to get requests from
     * @param n     amount of threads responding the requests
     * */
    @Override
    public void start(int port, int n) {
        try {
            threads = Executors.newFixedThreadPool(n);
            socket = new DatagramSocket(port);
            final int size = socket.getReceiveBufferSize();

            mainT = new Thread(() -> {
                while (!Thread.interrupted()) {
                    try {
                        byte[] buffer = new byte[size];
                        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                        socket.receive(request);
                        this.threads.submit(() -> respond(request));
                    } catch (IOException e) {
                        System.err.println("Error receiving message: " + e.getMessage());
                    }
                }
            });
            mainT.start();
        } catch (SocketException e) {
            System.err.println("Error creating socket: " + e.getMessage());
        }
    }

    /**
     * Stops the server.
     * */
    @Override
    public void close() {
        socket.close();
        mainT.interrupt();
        threads.shutdown();
    }
}
