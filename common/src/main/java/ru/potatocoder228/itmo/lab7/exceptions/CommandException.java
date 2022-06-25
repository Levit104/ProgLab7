package ru.potatocoder228.itmo.lab7.exceptions;

public class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
        System.out.println(message);
    }
}
