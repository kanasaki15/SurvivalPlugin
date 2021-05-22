package xyz.n7mn.dev.survivalplugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.survivalplugin.command.*;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;
import xyz.n7mn.dev.survivalplugin.listener.DiscordListner;
import xyz.n7mn.dev.survivalplugin.listener.EventListener;
import xyz.n7mn.dev.survivalplugin.tab.PlayerTabList;
import xyz.n7mn.dev.survivalplugin.tab.UserHomeList;
import xyz.n7mn.dev.survivalplugin.timer.WorldReCreateTimer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class SurvivalPlugin extends JavaPlugin {

    private Boolean isMoveWorld = true;
    // private List<UUID> list = new ArrayList<>();
    private List<PlayerLocationData> PlayerList = new ArrayList<>();
    private List<LockCommandUser> lockUserList = new ArrayList<>();

    private Map<UUID, Location> chestList = new HashMap<>();
    private Map<UUID, Location> graveList = new HashMap<>();


    private JDA jda = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        try {
            jda = JDABuilder.createLight(getConfig().getString("discordToken"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EMOJIS)
                    .addEventListeners(new DiscordListner(this))
                    .enableCache(CacheFlag.VOICE_STATE)
                    .enableCache(CacheFlag.EMOTE)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();
        } catch (Exception e){
            e.printStackTrace();
        }

        getServer().createWorld(WorldCreator.name("sigen"));
        World sigen = getServer().getWorld("sigen");
        World world = getServer().getWorld("world");
        sigen.setTime(world.getTime());
        sigen.setPVP(false);
        world.setPVP(false);

        getLogger().info("資源ワールドの読み込みに成功しました。 seed : " + sigen.getSeed());

        getCommand("sigen").setExecutor(new SigenCommand(PlayerList, isMoveWorld));
        getCommand("main").setExecutor(new MainCommand(PlayerList));

        getCommand("user").setExecutor(new UserCommand(this));
        getCommand("user").setTabCompleter(new PlayerTabList());

        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("sethome").setTabCompleter(new UserHomeList(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("home").setTabCompleter(new UserHomeList(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("delhome").setTabCompleter(new UserHomeList(this));

        getCommand("spawn").setExecutor(new SpawnCommand(this));

        getCommand("noti").setExecutor(new NotificationCommand(this, jda));

        getCommand("lock").setExecutor(new LockCommand(lockUserList));
        getCommand("unlock").setExecutor(new UnLockCommand(lockUserList));

        getServer().getPluginManager().registerEvents(new EventListener(this, jda, lockUserList, chestList, graveList), this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, new WorldReCreateTimer(this), 0L, 20L);

        // チェスト保護リスト読み込み
        File file_c = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/chest/");
        if (file_c.exists()){
            for (String pass : file_c.list()){
                // getLogger().info("debug : " + pass);
                File file = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/chest/" + pass);
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);

                    UUID uuid = UUID.fromString(config.getString("world"));
                    int x = config.getInt("X");
                    int y = config.getInt("Y");
                    int z = config.getInt("Z");

                    World target = getServer().getWorld(uuid);
                    if (target != null){
                        Block block = new Location(target, x, y, z).getBlock();
                        block.setMetadata("uuid", new FixedMetadataValue(this, uuid.toString()));
                    }

                } catch (IOException | InvalidConfigurationException e){
                    e.printStackTrace();
                    Bukkit.getServer().getPluginManager().disablePlugin(this);
                }
            }
        }
        file_c.deleteOnExit();

        // 墓リスト読み込み
        File file_g = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/grave/");
        if (file_g.exists()){
            for (String pass : file_g.list()){
                // getLogger().info("debug : " + pass);
                File file = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/grave/" + pass);
                YamlConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);

                    UUID uuid = UUID.fromString(config.getString("world"));
                    int x = config.getInt("X");
                    int y = config.getInt("Y");
                    int z = config.getInt("Z");

                    World target = getServer().getWorld(uuid);
                    if (target != null){
                        Block block = new Location(target, x, y, z).getBlock();
                        block.setMetadata("DeathUUID", new FixedMetadataValue(this, uuid.toString()));
                    }
                } catch (IOException | InvalidConfigurationException e){
                    e.printStackTrace();
                    Bukkit.getServer().getPluginManager().disablePlugin(this);
                }
            }
        }

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 起動しました。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (jda != null){
            jda.shutdownNow();
        }

        for (PlayerLocationData data : PlayerList){
            data.getPlayer().teleport(data.getLocation());
        }
        PlayerList = new ArrayList<>();

        // チェスト保護リスト
        File file1 = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/chest/");
        if (!file1.exists()){
            file1.mkdir();
        }
        chestList.forEach((uuid, location) -> {
            File file2 = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/chest/"+uuid.toString()+".yml");
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.set("world", location.getWorld().getUID().toString());
                config.set("X", location.getBlockX());
                config.set("Y", location.getBlockY());
                config.set("Z", location.getBlockZ());
                config.save(file2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 墓リスト
        File file3 = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/grave/");
        if (!file3.exists()){
            file3.mkdir();
        }
        graveList.forEach((uuid, location) -> {
            File file4 = new File("./" + this.getDataFolder().getPath().replaceAll("\\\\", "/") + "/grave/"+uuid.toString()+".yml");
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.set("world", location.getWorld().getUID().toString());
                config.set("X", location.getBlockX());
                config.set("Y", location.getBlockY());
                config.set("Z", location.getBlockZ());
                config.save(file4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了しました。");
    }

    public List<PlayerLocationData> getPlayerList() {
        return PlayerList;
    }

    public boolean isMoveWorld() {
        return isMoveWorld;
    }

    public void setMoveWorld(boolean moveWorld) {
        isMoveWorld = moveWorld;
    }
}
