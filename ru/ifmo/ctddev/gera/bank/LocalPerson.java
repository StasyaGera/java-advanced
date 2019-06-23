package ru.ifmo.ctddev.gera.bank;

import java.io.Serializable;

/**
 * Created by penguinni on 30.04.17.
 */
public class LocalPerson extends Person implements Serializable {
    public LocalPerson(String name, String surname, int passportNo) {
        super(name, surname, passportNo);
    }
}
