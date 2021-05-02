package xyz.n7mn.dev.survivalplugin.data;

import java.util.Date;
import java.util.UUID;

public class SurvivalUser {

    private UUID UUID;
    private Date FirstJoinDate;
    private Date LastJoinDate;
    private UUID RoleID;
    private long Count;

    public SurvivalUser(){
        this.UUID = null;
        this.FirstJoinDate = new Date();
        this.LastJoinDate = this.FirstJoinDate;
        this.RoleID = this.UUID;
        this.Count = 0L;
    }

    public SurvivalUser(UUID uuid, Date firstJoinDate, Date lastJoinDate, UUID roleID, long count){
        this.UUID = uuid;
        this.FirstJoinDate = firstJoinDate;
        this.LastJoinDate = lastJoinDate;
        this.RoleID = roleID;
        this.Count = count;
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public Date getFirstJoinDate() {
        return FirstJoinDate;
    }

    public Date getLastJoinDate() {
        return LastJoinDate;
    }

    public java.util.UUID getRoleID() {
        return RoleID;
    }

    public long getCount() {
        return Count;
    }
}
