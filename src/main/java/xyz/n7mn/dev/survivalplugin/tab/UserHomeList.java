package xyz.n7mn.dev.survivalplugin.tab;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserHomeList implements TabExecutor {

    private final Plugin plugin;
    public UserHomeList(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0){
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        List<String> list = new ArrayList<>();
        if (sender instanceof Player){
            Player player = (Player) sender;

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM HomeList WHERE MinecraftUser = ?");
                statement.setString(1, player.getUniqueId().toString());
                ResultSet set = statement.executeQuery();
                while (set.next()){
                    list.add(set.getString("Name"));
                }
                set.close();
                statement.close();
                con.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }

        return list;
    }
}
