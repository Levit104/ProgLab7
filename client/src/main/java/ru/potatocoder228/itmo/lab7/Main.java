package ru.potatocoder228.itmo.lab7;

public class Main {
    public static void main(String[] args) {
        System.out.println("Добро пожаловать в клиентскую версию консольного приложения.");
        System.out.println("help - список всех доступных команд.");
        System.out.println("\nДля того, чтобы пользоваться приложением, вам необходимо зарегистрироваться.");
        Client client = new Client();
        client.start(args);
    }
}
