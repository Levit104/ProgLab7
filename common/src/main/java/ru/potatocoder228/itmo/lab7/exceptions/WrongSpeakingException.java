package ru.potatocoder228.itmo.lab7.exceptions;

/**
 * Исключение бросается, когда у объекта неверная способность говорить
 */

public class WrongSpeakingException extends WrongFieldException {
    public WrongSpeakingException(String s) {
        super(s);
        System.out.println(s);
    }

    public WrongSpeakingException() {
        super();
    }
}
