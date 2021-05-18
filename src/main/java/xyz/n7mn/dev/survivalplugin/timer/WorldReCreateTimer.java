package xyz.n7mn.dev.survivalplugin.timer;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.function.WorldReCreate;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.*;

public class WorldReCreateTimer implements Runnable {

    private final Plugin plugin;

    public WorldReCreateTimer(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {

        Date date = new Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);

        int week = instance.get(DAY_OF_WEEK);
        int hour = instance.get(HOUR);
        int minute = instance.get(MINUTE);
        int second = instance.get(SECOND);

        int ap = instance.get(AM_PM);

        if (ap == 1 && hour == 1 && week == 7 && second == 0){
            String text = "";
            if (minute == 50){
                text = "あと10分で資源ワールドを再生成します。 貴重品はメインワールドに持ち帰ってください。";
                plugin.getLogger().info("定期リセット処理 10分前");
            }

            if (minute == 55){
                text = "あと5分で資源ワールドを再生成します。 貴重品はメインワールドに持ち帰ってください。";
                plugin.getLogger().info("定期リセット処理 5分前");
            }

            if (minute == 59){
                text = "あと1分で資源ワールドを再生成します。 貴重品はメインワールドに持ち帰ってください。";
                plugin.getLogger().info("定期リセット処理 1分前");
            }

            if (text.length() == 0){
                return;
            }

            for (Player player : plugin.getServer().getOnlinePlayers()){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + text);
            }
            return;
        }

        if (ap == 1 && hour == 2 && minute == 0 && second == 0 && week == 7){
            WorldReCreate.run(plugin);
        }

    }
}
