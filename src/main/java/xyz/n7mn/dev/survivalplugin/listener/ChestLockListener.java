package xyz.n7mn.dev.survivalplugin.listener;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChestLockListener implements Listener {

    private List<LockCommandUser> lockUserList;
    private final Plugin plugin;
    public ChestLockListener(Plugin plugin, List<LockCommandUser> lockUserList){
        this.plugin = plugin;
        this.lockUserList = lockUserList;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InventoryOpenEvent(InventoryOpenEvent e){

        if (e.getInventory().getLocation() == null){
            return;
        }

        if (e.getInventory().getType() == InventoryType.CHEST){
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
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                    PreparedStatement statement = con.prepareStatement("SELECT * FROM LockUUIDList WHERE Active = 1 AND world = ? AND x = ? AND y = ? AND z = ?");
                    statement.setString(1, chest.getLocation().getWorld().getUID().toString());
                    statement.setInt(2, chest.getLocation().getBlockX());
                    statement.setInt(3, chest.getLocation().getBlockY());
                    statement.setInt(4, chest.getLocation().getBlockZ());
                    ResultSet set = statement.executeQuery();
                    if (set.next()){
                        chestID = UUID.fromString(set.getString("UUID"));
                    } else {
                        chestID = UUID.randomUUID();

                        PreparedStatement statement1 = con.prepareStatement("INSERT INTO `LockUUIDList`(`UUID`, `world`, `x`, `y`, `z`, `Active`) VALUES (?,?,?,?,?,?)");
                        statement1.setString(1, chestID.toString());
                        statement1.setString(2, chest.getLocation().getWorld().getUID().toString());
                        statement1.setInt(3, chest.getLocation().getBlockX());
                        statement1.setInt(4, chest.getLocation().getBlockY());
                        statement1.setInt(5, chest.getLocation().getBlockZ());
                        statement1.setBoolean(6, true);
                        statement1.execute();
                        statement1.close();
                    }
                    set.close();
                    statement.close();

                    con.close();
                } catch (SQLException ex){
                    ex.printStackTrace();
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "エラーが発生しました。もう一度実行してもエラーになる場合は運営に教えてください。");
                    return;
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
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockUUIDList WHERE Active = 1 AND world = ? AND x = ? AND y = ? AND z = ?");
                statement.setString(1, chest.getLocation().getWorld().getUID().toString());
                statement.setInt(2, chest.getLocation().getBlockX());
                statement.setInt(3, chest.getLocation().getBlockY());
                statement.setInt(4, chest.getLocation().getBlockZ());
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    chestID = UUID.fromString(set.getString("UUID"));
                } else {
                    chestID = UUID.randomUUID();

                    PreparedStatement statement1 = con.prepareStatement("INSERT INTO `DeathList`(`UUID`, `MinecraftUUID`, `world`, `x`, `y`, `z`, `Active`) VALUES (?,?,?,?,?,?,?)");
                    statement1.setString(1, chestID.toString());
                    statement1.setString(2, e.getPlayer().getUniqueId().toString());
                    statement1.setString(3, chest.getLocation().getWorld().getUID().toString());
                    statement1.setInt(4, chest.getLocation().getBlockX());
                    statement1.setInt(5, chest.getLocation().getBlockY());
                    statement1.setInt(6, chest.getLocation().getBlockZ());
                    statement1.setBoolean(7, true);
                    statement1.execute();
                    statement1.close();
                }
                set.close();
                statement.close();

                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
                return;
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
            return;
        }

        if (e.getInventory().getType() == InventoryType.SHULKER_BOX){
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
            ShulkerBox box = (ShulkerBox) location.getBlock().getState();

            UUID uuid;
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockUUIDList WHERE Active = 1 AND world = ? AND x = ? AND y = ? AND z = ?");
                statement.setString(1, box.getLocation().getWorld().getUID().toString());
                statement.setInt(2, box.getLocation().getBlockX());
                statement.setInt(3, box.getLocation().getBlockY());
                statement.setInt(4, box.getLocation().getBlockZ());
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    uuid = UUID.fromString(set.getString("UUID"));
                } else {
                    uuid = UUID.randomUUID();

                    PreparedStatement statement1 = con.prepareStatement("INSERT INTO `LockUUIDList`(`UUID`, `world`, `x`, `y`, `z`, `Active`) VALUES (?,?,?,?,?,?)");
                    statement1.setString(1, uuid.toString());
                    statement1.setString(2, box.getLocation().getWorld().getUID().toString());
                    statement1.setInt(3, box.getLocation().getBlockX());
                    statement1.setInt(4, box.getLocation().getBlockY());
                    statement1.setInt(5, box.getLocation().getBlockZ());
                    statement1.setBoolean(6, true);
                    statement1.execute();
                    statement1.close();
                    plugin.getLogger().info("test");
                }
                set.close();
                statement.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
                return;
            }

            plugin.getLogger().info("UUID : " + uuid.toString());

            if (isFound) {
                e.getView().close();
                e.getPlayer().closeInventory();
                e.setCancelled(true);
                HumanEntity player = e.getPlayer();

                if (isAdd){
                    // 追加
                    try {
                        Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                        PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                        statement.setString(1, uuid.toString());
                        ResultSet set = statement.executeQuery();
                        boolean result = false;
                        boolean resultFound = false;
                        while (set.next()){
                            resultFound = true;
                            if (player.getUniqueId().toString().equals(set.getString("MinecraftUserID")) && set.getBoolean("IsParent")){
                                result = true;
                            }
                        }
                        set.close();
                        statement.close();

                        if (!result && resultFound){
                            player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"他の人が保護しているシュルカーボックスです。");
                            con.close();
                            return;
                        }

                        if (resultFound){
                            player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"すでに保護されているシュルカーボックスです");
                            con.close();
                            return;
                        }

                        player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"シュルカーボックスを保護しました。");
                        PreparedStatement statement1 = con.prepareStatement("INSERT INTO `LockList`(`UUID`, `BlockID`, `BlockType`, `MinecraftUserID`, `IsParent`, `Active`) VALUES (?,?,?,?,?,?)");
                        statement1.setString(1, UUID.randomUUID().toString());
                        statement1.setString(2, uuid.toString());
                        statement1.setString(3, box.getBlock().getType().name());
                        statement1.setString(4, player.getUniqueId().toString());
                        statement1.setBoolean(5, true);
                        statement1.setBoolean(6, true);
                        statement1.execute();
                        statement1.close();
                        con.close();

                    } catch (SQLException ex){
                        ex.printStackTrace();
                    }
                    return;
                }

                // 削除
                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                    PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                    statement.setString(1, uuid.toString());
                    ResultSet set = statement.executeQuery();
                    boolean result = false;
                    while (set.next()){
                        if (player.getUniqueId().toString().equals(set.getString("MinecraftUserID")) && set.getBoolean("IsParent")){
                            result = true;
                        }
                    }
                    set.close();
                    statement.close();

                    if (!result){
                        player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"他の人が保護しているシュルカーボックスです。");
                        con.close();
                        return;
                    }

                    player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"シュルカーボックスを保護解除しました。");
                    PreparedStatement statement1 = con.prepareStatement("UPDATE `LockList` SET `Active` = 0 WHERE BlockID = ?");
                    statement1.setString(1, uuid.toString());
                    statement1.execute();
                    statement1.close();
                    con.close();

                } catch (SQLException ex){
                    ex.printStackTrace();
                }
                return;
            }

            // 保護チェック
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, uuid.toString());
                ResultSet set = statement.executeQuery();

                boolean result = false;
                while (set.next()){
                    result = true;
                    if (e.getPlayer().getUniqueId().toString().equals(set.getString("MinecraftUserID"))){
                        set.close();
                        statement.close();
                        con.close();
                        return;
                    }
                }
                set.close();
                statement.close();
                con.close();

                if (!result){
                    return;
                }
            } catch (SQLException ex){
                ex.printStackTrace();
            }

            e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"他の人が保護しているシュルカーボックスです。");
            e.getView().close();
            e.getPlayer().closeInventory();
            e.setCancelled(true);

        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void BlockBreakEvent(BlockBreakEvent e){
        Block block = e.getBlock();
        if (block.getState() instanceof Chest || block.getState() instanceof ShulkerBox){

            String uuid = "";
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                PreparedStatement statement = con.prepareStatement("SELECT * FROM `LockUUIDList` WHERE world = ? AND x = ? AND y = ? AND z = ? AND Active = 1");
                statement.setString(1, block.getLocation().getWorld().getUID().toString());
                statement.setInt(2, block.getLocation().getBlockX());
                statement.setInt(3, block.getLocation().getBlockY());
                statement.setInt(4, block.getLocation().getBlockZ());
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    uuid = set.getString("UUID");
                }
                set.close();
                statement.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }

            plugin.getLogger().info("test");
            if (uuid.equals("")){
                return;
            }
            plugin.getLogger().info(uuid);
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                PreparedStatement statement = con.prepareStatement("SELECT * FROM LockList WHERE BlockID = ? AND Active = 1");
                statement.setString(1, uuid);
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "保護がかかっています。");
                    e.setCancelled(true);
                }

                set.close();
                statement.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }

            return;
        }

        if (block.getState() instanceof Sign){
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                PreparedStatement statement = con.prepareStatement("SELECT * FROM DeathList WHERE Active = 1 AND x = ? AND y = ? AND z = ?");
                statement.setInt(1, block.getLocation().getBlockX());
                statement.setInt(2, block.getLocation().getBlockY());
                statement.setInt(3, block.getLocation().getBlockZ());
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
