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
import org.bukkit.*;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;
import xyz.n7mn.dev.survivalplugin.event.DiscordonMessageReceivedEvent;
import xyz.n7mn.dev.survivalplugin.function.Lati2Hira;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
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

                    //e.getPlayer().sendMessage(component);
                    //e.getPlayer().sendMessage(component1);
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

        if (e.getInventory().getType() != InventoryType.CHEST){
            return;
        }

        boolean isFound = false;
        boolean isAdd = false;

        LockCommandUser u = null;
        int i = 0;
        for (LockCommandUser user : lockUserList){
            if (e.getPlayer().getUniqueId().equals(user.getUserUUID())){
                isAdd = user.isAdd();
                isFound = true;
                u = user;
                break;
            }
            i++;
        }

        if (u != null){
            lockUserList.remove(i);
        }

        Location location = e.getInventory().getLocation();
        Chest chest = (Chest) location.getBlock().getState();

        if (isFound){
            e.getView().close();
            e.getPlayer().closeInventory();
            e.setCancelled(true);

            UUID chestID;
            if (!chest.hasMetadata("uuid")){
                chestID = UUID.randomUUID();
                chest.setMetadata("uuid", new FixedMetadataValue(plugin, chestID.toString()));
                chest.update(true);
            } else {
                chestID = UUID.fromString((String) chest.getMetadata("uuid").get(0).value());
            }

            // ロック追加 or 解除処理
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, chestID.toString());
                ResultSet set = statement.executeQuery();


                if (isAdd){
                    // 追加
                    UUID addUser = u.getAddUser();
                    UUID userUUID = u.getUserUUID();

                    boolean check = false;
                    boolean isParent = false;
                    boolean isAddCheck = true;
                    while (set.next()){
                        check = true;
                        if (userUUID.toString().equals(set.getString("MinecraftUserID"))){
                            isParent = set.getBoolean("IsParent");
                            break;
                        }

                        if (addUser != null && addUser.toString().equals(set.getString("MinecraftUserID"))){
                            isAddCheck = false;
                        }
                    }

                    set.close();
                    statement.close();

                    // System.out.println("check " + check);
                    // System.out.println("isParent" + isParent);
                    if (check && !isParent && addUser != null){
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "保護を追加した人しか保護追加できません。");
                        con.close();
                        return;
                    }

                    if (check && addUser == null){
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "すでに登録されています。");
                        con.close();
                        return;
                    }

                    if (check && !isAddCheck){
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "すでに登録されています。");
                        con.close();
                        return;
                    }

                    if (!check && addUser != null){
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "保護されていないので追加登録できません。");
                        con.close();
                        return;
                    }

                    new Thread(()->{
                        try {
                            PreparedStatement statement1 = con.prepareStatement("INSERT INTO `LockList`(`UUID`, `BlockID`, `BlockType`, `MinecraftUserID`, `IsParent`, `Active`) VALUES (?,?,?,?,?,?)");
                            statement1.setString(1, UUID.randomUUID().toString());
                            statement1.setString(2, chestID.toString());
                            statement1.setString(3, chest.getType().name());
                            if (addUser == null){
                                statement1.setString(4, userUUID.toString());
                                statement1.setBoolean(5, true);
                            } else {
                                statement1.setString(4, addUser.toString());
                                statement1.setBoolean(5, false);
                            }
                            statement1.setBoolean(6, true);
                            statement1.execute();
                            statement1.close();
                            con.close();
                        } catch (SQLException ex){
                            ex.printStackTrace();
                        }

                    }).start();

                    if (addUser != null){
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "保護チェストに追加登録が完了しました。");
                    } else {
                        e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "チェストを保護しました。");
                    }

                    return;
                }
                // 削除
                UUID delUser = u.getAddUser();
                UUID userUUID = u.getUserUUID();

                boolean check = false;
                boolean isParent = false;
                boolean isDelCheck = true;
                while (set.next()){
                    check = true;
                    if (delUser != null && delUser.toString().equals(set.getString("MinecraftUserID"))){
                        isDelCheck = false;
                    }

                    if (userUUID.toString().equals(set.getString("MinecraftUserID"))){
                        isParent = set.getBoolean("IsParent");
                        break;
                    }

                }

                set.close();
                statement.close();

                if (check && !isParent && delUser != null){
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "保護を追加した人しか保護削除できません。");
                    con.close();
                    return;
                }

                if (check && !isParent){
                    //plugin.getLogger().info("a");
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "すでに解除されています。");
                    con.close();
                    return;
                }

                if (check && !isDelCheck){
                    //plugin.getLogger().info("b");
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "すでに解除されています。");
                    con.close();
                    return;
                }

                if (!check){
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "すでに解除されています。");
                    con.close();
                    return;
                }

                new Thread(()->{
                    try {
                        PreparedStatement statement1 = con.prepareStatement("UPDATE `LockList` SET `Active` = ? WHERE BlockID = ? AND MinecraftUserID = ?");
                        statement1.setBoolean(1, false);
                        statement1.setString(2, chestID.toString());
                        if (delUser == null){
                            statement1.setString(3, userUUID.toString());
                        } else {
                            statement1.setString(3, delUser.toString());
                        }
                        statement1.execute();
                        statement1.close();
                        con.close();
                    } catch (SQLException ex){
                        ex.printStackTrace();
                    }

                }).start();

                if (delUser != null){
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "保護チェストに登録解除が完了しました。");
                } else {
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "チェストを保護解除しました。");
                }
            } catch (SQLException ex){
                ex.printStackTrace();
            }
            return;
        }
        // ロックチェック
        UUID UserUUID = e.getPlayer().getUniqueId();
        UUID chestID;
        if (!chest.hasMetadata("uuid")){
            return;
        } else {
            chestID = UUID.fromString((String) chest.getMetadata("uuid").get(0).value());
        }

        try {

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

            PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
            statement.setString(1, chestID.toString());
            ResultSet set = statement.executeQuery();

            boolean isCheck = false;
            boolean isFoundData = false;
            while (set.next()){
                isFoundData = true;
                if (UserUUID.toString().equals(set.getString("MinecraftUserID"))){
                    isCheck = true;
                    break;
                }
            }

            if (!isCheck && isFoundData){
                e.getView().close();
                e.getPlayer().closeInventory();
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "このチェストは保護されています。");
            }

            set.close();
            statement.close();
            con.close();

        } catch (SQLException ex){
            ex.printStackTrace();
        }
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
        int exp = e.getDroppedExp();
        if (e.getEntity().getLocation().getWorld().getName().equals("world_the_end")){
            e.setKeepInventory(true);
            e.setKeepLevel(true);
            return;
        } else {
            e.setKeepInventory(false);
            e.setKeepLevel(false);
        }

        Player player = e.getEntity();
        PlayerInventory inventory = e.getEntity().getInventory();
        YamlConfiguration config = new YamlConfiguration();
        UUID DeathUUID = UUID.randomUUID();

        config.set("x", player.getLocation().getBlockX());
        config.set("y", player.getLocation().getBlockY());
        config.set("z", player.getLocation().getBlockZ());

        config.set("exp", exp);
        config.set("OldBlockType", player.getLocation().getBlock().getType().name());
        // BlockData
        config.set("OldBlockData", player.getLocation().getBlock().getBlockData());

        for (int i = 0; i < inventory.getSize(); i++){
            ItemStack item = inventory.getItem(i);
            if (item != null){
                config.set("item"+i, item);
                e.getEntity().getInventory().clear(i);
            }
        }


        try {
            File file = new File("./" + plugin.getDataFolder().getPath().replaceAll("\\\\", "/") + "/de/");
            if (!file.exists()){
                file.mkdir();
            }

            config.save(new File("./"+plugin.getDataFolder().getPath().replaceAll("\\\\", "/")+"/de/"+DeathUUID.toString()+".yml"));
            plugin.getLogger().info("[死体生成] "+DeathUUID.toString()+".yml");
        } catch (IOException ex){
            ex.printStackTrace();
        }

        player.getLocation().getWorld().getBlockAt(player.getLocation()).setType(Material.BIRCH_SIGN);

        Block block = player.getLocation().getBlock();
        block.setMetadata("DeathUUID", new FixedMetadataValue(plugin, DeathUUID.toString()));
        Sign sign = (Sign) block.getState();
        sign.line(0, Component.text("[死体]"));
        sign.line(1, Component.text(player.getName()));
        sign.update();

        player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "ワールド名"+player.getLocation().getWorld().getName()+"の" + "X:" + player.getLocation().getBlockX() + " Y:"+ player.getLocation().getBlockY() + " Z:" + player.getLocation().getBlockZ() + "に死体を生成しました。");

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerInteractEvent (PlayerInteractEvent e){

    }
}
