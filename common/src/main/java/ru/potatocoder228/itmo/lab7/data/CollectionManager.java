package ru.potatocoder228.itmo.lab7.data;

import ru.potatocoder228.itmo.lab7.database.DragonDatabaseManager;
import ru.potatocoder228.itmo.lab7.user.User;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class CollectionManager {
    private final BlockingDeque<Dragon> collection;
    private final BlockingDeque<Integer> idList = new LinkedBlockingDeque<>();
    private User user;
    private DragonDatabaseManager dragonManager;
    private Dragon newDragon;
    private LocalDateTime creatingTime;

    public CollectionManager() {
        collection = new LinkedBlockingDeque<>();
        creatingTime = LocalDateTime.now();
    }

    public void addWithoutIdGeneration(Dragon worker) {
        idList.add(worker.getId());
        collection.add(worker);
    }

    public void setDragon(Dragon dragon) {
        this.newDragon = dragon;
    }

    public Dragon getNewDragon() {
        return this.newDragon;
    }

    public int getSize() {
        return collection.size();
    }

    public LocalDateTime getCreatingTime() {
        return this.creatingTime;
    }

    public BlockingDeque<Dragon> getCollection() {
        return this.collection;
    }

    public void setDragonManager(DragonDatabaseManager dragonManager) {
        this.dragonManager = dragonManager;
    }

    public DragonDatabaseManager getDragonManager() {
        return this.dragonManager;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

}
