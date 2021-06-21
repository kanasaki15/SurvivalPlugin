package xyz.n7mn.dev.survivalplugin.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class ItemframeLockListener implements Listener {

    private List<LockCommandUser> lockUserList;
    private final Plugin plugin;

    public ItemframeLockListener(Plugin plugin, List<LockCommandUser> lockUserList){
        this.plugin = plugin;
        this.lockUserList = lockUserList;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerInteractEntityEvent (PlayerInteractEntityEvent e){
        Entity entity = e.getRightClicked();

        if (!(entity instanceof ItemFrame)){
            return;
        }

        boolean isLock = false;
        boolean isAdd = false;
        LockCommandUser u = null;
        for (LockCommandUser user : lockUserList){
            if (user.getUserUUID().equals(e.getPlayer().getUniqueId())){
                isLock = true;
                isAdd = user.isAdd();
                u = user;
                break;
            }
        }

        if (u != null){
            lockUserList.remove(u);
        }

        ItemFrame frame = (ItemFrame) entity;
        UUID uuid = frame.getUniqueId();
        Player player = e.getPlayer();
        if (isLock){
            // ロック関係
            e.setCancelled(true);
            if (isAdd){
                // ロック追加
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                    PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                    statement.setString(1, uuid.toString());
                    ResultSet set = statement.executeQuery();
                    boolean result = true;
                    if (set.next()){
                        result = false;
                    }
                    set.close();
                    statement.close();

                    if (!result){
                        con.close();
                        player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"この額縁はすでに保護されています。");
                        return;
                    }

                    player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"額縁を保護しました。");
                    PreparedStatement statement1 = con.prepareStatement("INSERT INTO `LockList`(`UUID`, `BlockID`, `BlockType`, `MinecraftUserID`, `IsParent`, `Active`) VALUES (?,?,?,?,?,?)");
                    statement1.setString(1, UUID.randomUUID().toString());
                    statement1.setString(2, uuid.toString());
                    statement1.setString(3, entity.getType().name());
                    statement1.setString(4, player.getUniqueId().toString());
                    statement1.setBoolean(5, true);
                    statement1.setBoolean(6, true);

                    statement1.execute();
                    statement1.close();
                    con.close();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                return;
            }
            // ロック解除
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();
                boolean result = false;
                boolean isFound = false;
                while (set.next()){
                    isFound = true;
                    if (player.getUniqueId().toString().equals(set.getString("MinecraftUserID")) && set.getBoolean("IsParent")){
                        result = true;
                        break;
                    }
                }
                set.close();
                statement.close();

                if (!isFound){
                    con.close();
                    player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"この額縁は保護されていません。");
                    return;
                }

                if (!result){
                    con.close();
                    player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"この額縁は他の人が保護しています。");
                    return;
                }

                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"額縁の保護解除をしました。");

                PreparedStatement statement1 = con.prepareStatement("UPDATE `LockList` SET `Active` = 0 WHERE BlockID = ?");
                statement1.setString(1, uuid.toString());
                statement1.execute();
                statement1.close();
                con.close();

            } catch (SQLException ex){
                ex.printStackTrace();
            }

        }

        // 保護チェック
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
            PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
            statement.setString(1, uuid.toString());
            ResultSet set = statement.executeQuery();

            boolean result = false;
            boolean lock = false;
            while (set.next()){
                lock = true;
                if (player.getUniqueId().toString().equals(set.getString("MinecraftUserID"))){
                    result = true;
                    break;
                }
            }
            set.close();
            statement.close();
            con.close();

            if (!lock){
                return;
            }

            if (!result){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"この額縁は他の人が保護している額縁です。");
                e.setCancelled(true);
            }

        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void HangingBreakEvent (HangingBreakEvent e){
        if (e.getEntity() instanceof ItemFrame){
            ItemFrame frame = (ItemFrame) e.getEntity();
            UUID uuid = frame.getUniqueId();

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();

                if (set.next()){
                    e.setCancelled(true);
                }

                set.close();
                statement.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityDamageEvent(EntityDamageEvent e){
        if (e.getEntity() instanceof ItemFrame){
            ItemFrame frame = (ItemFrame) e.getEntity();
            UUID uuid = frame.getUniqueId();

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();

                if (set.next()){
                    e.setCancelled(true);
                }

                set.close();
                statement.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityDamageByEntityEvent (EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof ItemFrame){
            ItemFrame frame = (ItemFrame) e.getEntity();
            UUID uuid = frame.getUniqueId();

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();

                if (set.next()){
                    e.setCancelled(true);
                }

                set.close();
                statement.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityTeleportEvent (EntityTeleportEvent e){
        if (e.getEntity() instanceof ItemFrame){
            ItemFrame frame = (ItemFrame) e.getEntity();
            UUID uuid = frame.getUniqueId();

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();

                if (set.next()){
                    e.setCancelled(true);
                }

                set.close();
                statement.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }
        }
    }
}
