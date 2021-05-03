package xyz.n7mn.dev.survivalplugin.listener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.event.DiscordonMessageReceivedEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class EventListener implements Listener {

    private final Plugin plugin;
    private List<UUID> PvPOnList;
    private final JDA jda;
    public EventListener(Plugin plugin, List<UUID> pvponlist, JDA jda){
        this.plugin = plugin;
        this.jda = jda;

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

                PreparedStatement statement = con.prepareStatement("SELECT * FROM SurvivalUser WHERE UUID = ?");
                statement.setString(1, e.getPlayer().getUniqueId().toString());

                ResultSet set = statement.executeQuery();
                if (set.next()){
                    for (Player player : plugin.getServer().getOnlinePlayers()){
                        player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+e.getPlayer().getName()+"さんが入室しました！");
                    }
                    long count = set.getLong("Count");
                    set.close();
                    statement.close();
                    count++;

                    PreparedStatement statement1 = con.prepareStatement("UPDATE SurvivalUser SET LastJoinDate = NOW(), Count = ? WHERE UUID = ?");
                    statement1.setLong(1, count);
                    statement1.setString(2, e.getPlayer().getUniqueId().toString());
                    statement1.execute();

                    statement1.close();
                    con.close();
                    return;
                }

                statement.close();
                for (Player player : plugin.getServer().getOnlinePlayers()){
                    player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+e.getPlayer().getName()+"さんが新規に入室しました！ゆっくりしていってね！");
                }

                PreparedStatement statement1 = con.prepareStatement("INSERT INTO `SurvivalUser`(`UUID`, `FirstJoinDate`, `LastJoinDate`, `RoleID`, `Count`) VALUES (?,NOW(),NOW(),'',1)");
                statement1.setString(1, e.getPlayer().getUniqueId().toString());
                statement1.execute();
                statement1.close();

                con.close();
            } catch (Exception ex){
                ex.printStackTrace();
            }

        }).start();

        new Thread(()->{
            if (jda != null && jda.getStatus() == JDA.Status.CONNECTED){
                // plugin.getLogger().info("test");
                TextChannel channel = jda.getTextChannelById(plugin.getConfig().getString("NotificationChannel"));

                channel.getHistoryAfter(1, 100).queue(messageHistory -> {
                    List<Message> list = messageHistory.getRetrievedHistory();
                    net.kyori.adventure.text.TextComponent component = Component.text(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.UNDERLINE+list.size()+"件のおしらせ"+ChatColor.RESET+"があります。");
                    component.clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/noti"));

                    e.getPlayer().sendMessage(component);
                });
            }
        }).start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerQuitEvent(PlayerQuitEvent e){
        e.quitMessage(Component.text(""));

        new Thread(()->{
            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+e.getPlayer().getName()+"さんが退出しました！");
            }
        }).start();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityShootBowEvent(EntityShootBowEvent e){
        if (e.getEntity() instanceof Player){
            System.out.println("！！");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void DiscordonMessageReceivedEvent (DiscordonMessageReceivedEvent e){
        MessageReceivedEvent event = e.getEvent();

        if (event.isWebhookMessage() || event.getAuthor().isBot()){
            return;
        }

        if (!event.getMessage().getTextChannel().getId().equals(plugin.getConfig().getString("NotificationChannel"))){
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()){
            player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"新しいお知らせがあります。");
            TextComponent component = new TextComponent("[確認する]");
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noti"));
            player.sendMessage(component);
        }
    }
}
