package ru.potatocoder228.itmo.lab7.exceptions;

public class RecursiveScriptExecuteException extends CommandException {
    public RecursiveScriptExecuteException() {
        super("Попытка рекурсивного выполнения скрипта.");
    }
}