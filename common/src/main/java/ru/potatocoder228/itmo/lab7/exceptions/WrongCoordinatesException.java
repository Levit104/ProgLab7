package ru.potatocoder228.itmo.lab7.exceptions;

/**
 * Исключение бросается, когда у объекта неверные координаты
 */

public class WrongCoordinatesException extends WrongFieldException {
    public WrongCoordinatesException(String s) {
        super(s);
        System.out.println(s);
    }

    public WrongCoordinatesException() {
        super();
    }
}
