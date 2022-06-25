package ru.potatocoder228.itmo.lab7.server;

import ru.potatocoder228.itmo.lab7.commands.CommandManager;
import ru.potatocoder228.itmo.lab7.connection.Answer;
import ru.potatocoder228.itmo.lab7.connection.Ask;
import ru.potatocoder228.itmo.lab7.connection.ClientStatus;
import ru.potatocoder228.itmo.lab7.connection.Status;
import ru.potatocoder228.itmo.lab7.data.CollectionManager;
import ru.potatocoder228.itmo.lab7.database.DatabaseHandler;
import ru.potatocoder228.itmo.lab7.database.DragonDatabaseManager;
import ru.potatocoder228.itmo.lab7.database.UserDatabaseManager;
import ru.potatocoder228.itmo.lab7.exceptions.DatabaseException;
import ru.potatocoder228.itmo.lab7.log.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private Queue<Ask> receiverQueue;
    private Queue<Answer> senderQueue;
    private ServerSocket serverSocket;
    private CommandManager commandManager;
    private ExecutorService request;
    private ExecutorService response;
    private DatabaseHandler databaseHandler;
    private UserDatabaseManager userManager;
    private boolean running = true;

    public Server(int port, Properties properties) {
        try {
            serverSocket = new ServerSocket(port);
            databaseHandler = new DatabaseHandler(properties.getProperty("url"), properties.getProperty("user"), properties.getProperty("password"));
            userManager = new UserDatabaseManager(databaseHandler);
            DragonDatabaseManager dragonDatabaseManager = new DragonDatabaseManager(databaseHandler, userManager);
            CollectionManager collectionManager = new CollectionManager();
            collectionManager.setDragonManager(dragonDatabaseManager);
            collectionManager.getDragonManager().deserializeCollection(collectionManager);

            commandManager = new CommandManager(collectionManager);
            request = Executors.newCachedThreadPool();
            response = Executors.newCachedThreadPool();
            receiverQueue = new LinkedList<>();
            senderQueue = new LinkedList<>();

            Log.logger.trace("Начало работы сервера.");
        } catch (IOException e) {
            Log.logger.error("Ошибка подключения. Вероятно, этот порт уже занят.");
            Thread.currentThread().interrupt();
        }
    }

    public void run() throws IOException {
        Runnable console = () -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    System.out.print("\nВведите команду:");
                    String line = scanner.nextLine();
                    if (line.equals("exit")) {
                        running = false;
                        databaseHandler.closeConnection();
                        System.exit(0);
                    } else {
                        Log.logger.trace("Некорректная команда.");
                    }
                } catch (NoSuchElementException e) {
                    Thread.currentThread().interrupt();
                    scanner = new Scanner(System.in);
                } catch (NullPointerException e) {
                    Log.logger.error(e.getMessage());
                }
            }
        };
        new Thread(console).start();
        while (running) {
            Socket socket = serverSocket.accept();
            Callable receiver = () -> {
                Ask ask;
                try {
                    ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                    ask = (Ask) is.readObject();
                    System.out.println("\n");
                    Log.logger.trace("Получен запрос от клиента: " + ask.getMessage());
                } catch (IOException | ClassNotFoundException e) {
                    Log.logger.error("Некорректный запрос от клиента...");
                    e.printStackTrace();
                    ask = new Ask();
                    ask.setStatus(Status.ERROR);
                }
                return ask;
            };
            FutureTask<Ask> task = new FutureTask<>(receiver);
            request.submit(task);
            Runnable handler = () -> {
                try {
                    receiverQueue.add(task.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                Answer answer = new Answer();
                Ask ask = receiverQueue.poll();
                if (ask != null) {
                    if (ask.getStatus().equals(Status.RUNNING)) {
                        String command = ask.getMessage();
                        String[] commandExecuter = command.split("\\s+", 2);
                        commandManager.setUser(ask.getUser());
                        commandManager.setNewDragon(ask.getDragon());
                        if (commandExecuter.length == 2) {
                            String clientMessage = commandManager.commandRun(commandExecuter[0], commandExecuter[1]);
                            Log.logger.trace("Команда: " + command);
                            answer.setMessage(clientMessage);
                            answer.setStatus(ClientStatus.REGISTER);
                            Log.logger.trace("Ответ обработан.");
                        } else if (commandExecuter.length == 1) {
                            String clientMessage = commandManager.commandRun(commandExecuter[0], "");
                            Log.logger.trace("Команда: " + command);
                            answer.setMessage(clientMessage);
                            answer.setStatus(ClientStatus.REGISTER);
                            Log.logger.trace("Ответ обработан.");
                        }
                    } else {
                        if (ask.getStatus().equals(Status.LOGIN)) {
                            try {
                                userManager.add(ask.getUser());
                                answer.setMessage("Регистрация прошла успешно!");
                                answer.setStatus(ClientStatus.REGISTER);
                            } catch (DatabaseException e) {
                                answer.setMessage(e.getMessage());
                                answer.setStatus(ClientStatus.UNKNOWN);
                            }
                            Log.logger.trace("Ответ обработан.");
                        } else if (ask.getStatus().equals(Status.ERROR)) {
                            answer.setMessage("Ошибка при обработке команды сервером. Повторите свой запрос снова...");
                        } else {
                            if (userManager.isValid(ask.getUser())) {
                                answer.setMessage("Авторизация прошла успешно.");
                                answer.setStatus(ClientStatus.REGISTER);
                            } else {
                                answer.setMessage("Неверный логин и пароль. Такого пользователя не существует.");
                                answer.setStatus(ClientStatus.UNKNOWN);
                            }
                            Log.logger.trace("Ответ обработан.");
                        }
                    }
                    senderQueue.add(answer);
                }
            };
            Thread messageHandler = new Thread(handler);
            messageHandler.start();
            Runnable sender = () -> {
                try {
                    messageHandler.join();
                    Answer answer;
                    if (!senderQueue.isEmpty()) {
                        answer = senderQueue.poll();
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(answer);
                        Log.logger.trace("Сообщение успешно отправлено клиенту.");
                        System.out.print("Введите команду:");
                    }
                } catch (IOException | InterruptedException e) {
                    Log.logger.error(e.getMessage());
                }
            };
            response.submit(sender);
        }
        Log.logger.trace("Завершение работы сервера.");
        serverSocket.close();
        databaseHandler.closeConnection();
    }
}