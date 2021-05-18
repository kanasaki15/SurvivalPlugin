package xyz.n7mn.dev.survivalplugin.function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.SurvivalPlugin;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;

import java.util.List;

public class WorldReCreate {

    public static void run(Plugin plugin){
        SurvivalPlugin p = (SurvivalPlugin) plugin;
        p.setMoveWorld(false);
        plugin.getLogger().info("定期リセット処理 開始");
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "資源ワールドを再生成します。移動しないでください。");
            }

            List<PlayerLocationData> list = p.getPlayerList();

            plugin.getLogger().info("プレーヤー移動開始 ("+list.size()+"人)");
            for (PlayerLocationData data : list){
                data.getPlayer().teleport(data.getLocation());
                data.getPlayer().sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] "+ChatColor.RESET+"資源ワールドからメインワールドへ移動しました。");
                plugin.getLogger().info("---> "+data.getPlayer().getName()+"を移動しました");
            }
            list.clear();
            plugin.getLogger().info("プレーヤー移動完了");
            plugin.getLogger().info("ワールドアンロード開始");
            World sigen = plugin.getServer().getWorld("sigen");
            plugin.getServer().unloadWorld(sigen, false);
            plugin.getLogger().info("ワールドアンロード完了");
            plugin.getLogger().info("ワールド削除開始");
            sigen.getWorldFolder().deleteOnExit();
            plugin.getLogger().info("ワールド削除完了");
            plugin.getLogger().info("ワールド作成開始");
            World world = plugin.getServer().createWorld(WorldCreator.name("sigen"));
            world.setTime(plugin.getServer().getWorld("world").getTime());
            plugin.getLogger().info("ワールド作成完了 seed : " + world.getSeed());

            p.setMoveWorld(true);
            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "資源ワールドの再生成が完了しました。 移動可能です。");
            }
        });

    }

}
