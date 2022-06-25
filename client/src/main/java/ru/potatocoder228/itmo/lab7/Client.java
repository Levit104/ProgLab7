package ru.potatocoder228.itmo.lab7;

import ru.potatocoder228.itmo.lab7.connection.Answer;
import ru.potatocoder228.itmo.lab7.connection.Ask;
import ru.potatocoder228.itmo.lab7.connection.ClientStatus;
import ru.potatocoder228.itmo.lab7.exceptions.ConnectionException;
import ru.potatocoder228.itmo.lab7.exceptions.RecursiveScriptExecuteException;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    private SocketChannel socketChannel;
    private Selector selector;
    private InetAddress host;
    private int port;

    public Client(int port) {
        try {
            this.port = port;
            this.host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }
    }

    public Client() {
    }

    public void run() throws ConnectionException {
        try {
            ClientConsole clientConsole = new ClientConsole(false);
            Ask ask = clientConsole.registration();
            startConnection(host, port);
            sendMessage(ask);
            Answer msg1 = receiveObject();
            System.out.println(msg1.getMessage());
            if (msg1.getStatus().equals(ClientStatus.UNKNOWN)) {
                run();
            } else {
                while (true) {
                    ask = clientConsole.inputCommand();
                    if (ask.getMessage().equals("exit")) {
                        System.out.println("Завершение работы приложения");
                        socketChannel.close();
                        System.exit(0);
                    }
                    startConnection(host, port);
                    sendMessage(ask);
                    msg1 = receiveObject();
                    System.out.println(msg1.getMessage());
                }
            }
        } catch (RecursiveScriptExecuteException e) {
            run();
        } catch (StreamCorruptedException e) {
            System.out.println("Сервер был перезагружен, попытка переподключения...");
            run();
        } catch (IOException e) {
            System.out.println("Ошибка при получении ответа от сервера. Возможно, он временно недоступен");
            run();
        }
    }

    public void start(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            Client client = new Client(port);
            client.run();
        } catch (NumberFormatException e) {
            System.out.println("Порт не был указан. Работа приложения будет завершена");
            Thread.currentThread().interrupt();
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Порт не был введен. Работа приложения будет завершена");
            Thread.currentThread().interrupt();
        } catch (ConnectionException e) {
            System.out.println("Неудачная попытка подключения к серверу");
            System.out.println("Если не хотите переподключиться - введите exit");
            getConsole(args);
        } catch (IllegalArgumentException e) {
            System.out.println("Некорректное значение порта. Работа приложения будет завершена");
            Thread.currentThread().interrupt();
        }
    }

    public void startConnection(InetAddress host, int port) throws ConnectionException {
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            socketChannel.connect(new InetSocketAddress(host, port));
            while (true) {
                selector.select(1000);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        return;
                    }
                }
            }
        } catch (IOException e) {
            throw new ConnectionException("Ошибка при попытке соединения с сервером. Проверьте его и перезапустите приложение");
        }
    }

    public void sendMessage(Ask msg) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(msg);
        ByteBuffer outBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if (key.isValid() && key.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.write(outBuffer);
                    if (outBuffer.remaining() < 1) {
                        return;
                    }
                }
            }
        }
    }

    public Answer receiveObject() throws IOException, ConnectionException {
        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(100000);
                    ByteBuffer outBuffer = ByteBuffer.allocate(100000);
                    try {
                        while (socketChannel.read(byteBuffer) > 0) {
                            byteBuffer.flip();
                            outBuffer.put(byteBuffer);
                            byteBuffer.compact();
                            try {
                                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(outBuffer.array()));
                                return (Answer) objectInputStream.readObject();
                            } catch (StreamCorruptedException e) {
                                if (e.getMessage().contains("invalid stream header")) {
                                    throw new StreamCorruptedException();
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        if (socketChannel.read(byteBuffer) == -1) {
                            throw new IOException();
                        }
                    } catch (IOException e) {
                        System.out.println("Некорректный ответ от сервера. При повторных ошибках стоит перезапустить его");
                        run();
                    }
                }
                iterator.remove();
            }
        }
    }

    public void getConsole(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            if (scanner.nextLine().equals("exit")) {
                Thread.currentThread().interrupt();
            } else {
                start(args);
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Некорректный ввод. Повторите ввод:");
            getConsole(args);
        }
    }
}
