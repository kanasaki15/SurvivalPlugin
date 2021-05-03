package xyz.n7mn.dev.survivalplugin.command;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    private final Plugin plugin;
    public SpawnCommand(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player){
            Player player = (Player) sender;

            if (!player.getLocation().getWorld().getName().equals("world")){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"通常のワールド以外からは戻れません。");
                return true;
            }

            World world = plugin.getServer().getWorld("world");
            player.teleport(world.getSpawnLocation());

            player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"初期スポーン地点に移動しました。");
        }

        return true;
    }
}
