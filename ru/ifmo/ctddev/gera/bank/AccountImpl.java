package ru.ifmo.ctddev.gera.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AccountImpl extends UnicastRemoteObject implements Account {
    private final String id;
    private int amount;

    public AccountImpl(int port, String id) throws RemoteException {
        super(port);
        this.id = id;
        amount = 0;
    }

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
