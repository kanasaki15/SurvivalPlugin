package xyz.n7mn.dev.survivalplugin.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayerTabList implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1){
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        for (Player player : Bukkit.getServer().getOnlinePlayers()){
            list.add(player.getName());
        }

        if (args.length == 1 && args[0].length() > 0){
            List<String> list2 = new ArrayList<>(list);
            for (String str : list2){
                if (str.startsWith(args[0])){
                    continue;
                }

                list.remove(str);
            }
        }

        return list;
    }
}
