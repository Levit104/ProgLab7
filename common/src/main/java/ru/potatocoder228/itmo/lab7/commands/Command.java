package ru.potatocoder228.itmo.lab7.commands;

@FunctionalInterface
public interface Command {
    String run(String arg);
}
