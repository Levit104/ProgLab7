package ru.potatocoder228.itmo.lab7.commands;

import ru.potatocoder228.itmo.lab7.data.CollectionManager;
import ru.potatocoder228.itmo.lab7.data.Dragon;
import ru.potatocoder228.itmo.lab7.user.User;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class CommandManager implements Commandable {
    private final CollectionManager collectionManager;
    private final HashMap<String, Command> map;
    private final HashMap<String, String> help;

    public CommandManager(CollectionManager collection) {
        this.collectionManager = collection;
        map = new HashMap<>();
        help = new HashMap<>();

        addCommand("add", "добавление нового элемента в коллекцию.", (a) -> {
            String status;
            try {
                collectionManager.getDragonManager().add(collectionManager.getNewDragon(), collectionManager);
                status = "Команда выполнена.";
            } catch (NullPointerException e) {
                status = "Не удалось добавить элемент в коллекцию...";
            }
            return status;
        });

        addCommand("add_if_max",
                "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции",
                (a) -> {
                    String status;
                    if (collectionManager.getCollection().isEmpty()) {
                        collectionManager.getDragonManager().add(collectionManager.getNewDragon(), collectionManager);
                        status = "Объект успешно добавлен в коллекцию";
                    } else {
                        int count = (int) collectionManager.getCollection()
                                .stream()
                                .filter(w -> w.getId() == collectionManager.getNewDragon().getId())
                                .count();
                        if (count == 0 && collectionManager.getCollection().getLast().getAge() < collectionManager.getNewDragon().getAge()) {
                            collectionManager.getDragonManager().addIfMax(collectionManager);
                            status = "Команда выполнена.";
                        } else if (count > 0) {
                            status = "Объект с таким id уже есть в коллекции.";
                        } else {
                            status = "Объект меньше максимального.";
                        }
                    }
                    return status;
                });

        addCommand("clear", "очистить коллекцию.", (a) -> {
            collectionManager.getDragonManager().clear(collectionManager);
            return "Ваши элементы коллекции удалены.";
        });

        addCommand("execute_script", "считать и исполнить скрипт из указанного файла.",
                (a) -> "Зарезервированная команда.");

        addCommand("exit", "завершить программу(без сохранения в файл).", (a) -> {
            String status = "Завершение работы сервера...";
            System.out.println(status);
            System.exit(0);
            return status;
        });

        addCommand("filter_greater_than_description", "вывести элементы, значение поля description которых больше заданного.",
                (a) -> {
                    String status;
                    try {
                        status = "Вот все нужные объекты:\n";
                        StringBuilder string = new StringBuilder();
                        for (Dragon dragon : collectionManager.getCollection()) {
                            if (dragon.getDescription().length() > Integer.parseInt(a)) {
                                string.append(dragon);
                            }
                        }
                        status += string.toString();
                    } catch (NumberFormatException e) {
                        status = "Некорректный аргумент команды";
                    }
                    return status;
                }
        );

        addCommand("filter_less_than_cave", "вывести все элементы, значение поля cave которых меньше заданного.",
                (a) -> {
                    String status;
                    try {
                        status = "Вот все нужные объекты:\n";
                        StringBuilder string = new StringBuilder();
                        for (Dragon dragon : collectionManager.getCollection()) {
                            if (dragon.getCave() < Integer.parseInt(a)) {
                                string.append(dragon);
                            }
                        }
                        status += string.toString();
                    } catch (NumberFormatException e) {
                        status = "Некорректный аргумент команды";
                    }
                    return status;
                });

        addCommand("help", "вывод справки по доступным командам.",
                (a) -> {
                    String status;
                    status = this.help.entrySet().toString().replace(",", "\n");
                    return " " + status.replace("=", ":").substring(1, status.length() - 1);
                });

        addCommand("info", "выводит информацию о коллекции.",
                (a) -> {
                    String status = "\nИнформация о коллекции:" + "\n\tТип коллекции: LinkedList" + "\n\tВремя создания коллекции: ";
                    status += collectionManager.getCreatingTime().format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"));
                    status += "\n\tКоличество элементов в коллекции: " + collectionManager.getSize();
                    return status;
                });

        addCommand("print_field_descending_speaking", "вывести значения поля speaking всех элементов в порядке убывания.",
                (a) -> {
                    String status = "";
                    StringBuilder string = new StringBuilder();
                    for (Dragon dragon : collectionManager.getCollection()) {
                        string.append("\n").append(dragon.getSpeaking());
                    }
                    status += string.toString();
                    return status;
                });

        addCommand("remove_by_id", "удалить элемент из коллекции по его id.",
                (a) -> {
                    String status;
                    try {
                        collectionManager.getDragonManager().removeByID(Integer.parseInt(a), collectionManager);
                        boolean exists = collectionManager.getCollection().stream()
                                .anyMatch(w -> w.getId() == Integer.parseInt(a));
                        if (exists) {
                            status = "Объект успешно удалён из коллекции";
                        } else {
                            status = "Объекта с таким id не существует...";
                        }
                    } catch (NumberFormatException e) {
                        status = "Некорректный аргумент команды.";
                    }
                    return status;
                });
        addCommand("remove_first", "удалить первый элемент из коллекции.",
                (a) -> {
                    String status;
                    if (collectionManager.getSize() > 0) {
                        collectionManager.getDragonManager().removeFirst(collectionManager.getCollection().getFirst().getId(), collectionManager);
                        status = "Команда успешно выполнена.";
                    } else {
                        status = "Коллекция пуста.";
                    }
                    return status;
                });
        addCommand("remove_greater", "удалить из коллекции все элементы, превышающие заданный.",
                (a) -> {
                    collectionManager.getCollection()
                            .stream()
                            .filter(w -> w.getAge() > collectionManager.getNewDragon().getAge())
                            .forEach(w -> collectionManager.getDragonManager().removeByID(w.getId(), collectionManager));
                    return "Команда выполнена.";
                });
        addCommand("show", "вывод элементов коллекции в строковом представлении.",
                (a) -> {
                    String status = "Вот все объекты коллекции:\n";
                    StringBuilder string = new StringBuilder();
                    for (Dragon dragon : collectionManager.getCollection()) {
                        string.append(dragon.toString());
                    }
                    status += string.toString();
                    if (collectionManager.getCollection().isEmpty()) {
                        status = "Коллекция пуста.";
                    }
                    return status;
                });
        addCommand("update", "обновить значение элемента коллекции, id которого равен заданному.",
                (a) -> {
                    String status;
                    try {
                        collectionManager.getNewDragon().setId(Integer.parseInt(a));
                        collectionManager.getDragonManager().updateByID(Integer.parseInt(a), collectionManager);
                        collectionManager.getCollection().removeIf(dragon -> dragon.getId() == Integer.parseInt(a));
                        collectionManager.getCollection().add(collectionManager.getNewDragon());
                        status = "Объект успешно обновлён.";
                    } catch (NumberFormatException e) {
                        status = "Некорректный аргумент команды";
                    }
                    return status;
                });
    }


    public synchronized String commandRun(String com, String arg) {
        String clientMessage;
        if (this.map.containsKey(com)) {
            clientMessage = this.map.get(com).run(arg);
        } else {
            clientMessage = "Некорректная команда";
        }
        return clientMessage;
    }

    public synchronized void setNewDragon(Dragon dragon) {
        collectionManager.setDragon(dragon);
    }

    public synchronized void setUser(User user) {
        collectionManager.setUser(user);
    }

    public void addCommand(String com, String description, Command command) {
        addRunning(com, command);
        addHelp(com, description);
    }

    public void addRunning(String com, Command command) {
        map.put(com, command);
    }

    public void addHelp(String com, String description) {
        help.put(com, description);
    }
}
