package ru.ifmo.ctddev.gera.bank;

import java.rmi.*;

public interface Bank extends Remote {
    Account createAccount(String id) throws RemoteException;
    Account getAccount(String id) throws RemoteException;
}
