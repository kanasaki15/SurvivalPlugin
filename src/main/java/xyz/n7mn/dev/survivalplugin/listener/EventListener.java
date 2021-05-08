package xyz.n7mn.dev.survivalplugin.listener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;
import xyz.n7mn.dev.survivalplugin.event.DiscordonMessageReceivedEvent;
import xyz.n7mn.dev.survivalplugin.function.Lati2Hira;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class EventListener implements Listener {

    private final Plugin plugin;
    private final JDA jda;
    private List<LockCommandUser> lockUserList;
    public EventListener(Plugin plugin, JDA jda, List<LockCommandUser> lockUserList){
        this.plugin = plugin;
        this.jda = jda;

        this.lockUserList = lockUserList;
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
                    TextComponent component = Component.text(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.UNDERLINE+list.size()+"件のおしらせ"+ChatColor.RESET+"があります。");

                    TextComponent component1 = Component.text("[確認する]");
                    component1 = component1.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/noti"));

                    e.getPlayer().sendMessage(component);
                    e.getPlayer().sendMessage(component1);
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
    public void AsyncChatEvent (AsyncChatEvent e){
        // plugin.getLogger().info("test");
        Component message = e.message();
        TextComponent m = (TextComponent) message;

        String s = m.content();
        if (s.length() != s.getBytes(StandardCharsets.UTF_8).length){
            return;
        }
        s = Lati2Hira.parse(s);

        StringBuffer sb = new StringBuffer();
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://www.google.com/transliterate?langpair=ja-Hira|ja&text="+ URLEncoder.encode(s, "UTF-8"))
                    .build();

            Response response = client.newCall(request).execute();
            String MojiCode = "UTF-8";
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")){
                MojiCode = "windows-31j";
            }
            String RequestText = new String(response.body().string().getBytes(),MojiCode);

            for ( JsonElement jsonElements : new Gson().fromJson(RequestText, JsonArray.class)){
                sb.append(jsonElements.getAsJsonArray().get(1).getAsJsonArray().get(0).getAsString());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        e.message(Component.text(sb.toString() + " ("+m.content()+")"));

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InventoryOpenEvent(InventoryOpenEvent e){
        new Thread(()->{

            boolean isFound = false;
            boolean isAdd = false;

            for (LockCommandUser user : lockUserList){
                if (e.getPlayer().getUniqueId().equals(user.getUserUUID())){
                    isAdd = user.isAdd();
                    isFound = true;
                    break;
                }
            }

            if (!isFound){
                // TODO 保護されてるかどうかチェックしてされていたらその人かチェックして違ったらエラーメッセージを出す処理
                Location location = e.getInventory().getLocation();
                if (!(location.getBlock().getState() instanceof Chest)){
                    return;
                }

                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                e.getView().close();
                e.getPlayer().closeInventory();
            });

            if (isAdd){
                // TODO 保護追加処理
            } else {
                // TODO 保護削除処理
            }
        }).start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void DiscordonMessageReceivedEvent (DiscordonMessageReceivedEvent e){
        MessageReceivedEvent event = e.getMessageReceivedEvent();

        if (event.isWebhookMessage() || event.getAuthor().isBot()){
            return;
        }

        if (!event.getMessage().getTextChannel().getId().equals(plugin.getConfig().getString("NotificationChannel"))){
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()){
            player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"新しいお知らせがあります。");
            TextComponent component = Component.text("[確認する]");
            component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/noti"));
            player.sendMessage(component);
        }
    }

}
