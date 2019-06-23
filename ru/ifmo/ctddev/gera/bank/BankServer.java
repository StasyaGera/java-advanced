package ru.ifmo.ctddev.gera.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by penguinni on 30.04.17.
 */
public class BankServer {
    Bank bank = new BankImpl(3);

    void register() {
        try {
            UnicastRemoteObject.exportObject(bank);
            Naming.rebind("//localhost/bank", bank);
        } catch (RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
