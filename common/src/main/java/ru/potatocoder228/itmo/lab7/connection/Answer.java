package ru.potatocoder228.itmo.lab7.connection;


public class Answer implements Response {
    private static final long serialVersionUID = 667;
    private String msg;
    private ClientStatus status;

    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public ClientStatus getStatus() {
        return this.status;
    }
}
