package ru.potatocoder228.itmo.lab7.main;

import ru.potatocoder228.itmo.lab7.log.Log;
import ru.potatocoder228.itmo.lab7.server.Server;

import java.io.IOException;
import java.util.Properties;


public class Main {
    public static void main(String[] args) {
        System.out.println("Начало работы сервера.");
        int port;
        String password;
        String user;
        String url = "jdbc:postgresql://pg:5432/studs";
        try {
            port = Integer.parseInt(args[0]);
            user = args[1];
            password = args[2];
            Properties settings = new Properties();
            settings.setProperty("url", url);
            settings.setProperty("user", user);
            settings.setProperty("password", password);
            Server server = new Server(port, settings);
            server.run();
        } catch (NumberFormatException e) {
            Log.logger.error("Порт должен быть числом. Сервер завершает свою работу.");
            Thread.currentThread().interrupt();
        } catch (IndexOutOfBoundsException e) {
            Log.logger.error("Вы не ввели порт. Сервер завершает свою работу.");
            Thread.currentThread().interrupt();
        } catch (IllegalArgumentException e) {
            Log.logger.error("Некорректное значение порта.");
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            Log.logger.error("Ошибка при завершении работы:" + e.getMessage());
        }
    }
}
