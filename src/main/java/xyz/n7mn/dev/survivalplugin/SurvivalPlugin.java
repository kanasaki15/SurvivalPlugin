package xyz.n7mn.dev.survivalplugin;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.n7mn.dev.survivalplugin.command.MainCommand;
import xyz.n7mn.dev.survivalplugin.command.SetHomeCommand;
import xyz.n7mn.dev.survivalplugin.command.SigenCommand;
import xyz.n7mn.dev.survivalplugin.command.UserCommand;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;
import xyz.n7mn.dev.survivalplugin.listener.EventListener;
import xyz.n7mn.dev.survivalplugin.timer.WorldReCreateTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SurvivalPlugin extends JavaPlugin {

    private Boolean isMoveWorld = true;
    private List<UUID> list = new ArrayList<>();
    private List<PlayerLocationData> PlayerList = new ArrayList<>();
    private BukkitTask task = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        getServer().createWorld(WorldCreator.name("sigen"));
        World sigen = getServer().getWorld("sigen");
        sigen.setTime(getServer().getWorld("world").getTime());

        getLogger().info("sigenワールドの読み込みに成功しました。 seed : " + sigen.getSeed());

        getCommand("sigen").setExecutor(new SigenCommand(PlayerList, isMoveWorld));
        getCommand("main").setExecutor(new MainCommand(PlayerList));
        getCommand("user").setExecutor(new UserCommand());
        getCommand("sethome").setExecutor(new SetHomeCommand());

        getServer().getPluginManager().registerEvents(new EventListener(this, list), this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, new WorldReCreateTimer(this), 0L, 20L);

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 起動しました。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        for (PlayerLocationData data : PlayerList){
            data.getPlayer().teleport(data.getLocation());
        }

        PlayerList = new ArrayList<>();

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了しました。");
    }

    public List<PlayerLocationData> getPlayerList() {
        return PlayerList;
    }

    public boolean isMoveWorld() {
        return isMoveWorld;
    }

    public void setMoveWorld(boolean moveWorld) {
        isMoveWorld = moveWorld;
    }
}
