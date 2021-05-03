package xyz.n7mn.dev.survivalplugin.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Enumeration;

public class HomeCommand implements CommandExecutor {

    private final Plugin plugin;

    public HomeCommand(Plugin plugin){
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
            sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"設定した場所に飛ぶには/home <名前>です。");
            return true;
        }

        try {
            if (sender instanceof Player){
                Player player = (Player) sender;

                if (!player.getLocation().getWorld().getName().equals("world")){
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"通常のワールドに戻ってから実行してください。");
                    return true;
                }

                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM HomeList WHERE MinecraftUser = ? AND Name = ?");
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, args[0]);
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    player.teleport(new Location(player.getWorld(), set.getInt("x"), set.getInt("y"), set.getInt("z")));
                    player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+args[0]+"へ移動しました。");
                    set.close();
                    statement.close();
                    con.close();
                    return true;
                }

                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"指定の行き先が見つからないようです。");
                set.close();
                statement.close();
                con.close();
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        return true;
    }
}
