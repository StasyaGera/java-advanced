package ru.ifmo.ctddev.gera.bank;

import java.rmi.RemoteException;

/**
 * Created by penguinni on 30.04.17.
 */
public class Person {
    protected String name, surname;
    protected int passportNo;

    public Person(String name, String surname, int passportNo) {
        this.name = name;
        this.surname = surname;
        this.passportNo = passportNo;
    }

    String getName() throws RemoteException {
        return name;
    }

    String getSurname() throws RemoteException {
        return surname;
    }

    int getPassportNo() throws RemoteException {
        return passportNo;
    }
}
