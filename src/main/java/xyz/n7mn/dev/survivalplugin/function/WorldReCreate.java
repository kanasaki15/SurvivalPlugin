package xyz.n7mn.dev.survivalplugin.function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.SurvivalPlugin;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;

import java.io.File;
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

            plugin.getLogger().info("プレーヤー移動開始");
            int i = 0;
            for (Player player : plugin.getServer().getOnlinePlayers()){
                if (player.getLocation().getWorld().getName().startsWith("sigen")){
                    i++;
                    if (player.getBedSpawnLocation() != null && !player.getBedSpawnLocation().getWorld().getName().startsWith("sigen")){
                        player.teleport(player.getBedSpawnLocation());
                        continue;
                    }
                    player.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
                }
            }
            plugin.getLogger().info("プレーヤー移動完了 ("+i+"人)");
            plugin.getLogger().info("ワールドアンロード開始");
            World sigen1 = plugin.getServer().getWorld("sigen");
            World sigen2 = plugin.getServer().getWorld("sigen_nether");
            World sigen3 = plugin.getServer().getWorld("sigen_end");
            plugin.getServer().unloadWorld(sigen1, false);
            plugin.getServer().unloadWorld(sigen2, false);
            plugin.getServer().unloadWorld(sigen3, false);
            plugin.getLogger().info("ワールドアンロード完了");
            plugin.getLogger().info("ワールド削除開始");

            File w1 = sigen1.getWorldFolder();
            String path = w1.getPath().replaceAll("\\\\","/");
            //System.out.println(path);
            for (String pass : w1.list()){
                //System.out.println(pass);
                File w2 = new File(path + "/" + pass);
                if (w2.isDirectory()){
                    for (String pass2 : w2.list()){
                        File w3 = new File(path + "/" + pass + "/" + pass2);
                        w3.delete();
                    }

                }
                w2.delete();
            }
            w1.deleteOnExit();

            w1 = sigen2.getWorldFolder();
            path = w1.getPath().replaceAll("\\\\","/");
            for (String pass : w1.list()){
                //System.out.println(pass);
                File w2 = new File(path + "/" + pass);
                if (w2.isDirectory()){
                    for (String pass2 : w2.list()){
                        File w3 = new File(path + "/" + pass + "/" + pass2);
                        w3.delete();
                    }

                }
                w2.delete();
            }
            w1.deleteOnExit();

            w1 = sigen3.getWorldFolder();
            path = w1.getPath().replaceAll("\\\\","/");
            for (String pass : w1.list()){
                //System.out.println(pass);
                File w2 = new File(path + "/" + pass);
                if (w2.isDirectory()){
                    for (String pass2 : w2.list()){
                        File w3 = new File(path + "/" + pass + "/" + pass2);
                        w3.delete();
                    }

                }
                w2.delete();
            }
            w1.deleteOnExit();

            plugin.getLogger().info("ワールド削除完了");
            plugin.getLogger().info("ワールド作成開始");
            World world1 = plugin.getServer().createWorld(WorldCreator.name("sigen"));
            WorldCreator temp_nether = WorldCreator.name("sigen_nether");
            temp_nether.environment(World.Environment.NETHER);
            World world2 = plugin.getServer().createWorld(temp_nether);
            WorldCreator temp_end = WorldCreator.name("sigen_end");
            temp_end.environment(World.Environment.THE_END);
            World world3 = plugin.getServer().createWorld(temp_end);
            world1.setFullTime(plugin.getServer().getWorld("world").getFullTime());
            world2.setFullTime(plugin.getServer().getWorld("world").getFullTime());
            world3.setFullTime(plugin.getServer().getWorld("world").getFullTime());
            plugin.getLogger().info("" +
                    "ワールド作成完了\n" +
                    "sigen        : "+world1.getSeed() + "\n" +
                    "sigen_nether : "+world2.getSeed() + "\n" +
                    "sigen_end    : "+world3.getSeed() + "\n"
            );

            p.setMoveWorld(true);
            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "資源ワールドの再生成が完了しました。 移動可能です。");
            }
        });

    }

}
