package xyz.n7mn.dev.survivalplugin;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.survivalplugin.command.MainCommand;
import xyz.n7mn.dev.survivalplugin.command.SigenCommand;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;
import xyz.n7mn.dev.survivalplugin.listener.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SurvivalPlugin extends JavaPlugin {

    private List<UUID> list = new ArrayList<>();
    private List<PlayerLocationData> PlayerList = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        getServer().createWorld(WorldCreator.name("sigen"));
        World sigen = getServer().getWorld("sigen");
        sigen.setTime(getServer().getWorld("world").getTime());

        getLogger().info("sigenワールドの読み込みに成功しました。 seed : " + sigen.getSeed());

        getCommand("sigen").setExecutor(new SigenCommand(PlayerList));
        getCommand("main").setExecutor(new MainCommand(PlayerList));

        getServer().getPluginManager().registerEvents(new EventListener(this, list), this);

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 起動しました。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        for (PlayerLocationData data : PlayerList){
            data.getPlayer().teleport(data.getLocation());
        }

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了しました。");
    }
}
