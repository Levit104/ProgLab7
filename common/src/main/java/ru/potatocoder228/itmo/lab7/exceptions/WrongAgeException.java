package ru.potatocoder228.itmo.lab7.exceptions;

/**
 * Исключение бросается, когда у объекта неверный возраст
 */

public class WrongAgeException extends WrongFieldException {
    public WrongAgeException(String s) {
        super(s);
        System.out.println(s);
    }

    public WrongAgeException() {
        super();
    }
}