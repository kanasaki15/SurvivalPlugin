package xyz.n7mn.dev.survivalplugin.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;

import java.util.ArrayList;
import java.util.List;

public class LockCommand implements CommandExecutor {

    private List<LockCommandUser> list;
    public LockCommand(List<LockCommandUser> list){
        this.list = list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)){
            return true;
        }

        Player player = (Player) sender;

        if (list == null){
            list = new ArrayList<>();
        }

        for (LockCommandUser user : list){
            if (player.getUniqueId().equals(user.getUserUUID()) && user.isAdd()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"保護したい対象のモノを右クリックしてください。(現在はチェストのみ可能)");
                return true;
            } else if (player.getUniqueId().equals(user.getUserUUID())){
                player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"まずは保護解除の操作を完了させてください。");
                return true;
            }
        }

        list.add(new LockCommandUser(player.getUniqueId(), true));
        player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET+"保護したい対象のモノを右クリックしてください。(現在はチェストのみ可能)");

        return true;
    }
}
