package xyz.n7mn.dev.survivalplugin;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.survivalplugin.command.MainCommand;
import xyz.n7mn.dev.survivalplugin.command.SigenCommand;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;

import java.util.ArrayList;
import java.util.List;

public final class SurvivalPlugin extends JavaPlugin {


    @Override
    public void onEnable() {
        // Plugin startup logic

        List<World> worlds = getServer().getWorlds();

        boolean flag = false;
        for (World world : worlds){
            if (world.getName().equals("sigen")){
                flag = true;
                break;
            }
        }

        if (!flag){
            getServer().createWorld(WorldCreator.name("sigen"));
            getServer().getWorld("sigen").setTime(getServer().getWorld("world").getTime());
        }


        List<PlayerLocationData> list = new ArrayList<>();
        getCommand("sigen").setExecutor(new SigenCommand(list));
        getCommand("main").setExecutor(new MainCommand(list));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
