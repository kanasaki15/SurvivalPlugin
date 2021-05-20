package xyz.n7mn.dev.survivalplugin.data;

import java.util.UUID;

public class LockCommandUser {

    private UUID UserUUID;
    private boolean isAdd;
    private UUID AddUser;

    public LockCommandUser(UUID userUUID, boolean isAdd){
        this.UserUUID = userUUID;
        this.isAdd = isAdd;
    }

    public LockCommandUser(UUID userUUID, boolean isAdd, UUID addUser){
        this.UserUUID = userUUID;
        this.isAdd = isAdd;
        this.AddUser = addUser;
    }

    public UUID getUserUUID() {
        return UserUUID;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public UUID getAddUser(){
        return AddUser;
    }
}
