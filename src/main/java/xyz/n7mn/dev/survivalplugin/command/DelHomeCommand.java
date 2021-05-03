package xyz.n7mn.dev.survivalplugin.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.UUID;

public class DelHomeCommand implements CommandExecutor {

    private final Plugin plugin;
    public DelHomeCommand(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1){
            sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"/delhome <名前>で削除できます。");
            return true;
        }

        if (sender instanceof Player){
            new Thread(()->{
                Player player = (Player) sender;

                try {
                    Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                    PreparedStatement statement = con.prepareStatement("SELECT * FROM HomeList WHERE MinecraftUser = ? AND Name = ?");
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setString(2, args[0]);
                    ResultSet set = statement.executeQuery();
                    if (set.next()){
                        UUID uuid = UUID.fromString(set.getString("ID"));
                        set.close();
                        statement.close();

                        statement = con.prepareStatement("DELETE FROM `HomeList` WHERE ID = ?");
                        statement.setString(1, uuid.toString());
                        statement.execute();
                        statement.close();

                        sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+args[0]+"を削除しました。");
                        con.close();
                        return;
                    }
                    set.close();
                    statement.close();
                    con.close();

                    sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+args[0]+"は見つかりませんでした。");
                } catch (SQLException e){
                    e.printStackTrace();
                }


            }).start();
        }

        return true;
    }
}
