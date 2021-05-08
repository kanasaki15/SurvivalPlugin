package xyz.n7mn.dev.survivalplugin.data;

import java.util.UUID;

public class LockCommandUser {

    private UUID UserUUID;
    private boolean isAdd;

    public LockCommandUser(UUID userUUID, boolean isAdd){
        this.UserUUID = userUUID;
        this.isAdd = isAdd;
    }

    public UUID getUserUUID() {
        return UserUUID;
    }

    public boolean isAdd() {
        return isAdd;
    }
}
