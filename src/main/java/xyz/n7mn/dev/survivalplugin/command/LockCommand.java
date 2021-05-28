package xyz.n7mn.dev.survivalplugin.command;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;
import xyz.n7mn.dev.survivalplugin.data.MCID2UUID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"保護したい対象のモノを右クリックしてください。\n(保護可能な種類：チェスト/額縁/シュルカーボックス)");
                return true;
            } else if (player.getUniqueId().equals(user.getUserUUID())){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"まずは保護解除の操作を完了させてください。");
                return true;
            }
        }

        if (args.length == 0){
            list.add(new LockCommandUser(player.getUniqueId(), true));
        } else if (args.length == 1) {
            Player adduser = Bukkit.getServer().getPlayer(args[0]);
            if (adduser == null) {
                for (Player on : Bukkit.getServer().getOnlinePlayers()){
                    if (on.getName().toLowerCase().equals(args[0].toLowerCase())){
                        adduser = on;
                        break;
                    }
                }

                if (adduser == null){
                    for (OfflinePlayer off : Bukkit.getServer().getOfflinePlayers()){
                        if (off.getName().toLowerCase().equals(args[0].toLowerCase())){
                            adduser = off.getPlayer();
                            break;
                        }
                    }
                }
            }

            UUID addUserUUID = null;
            if (adduser == null){
                OkHttpClient client = new OkHttpClient();
                try {
                    Request request = new Request.Builder()
                            .url("https://api.mojang.com/users/profiles/minecraft/"+args[0])
                            .build();

                    Response response = client.newCall(request).execute();
                    addUserUUID = UUID.fromString(new Gson().fromJson(response.body().string(), MCID2UUID.class).getId().replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
                    response.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            } else {
                addUserUUID = adduser.getUniqueId();
            }

            if (addUserUUID == null){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"存在しないユーザー名です。");
                return true;
            }

            list.add(new LockCommandUser(player.getUniqueId(), true, addUserUUID));
        }

        player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"保護したい対象のモノを右クリックしてください。\n(保護可能な種類：チェスト/額縁/シュルカーボックス)");

        return true;
    }
}
