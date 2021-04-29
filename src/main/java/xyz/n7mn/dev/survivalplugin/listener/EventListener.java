package xyz.n7mn.dev.survivalplugin.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class EventListener implements Listener {

    private final Plugin plugin;
    private List<UUID> PvPOnList;
    public EventListener(Plugin plugin, List<UUID> pvponlist){
        this.plugin = plugin;

        if (pvponlist == null){
            this.PvPOnList = new ArrayList<>();
        } else {
            this.PvPOnList = pvponlist;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerJoinEvent (PlayerJoinEvent e){
        e.joinMessage(Component.text(""));

        new Thread(()->{
            try {
                boolean found = false;
                Enumeration<Driver> drivers = DriverManager.getDrivers();

                while (drivers.hasMoreElements()){
                    Driver driver = drivers.nextElement();
                    if (driver.equals(new com.mysql.cj.jdbc.Driver())){
                        found = true;
                        break;
                    }
                }

                if (!found){
                    DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
                }

                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                // TODO: 新規チェックからのメッセージ挿入(TableName : SurvivalUser)

                con.close();

            } catch (Exception ex){
                ex.printStackTrace();
            }


        }).start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerQuitEvent(PlayerQuitEvent e){
        e.quitMessage(Component.text(""));

        new Thread(()->{
            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+e.getPlayer().getName()+"さんが退出しました！");
            }
        }).start();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityShootBowEvent(EntityShootBowEvent e){
        if (e.getEntity() instanceof Player){
            System.out.println("！！");
        }
    }
}
