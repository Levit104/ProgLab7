package ru.potatocoder228.itmo.lab7.connection;

import ru.potatocoder228.itmo.lab7.data.Dragon;
import ru.potatocoder228.itmo.lab7.user.User;

public class Ask implements Request {
    private static final long serialVersionUID = 666;
    private String msg;
    private Dragon dragon;
    private User user;
    private Status status = Status.RUNNING;

    public Ask() {
        msg = "";
    }


    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    public Dragon getDragon() {
        return this.dragon;
    }

    public void setDragon(Dragon dragon) {
        this.dragon = dragon;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}
