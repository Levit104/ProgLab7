package ru.potatocoder228.itmo.lab7.database;

import ru.potatocoder228.itmo.lab7.data.*;
import ru.potatocoder228.itmo.lab7.exceptions.DatabaseException;
import ru.potatocoder228.itmo.lab7.exceptions.InvalidDataException;
import ru.potatocoder228.itmo.lab7.log.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class DragonDatabaseManager extends CollectionManager {
    private final static String INSERT_WORKER_QUERY = "INSERT INTO DRAGONS (name, coordinates_x, coordinates_y, creation_date, age, description, speaking, type, cave, user_login, id)" +
            "VALUES (?,?,?,?,?,?,?,?,?,?,DEFAULT) RETURNING id;";

    private final DatabaseHandler databaseHandler;
    private final UserDatabaseManager userManager;

    public DragonDatabaseManager(DatabaseHandler c, UserDatabaseManager userDatabaseManager) throws DatabaseException {
        super();
        this.userManager = userDatabaseManager;
        databaseHandler = c;
        create();
    }

    private void create() throws DatabaseException {
        //language=SQL
        String create =
                "CREATE TABLE IF NOT EXISTS DRAGONS(" +
                        "id SERIAL PRIMARY KEY CHECK ( id > 0 )," +
                        "name TEXT NOT NULL CHECK (name <> '')," +
                        "coordinates_x BIGINT NOT NULL ," +
                        "coordinates_y DOUBLE PRECISION NOT NULL CHECK (coordinates_y > -123 )," +
                        "creation_date TEXT NOT NULL," +
                        "age INT NOT NULL CHECK(age > 0)," +
                        "description TEXT," +
                        "speaking BOOLEAN," +
                        "type TEXT," +
                        "cave FLOAT NOT NULL," +
                        "user_login TEXT NOT NULL REFERENCES USERS(login));";

        try (PreparedStatement createStatement = databaseHandler.getPreparedStatement(create)) {
            createStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException("Не можем создать базу данных с драконами...");
        }
    }

    private Dragon getDragon(ResultSet resultSet) throws SQLException {
        Coordinates coordinates = new Coordinates(resultSet.getLong("coordinates_x"), resultSet.getDouble("coordinates_y"));
        Integer id = resultSet.getInt("id");
        String name = resultSet.getString("name");

        LocalDateTime creationDate = LocalDateTime.parse(resultSet.getString("creation_date"), DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy"));
        int age = resultSet.getInt("age");

        String description = resultSet.getString("description");

        Boolean speaking = resultSet.getBoolean("speaking");
        DragonCave cave = new DragonCave(resultSet.getFloat("cave"));
        DragonType type = null;
        try {
            type = DragonType.valueOf(resultSet.getString("type"));
        } catch (IllegalArgumentException e) {
            Log.logger.trace("У объекта с id " + id + " отсутствует тип.");
        }
        Dragon dragon = new Dragon(name, coordinates, age, description, speaking, type, cave);
        dragon.setCreationDate(creationDate);
        dragon.setId(id);
        dragon.setUserLogin(resultSet.getString("user_login"));
        if (!userManager.isPresent(dragon.getUserLogin())) throw new DatabaseException("Пользователь не найден.");
        return dragon;
    }

    private void setDragon(PreparedStatement statement, Dragon dragon) throws SQLException {
        statement.setString(1, dragon.getName());
        statement.setFloat(2, dragon.getCoordinates().getX());
        statement.setDouble(3, dragon.getCoordinates().getY());
        statement.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")));
        statement.setLong(5, dragon.getAge());
        statement.setString(6, dragon.getDescription());
        statement.setBoolean(7, dragon.getSpeaking());
        statement.setString(8, dragon.getType().toString());
        statement.setFloat(9, dragon.getCave());
        statement.setString(10, dragon.getUserLogin());
    }

    public void add(Dragon dragon, CollectionManager collectionManager) {
        databaseHandler.setCommitMode();
        databaseHandler.setSavepoint();
        try (PreparedStatement statement = databaseHandler.getPreparedStatement(INSERT_WORKER_QUERY, true)) {
            setDragon(statement, dragon);
            if (statement.executeUpdate() == 0) throw new DatabaseException();
            ResultSet resultSet = statement.getGeneratedKeys();

            if (!resultSet.next()) throw new DatabaseException();
            dragon.setId(resultSet.getInt(resultSet.findColumn("id")));
            databaseHandler.commit();
        } catch (SQLException | DatabaseException e) {
            databaseHandler.rollback();
            e.printStackTrace();
            throw new DatabaseException("Невозможно добавить элемент в базу данных.");
        } finally {
            databaseHandler.setNormalMode();
            updateCollection(collectionManager);
        }
    }

    public void removeByID(Integer id, CollectionManager collectionManager) {
        //language=SQL
        String query = "DELETE FROM DRAGONS WHERE id = ? AND user_login = ?;";
        try (PreparedStatement statement = databaseHandler.getPreparedStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, collectionManager.getUser().getLogin());
            statement.execute();
            updateCollection(collectionManager);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException("Невозможно удалить из базы данных.");
        }
    }

    public void removeFirst(Integer id, CollectionManager collectionManager) {
        removeByID(id, collectionManager);
        updateCollection(collectionManager);
    }

    public void updateByID(Integer id, CollectionManager collectionManager) {
        databaseHandler.setCommitMode();
        databaseHandler.setSavepoint();
        //language=SQL
        String sql = "UPDATE DRAGONS SET " +
                "name=?," +
                "coordinates_x=?," +
                "coordinates_y=?," +
                "creation_date=?," +
                "age=?," +
                "description=?," +
                "speaking=?," +
                "type=?," +
                "cave=?," +
                "user_login=?" +
                "WHERE id=?;";
        try (PreparedStatement statement = databaseHandler.getPreparedStatement(sql)) {
            setDragon(statement, collectionManager.getNewDragon());
            statement.setInt(11, id);
            statement.execute();
            databaseHandler.commit();
            updateCollection(collectionManager);
        } catch (SQLException e) {
            databaseHandler.rollback();
            throw new DatabaseException("Невозможно проапдейтить дракона #" + collectionManager.getNewDragon().getId() + " в базе данных.");
        } finally {
            databaseHandler.setNormalMode();
        }
    }

    public void addIfMax(CollectionManager collectionManager) {
        //language=SQL
        String getMaxQuery = "SELECT MAX(age) FROM DRAGONS;";
        databaseHandler.setCommitMode();
        databaseHandler.setSavepoint();
        try (Statement getStatement = databaseHandler.getStatement();
             PreparedStatement insertStatement = databaseHandler.getPreparedStatement(INSERT_WORKER_QUERY)) {

            ResultSet resultSet = getStatement.executeQuery(getMaxQuery);
            if (!resultSet.next()) throw new DatabaseException("Добавление невозможно.");

            setDragon(insertStatement, collectionManager.getNewDragon());
            collectionManager.getNewDragon().setId(resultSet.getInt("id"));
            databaseHandler.commit();
        } catch (SQLException e) {
            databaseHandler.rollback();
            throw new DatabaseException("Не могу добавить элемент в базу данных...");
        } finally {
            databaseHandler.setNormalMode();
        }
    }

    public void clear(CollectionManager collectionManager) {
        databaseHandler.setCommitMode();
        databaseHandler.setSavepoint();
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement statement = databaseHandler.getPreparedStatement("DELETE FROM DRAGONS WHERE user_login= ? RETURNING id;")) {
            statement.setString(1, collectionManager.getUser().getLogin());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt(1);
                ids.add(id);
            }
            updateCollection(collectionManager);
        } catch (SQLException e) {
            databaseHandler.rollback();
            throw new DatabaseException("Не могу очистить базу данных.");
        } finally {
            databaseHandler.setNormalMode();
        }
    }

    public void deserializeCollection(CollectionManager collectionManager) {
        //language=SQL
        String query = "SELECT * FROM DRAGONS;";
        try (PreparedStatement selectAllStatement = databaseHandler.getPreparedStatement(query)) {
            ResultSet resultSet = selectAllStatement.executeQuery();
            int damagedElements = 0;
            while (resultSet.next()) {
                try {
                    Dragon dragon = getDragon(resultSet);
                    if (!dragon.validate()) throw new InvalidDataException("Элемент поврежден");
                    collectionManager.addWithoutIdGeneration(dragon);
                } catch (InvalidDataException | SQLException e) {
                    damagedElements += 1;
                }
            }
            if (damagedElements == 0) Log.logger.info("Загрузка коллекции прошла успешно");
            else Log.logger.warn(damagedElements + " элементов было повреждено...");
        } catch (SQLException e) {
            throw new DatabaseException("Загрузка коллекции невозможна...");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void updateCollection(CollectionManager collectionManager) {
        //language=SQL
        String query = "SELECT * FROM DRAGONS;";
        try (PreparedStatement selectAllStatement = databaseHandler.getPreparedStatement(query)) {
            ResultSet resultSet = selectAllStatement.executeQuery();
            collectionManager.getCollection().clear();
            while (resultSet.next()) {
                Dragon dragon = getDragon(resultSet);
                collectionManager.addWithoutIdGeneration(dragon);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Обновление коллекции невозможно...");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
