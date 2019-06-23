package ru.ifmo.ctddev.gera.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by penguinni on 30.04.17.
 */
public class RemotePerson extends Person implements Remote {
    public RemotePerson(String name, String surname, int passportNo) throws RemoteException {
        super(name, surname, passportNo);
    }
}
