package ru.potatocoder228.itmo.lab7.exceptions;

/**
 * Исключение бросается, когда у объекта неверный тип
 */

public class WrongTypeException extends WrongFieldException {
    public WrongTypeException(String s) {
        super(s);
        System.out.println(s);
    }

    public WrongTypeException() {
        super();
    }
}
