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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;
import xyz.n7mn.dev.survivalplugin.event.DiscordonMessageReceivedEvent;
import xyz.n7mn.dev.survivalplugin.function.Lati2Hira;

import java.io.File;
import java.io.IOException;
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

            LockCommandUser u = null;
            for (LockCommandUser user : lockUserList){
                if (e.getPlayer().getUniqueId().equals(user.getUserUUID())){
                    isAdd = user.isAdd();
                    isFound = true;
                    u = user;
                    break;
                }
            }

            if (u != null){
                lockUserList.remove(u);
            }

            Location location = e.getInventory().getLocation();
            if (!isFound){

                try {
                    if (!(location.getBlock().getState() instanceof Chest)){
                        return;
                    }
                } catch (Exception ex){
                    return;
                }

                if (!(location.getBlock().getState() instanceof Chest)){
                    return;
                }

                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                    PreparedStatement statement = con.prepareStatement("SELECT * FROM ChestLockList WHERE WorldUUID = ? AND x = ? AND y = ? AND z = ? AND Active = 1");
                    statement.setString(1, location.getWorld().getUID().toString());
                    statement.setInt(2, location.getBlockX());
                    statement.setInt(3, location.getBlockY());
                    statement.setInt(4, location.getBlockZ());
                    ResultSet set = statement.executeQuery();
                    if (set.next()){
                        if (e.getPlayer().getUniqueId().equals(UUID.fromString(set.getString("LockUser")))){
                            set.close();
                            statement.close();
                            con.close();
                            return;
                        }

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            e.getView().close();
                            e.getPlayer().closeInventory();
                            e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"他の人が保護しているチェストです。");
                        });
                        plugin.getLogger().info(e.getPlayer().getName() + "さんが"+set.getString("LockUsername")+"さんの保護されたチェストを開けようとしました。");

                        set.close();
                        statement.close();
                        con.close();
                        return;
                    }
                } catch (SQLException ex){
                    ex.printStackTrace();
                }

                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                e.getView().close();
                e.getPlayer().closeInventory();
            });

            if (isAdd){
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                    con.setAutoCommit(true);

                    PreparedStatement statement = con.prepareStatement("SELECT * FROM ChestLockList WHERE WorldUUID = ? AND x = ? AND y = ? AND z = ? AND Active = 1");
                    statement.setString(1, location.getWorld().getUID().toString());
                    statement.setInt(2, location.getBlockX());
                    statement.setInt(3, location.getBlockY());
                    statement.setInt(4, location.getBlockZ());
                    ResultSet set = statement.executeQuery();

                    if (set.next()){
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"すでに保護されているチェストです。");
                        });

                        set.close();
                        statement.close();
                        con.close();
                        return;
                    }

                    set.close();
                    statement.close();

                    PreparedStatement statement2 = con.prepareStatement("INSERT INTO `ChestLockList`(`UUID`, `LockUser`, `LockUsername`, `WorldUUID`, `x`, `y`, `z`, `Active`) VALUES (?,?,?,?,?,?,?,?)");
                    statement2.setString(1, UUID.randomUUID().toString());
                    statement2.setString(2, e.getPlayer().getUniqueId().toString());
                    statement2.setString(3, e.getPlayer().getName());
                    statement2.setString(4, location.getWorld().getUID().toString());
                    statement2.setInt(5, location.getBlockX());
                    statement2.setInt(6, location.getBlockY());
                    statement2.setInt(7, location.getBlockZ());
                    statement2.setBoolean(8, true);
                    statement2.execute();

                    statement2.close();
                    con.close();

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"チェストを保護しました。");
                    });
                } catch (SQLException ex){
                    ex.printStackTrace();
                }
            } else {
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                    con.setAutoCommit(true);

                    PreparedStatement statement = con.prepareStatement("SELECT * FROM ChestLockList WHERE WorldUUID = ? AND x = ? AND y = ? AND z = ? AND Active = 1");
                    statement.setString(1, location.getWorld().getUID().toString());
                    statement.setInt(2, location.getBlockX());
                    statement.setInt(3, location.getBlockY());
                    statement.setInt(4, location.getBlockZ());
                    ResultSet set = statement.executeQuery();

                    String uuid;
                    if (!set.next()){
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"このチェストは保護されていません。");
                        });

                        set.close();
                        statement.close();
                        con.close();
                        return;
                    } else {
                        uuid = set.getString("UUID");

                        if (!e.getPlayer().getUniqueId().equals(UUID.fromString(uuid)) && !e.getPlayer().isOp()){
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"他の人が保護したチェストは解除できません。");
                            });

                            set.close();
                            statement.close();
                            con.close();
                            return;
                        }

                    }

                    set.close();
                    statement.close();

                    PreparedStatement statement2 = con.prepareStatement("UPDATE `ChestLockList` SET `Active`= ? WHERE UUID = ?");
                    statement2.setBoolean(1, false);
                    statement2.setString(2, uuid);

                    statement2.execute();

                    statement2.close();
                    con.close();

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"チェストを保護解除しました。");
                    });


                } catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void DiscordOnMessageReceivedEvent (DiscordonMessageReceivedEvent e){
        MessageReceivedEvent event = e.getMessageReceivedEvent();

        if (event.isWebhookMessage() || event.getAuthor().isBot()){
            return;
        }

        if (event.getMessage().getTextChannel().getId().equals(plugin.getConfig().getString("NotificationChannel"))){
            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"新しいお知らせがあります。");
                TextComponent component = Component.text("[確認する]");
                component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/noti"));
                player.sendMessage(component);
            }

            return;
        }

        if (event.getMessage().getTextChannel().getId().equals(plugin.getConfig().getString("ChatChannel"))){
            String discordName = event.getAuthor().getName();
            if (event.getMessage().getMember().getNickname() != null){
                discordName = event.getMessage().getMember().getNickname();
            }

            String text = ChatColor.AQUA + "[Discord] "+ discordName + " " + ChatColor.RESET + e.getMessageReceivedEvent().getMessage().getContentDisplay();
            plugin.getLogger().info(text);
            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(text);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerDeathEvent(PlayerDeathEvent e){
        e.setDroppedExp(0);

        Player player = e.getEntity();
        int size = player.getInventory().getSize();
        String fileName = player.getUniqueId().toString() + "_" + player.getLocation().getWorld().getName() + "_" + player.getLocation().getBlockX() + "_" + player.getLocation().getBlockY() + "_" + player.getLocation().getBlockZ() + ".yml";
        // plugin.getLogger().info("生成チェック : " + plugin.getDataFolder().getPath().replaceAll("\\\\","/") + "/d/" + fileName);
        File file1 = new File("./" + plugin.getDataFolder().getPath().replaceAll("\\\\","/") + "/d/");
        if (!file1.exists()){
            file1.mkdir();
        }

        try {
            File file = new File("./" + plugin.getDataFolder().getPath().replaceAll("\\\\","/") + "/d/" + fileName);
            file.createNewFile();

            YamlConfiguration config = new YamlConfiguration();

            if (player.getLocation().getBlock().getType() != Material.AIR){
                config.set("oldBlock", player.getLocation().getBlock());
            }

            player.getLocation().getBlock().setType(Material.BIRCH_SIGN);

            Block block = player.getLocation().getBlock();

            Sign sign = (Sign) block.getState();
            sign.line(0, Component.text("[死体]"));
            sign.line(1, Component.text(player.getName()));
            sign.update();

            for (int i = 0; i < size; i++){
                if (player.getInventory().getItem(i) == null){
                    continue;
                }
                config.set("block"+i, player.getInventory().getItem(i));
                player.getInventory().clear(i);
            }
            config.save(file);


            plugin.getLogger().info("生成チェック : " + plugin.getDataFolder().getPath().replaceAll("\\\\","/") + "/d/" + fileName);
            for (Player onPlayer : Bukkit.getServer().getOnlinePlayers()){
                onPlayer.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + e.deathMessage());
            }

            player.sendMessage(ChatColor.YELLOW+"[ななみ生活鯖] "+ChatColor.RESET+"以下の場所に墓を生成しました！\nX: "+player.getLocation().getBlockX()+" Y:"+player.getLocation().getBlockY()+" Z: "+player.getLocation().getBlockZ());
        } catch (IOException ex){
            ex.printStackTrace();
        }


        player.spigot().respawn();
        e.setCancelled(true);

    }
}
