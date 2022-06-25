package ru.potatocoder228.itmo.lab7.commands;

public interface Commandable {
    String commandRun(String com, String arg);

    void addCommand(String com, String description, Command command);

    void addHelp(String com, String description);

    void addRunning(String com, Command command);
}
