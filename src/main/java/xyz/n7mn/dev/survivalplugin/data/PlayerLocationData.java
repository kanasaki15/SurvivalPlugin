package xyz.n7mn.dev.survivalplugin.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerLocationData{
    private Player player;
    private Location location;

    public PlayerLocationData(Player player, Location location){
        this.player = player;
        this.location = location;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }
}
