package xyz.n7mn.dev.survivalplugin.command;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.data.MCID2UUID;
import xyz.n7mn.dev.survivalplugin.data.SurvivalUser;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class UserCommand implements CommandExecutor {

    private final Plugin plugin;
    public UserCommand(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("7miseikatu.op") && sender.isOp()){
            sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RED + "そのコマンドを実行する権限はありません。");
            return true;
        }

        new Thread(()->{
            List<SurvivalUser> userList = new ArrayList<>();
            try {
                boolean found = false;
                Enumeration<Driver> drivers = DriverManager.getDrivers();

                while (drivers.hasMoreElements()){
                    Driver driver = drivers.nextElement();
                    if (driver.equals(new com.mysql.cj.jdbc.Driver())){
                        found = true;
                        break;
                    }
                }

                if (!found){
                    DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
                }

                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("mysqlServer") + ":" + plugin.getConfig().getInt("mysqlPort") + "/" + plugin.getConfig().getString("mysqlDatabase") + plugin.getConfig().getString("mysqlOption"), plugin.getConfig().getString("mysqlUsername"), plugin.getConfig().getString("mysqlPassword"));
                PreparedStatement statement = con.prepareStatement("SELECT * FROM SurvivalUser");
                ResultSet set = statement.executeQuery();
                while (set.next()){
                    userList.add(new SurvivalUser(
                            UUID.fromString(set.getString("UUID")),
                            set.getDate("FirstJoinDate"),
                            set.getDate("LastJoinDate"),
                            UUID.fromString(set.getString("RoleID")),
                            set.getLong("Count")
                    ));
                }
                set.close();
                statement.close();
                con.close();


            } catch (Exception e){
                e.printStackTrace();
            }

            if (args.length == 0){
                sender.sendMessage(ChatColor.YELLOW + "" +
                        "--- ななみ生活鯖 情報 ---\n" +
                        "オンラインユーザー数 : " + plugin.getServer().getOnlinePlayers().size()+" 人\n" +
                        "累計訪問数 : " + userList.size()
                );
                return;
            }

            if (args.length == 1){

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.mojang.com/users/profiles/minecraft/"+args[0])
                        .build();

                String s = "{}";
                try {
                    Response response = client.newCall(request).execute();
                    s = response.body().string();
                    response.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
                MCID2UUID json = new Gson().fromJson(s, MCID2UUID.class);
                UUID uuid = UUID.fromString(json.getId().replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                boolean found = false;
                for (SurvivalUser user : userList){
                    if (!user.getUUID().equals(uuid)){
                        continue;
                    }

                    found = true;
                    sender.sendMessage("" +
                            "--- "+args[0]+"さんの情報 ---\n" +
                            "UUID : " + uuid.toString() + "\n" +
                            "初参加 : " + sdf.format(user.getFirstJoinDate()) + "\n" +
                            "最終ログイン : " + sdf.format(user.getLastJoinDate()) + "\n" +
                            "参加回数 : " + user.getCount()
                    );
                    break;
                }

                if (!found){
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "そのユーザーの情報は見つかりませんでした。");
                }
            }

        }).start();


        return true;
    }
}
