package xyz.n7mn.dev.survivalplugin.command;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Enumeration;
import java.util.UUID;

public class SetHomeCommand implements CommandExecutor {

    private final Plugin plugin;

    public SetHomeCommand(Plugin plugin){
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length != 1){
            sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"/sethome <名前>でワープ位置を設定できます。");
            return true;
        }

        new Thread(()->{

            if (args[0].length() > 20){
                sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"名前は20文字以内でお願いします。");
                return;
            }

            if (sender instanceof Player){
                Player player = (Player) sender;

                if (!player.getLocation().getWorld().getName().equals("world")){
                    player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"メインワールド以外では設定できません。");
                    return;
                }

                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                    PreparedStatement statement = con.prepareStatement("SELECT * FROM HomeList WHERE MinecraftUser = ? AND Name = ?");
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setString(2, args[0]);

                    ResultSet set = statement.executeQuery();
                    if (set.next()){
                        player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"すでに設定されている名前です。");
                        set.close();
                        statement.close();
                        con.close();
                        return;
                    }
                    set.close();
                    statement.close();
                    con.close();
                } catch (SQLException e){
                    e.printStackTrace();
                }

                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));

                    PreparedStatement statement = con.prepareStatement("INSERT INTO `HomeList` (`ID`, `MinecraftUser`, `Name`, `x`, `y`, `z`) VALUES (?, ?, ?, ?, ?, ?) ");
                    statement.setString(1, UUID.randomUUID().toString());
                    statement.setString(2, player.getUniqueId().toString());
                    statement.setString(3, args[0]);
                    statement.setInt(4, player.getLocation().getBlockX());
                    statement.setInt(5, player.getLocation().getBlockY());
                    statement.setInt(6, player.getLocation().getBlockZ());
                    statement.execute();
                    statement.close();
                    con.close();
                } catch (SQLException e){
                    e.printStackTrace();
                }

                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+args[0]+"という名前でX:"+player.getLocation().getBlockX()+" Y:"+player.getLocation().getBlockY() + " Z:"+player.getLocation().getBlockZ()+" に設定しました。");
                TextComponent component = new TextComponent(ChatColor.UNDERLINE + "削除するには「/delhome "+args[0]+"」と入力してください。");
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delhome " + args[0]));
                player.sendMessage(component);
            }

        }).start();

        return true;
    }
}
