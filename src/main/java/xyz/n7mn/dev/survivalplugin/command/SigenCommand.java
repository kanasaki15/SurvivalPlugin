package xyz.n7mn.dev.survivalplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;

import java.util.List;

public class SigenCommand implements CommandExecutor {

    private List<PlayerLocationData> locationDataList;
    private Boolean isMoveWorld;

    public SigenCommand(List<PlayerLocationData> locationDataList, Boolean isMoveWorld){
        this.locationDataList = locationDataList;
        this.isMoveWorld = isMoveWorld;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player){

            Player player = (Player) sender;

            if (isMoveWorld){
                locationDataList.add(new PlayerLocationData(player, player.getLocation()));
                Location location = Bukkit.getWorld("sigen").getSpawnLocation();
                player.teleport(location);

                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "資源ワールドへ移動しました。 戻るには「/main」と入力してください。");
            } else {
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RED + "現在資源ワールドへの移動は禁止されています。");
            }

        }

        return true;
    }
}
