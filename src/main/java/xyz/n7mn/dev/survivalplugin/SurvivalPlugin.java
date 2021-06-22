package xyz.n7mn.dev.survivalplugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.survivalplugin.command.*;
import xyz.n7mn.dev.survivalplugin.data.LockCommandUser;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;
import xyz.n7mn.dev.survivalplugin.listener.ChestLockListener;
import xyz.n7mn.dev.survivalplugin.listener.DiscordListner;
import xyz.n7mn.dev.survivalplugin.listener.ItemframeLockListener;
import xyz.n7mn.dev.survivalplugin.listener.PaperEventListener;
import xyz.n7mn.dev.survivalplugin.tab.PlayerTabList;
import xyz.n7mn.dev.survivalplugin.tab.UserHomeList;
import xyz.n7mn.dev.survivalplugin.timer.WorldReCreateTimer;

import java.util.*;

public final class SurvivalPlugin extends JavaPlugin {

    private Boolean isMoveWorld = true;
    // private List<UUID> list = new ArrayList<>();
    private List<PlayerLocationData> PlayerList = new ArrayList<>();
    private List<LockCommandUser> lockUserList = new ArrayList<>();

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


        // ワールド関連
        World world = getServer().getWorld("world");
        World nether = getServer().getWorld("world_nether");
        World the_end = getServer().getWorld("world_the_end");

        WorldCreator sigen_world = WorldCreator.name("sigen");
        WorldCreator sigen_n = WorldCreator.name("sigen_nether");
        sigen_n.environment(World.Environment.NETHER);
        WorldCreator sigen_end = WorldCreator.name("sigen_end");
        sigen_end.environment(World.Environment.THE_END);

        getServer().createWorld(sigen_world);
        getServer().createWorld(sigen_n);
        getServer().createWorld(sigen_end);


        World sigen = getServer().getWorld("sigen");
        World sigen_nether = getServer().getWorld("sigen_nether");
        World sigen_theend = getServer().getWorld("sigen_end");
        if (world != null){
            world.setPVP(false);
        }

        if (nether != null){
            nether.setPVP(false);
        }

        if (the_end != null){
            the_end.setPVP(false);
        }

        if (sigen != null){
            if (world != null){
                sigen.setFullTime(world.getFullTime());
            }
            sigen.setPVP(false);
            getLogger().info("資源ワールドの読み込みに成功しました。 seed : " + sigen.getSeed());
        }

        if (sigen_nether != null){
            if (world != null){
                sigen_nether.setFullTime(world.getFullTime());
            }
            sigen_nether.setPVP(false);
            getLogger().info("資源ネザーワールドの読み込みに成功しました。 seed : " + sigen_nether.getSeed());
        }

        if (sigen_theend != null){
            if (world != null){
                sigen_theend.setFullTime(world.getFullTime());
            }
            sigen_theend.setPVP(false);
            getLogger().info("資源エンドワールドの読み込みに成功しました。 seed : " + sigen_theend.getSeed());
        }

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

        getCommand("de").setExecutor(new DeathCommand());

        getServer().getPluginManager().registerEvents(new PaperEventListener(this, jda), this);
        getServer().getPluginManager().registerEvents(new ChestLockListener(this, lockUserList), this);
        getServer().getPluginManager().registerEvents(new ItemframeLockListener(this, lockUserList), this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, new WorldReCreateTimer(this), 0L, 20L);
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

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了しました。");
    }

    public List<PlayerLocationData> getPlayerList() {
        return PlayerList;
    }

    public void setMoveWorld(boolean moveWorld) {
        isMoveWorld = moveWorld;
    }
}
