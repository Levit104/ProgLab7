package ru.potatocoder228.itmo.lab7.database;

import ru.potatocoder228.itmo.lab7.exceptions.DatabaseException;
import ru.potatocoder228.itmo.lab7.log.Log;
import ru.potatocoder228.itmo.lab7.user.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDatabaseManager {
    private final DatabaseHandler databaseHandler;


    public UserDatabaseManager(DatabaseHandler handler) throws DatabaseException {
        databaseHandler = handler;
        create();
    }

    private void create() throws DatabaseException {
        //language=SQL
        String createTableSQL = "CREATE TABLE IF NOT EXISTS USERS" +
                "(login TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL);";

        databaseHandler.setCommitMode();
        databaseHandler.setSavepoint();
        try (PreparedStatement statement = databaseHandler.getPreparedStatement(createTableSQL)) {
            statement.execute();
            databaseHandler.commit();
        } catch (SQLException e) {
            databaseHandler.rollback();
            throw new DatabaseException("Невозможно создать таблицу в базе данных.");
        } finally {
            databaseHandler.setNormalMode();
        }
    }


    public void add(User user) throws DatabaseException {
        String sql = "INSERT INTO USERS (login, password) VALUES (?, ?)";

        databaseHandler.setCommitMode();
        databaseHandler.setSavepoint();
        try (PreparedStatement preparedStatement = databaseHandler.getPreparedStatement(sql)) {
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.execute();
            databaseHandler.commit();
        } catch (SQLException e) {
            databaseHandler.rollback();
            throw new DatabaseException("Что-то пошло не так при добавлении нового пользователя...");
        } finally {
            databaseHandler.setNormalMode();
        }
    }


    public boolean isValid(User user) {
        try {
            String password = user.getPassword();
            ResultSet rs = databaseHandler.getPreparedStatement("SELECT * FROM USERS WHERE login = '" + user.getLogin() + "'").executeQuery();
            while (rs.next())
                if (password.equals(rs.getString(2)))
                    return true;
            return false;
        } catch (SQLException e) {
            Log.logger.error("Не можем найти пользователя в базе.");
            return false;
        }
    }


    public boolean isPresent(String username) {
        try {
            PreparedStatement statement = databaseHandler.getPreparedStatement("SELECT * FROM USERS WHERE login = '" + username + "'");
            ResultSet rs = statement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Log.logger.error("Не можем выдать пользователя из базы данных.");
            return false;
        }
    }
}
