package xyz.n7mn.dev.survivalplugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.command.*;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;
import xyz.n7mn.dev.survivalplugin.event.DiscordonMessageReceivedEvent;
import xyz.n7mn.dev.survivalplugin.listener.EventListener;
import xyz.n7mn.dev.survivalplugin.tab.PlayerTabList;
import xyz.n7mn.dev.survivalplugin.tab.UserHomeList;
import xyz.n7mn.dev.survivalplugin.timer.WorldReCreateTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SurvivalPlugin extends JavaPlugin {

    private Boolean isMoveWorld = true;
    private List<UUID> list = new ArrayList<>();
    private List<PlayerLocationData> PlayerList = new ArrayList<>();
    private BukkitTask task = null;

    private JDA jda = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        try {
            jda = JDABuilder.createLight(getConfig().getString("discordToken"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EMOJIS)
                    .addEventListeners(new ListenerAdapter() {
                        @Override
                        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                            new Thread(()->{
                                Bukkit.getServer().getPluginManager().callEvent(new DiscordonMessageReceivedEvent(event));
                            }).start();
                        }
                    })
                    .enableCache(CacheFlag.VOICE_STATE)
                    .enableCache(CacheFlag.EMOTE)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();
        } catch (Exception e){
            e.printStackTrace();
        }

        getServer().createWorld(WorldCreator.name("sigen"));
        World sigen = getServer().getWorld("sigen");
        sigen.setTime(getServer().getWorld("world").getTime());

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

        getServer().getPluginManager().registerEvents(new EventListener(this, list, jda), this);

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

    public boolean isMoveWorld() {
        return isMoveWorld;
    }

    public void setMoveWorld(boolean moveWorld) {
        isMoveWorld = moveWorld;
    }
}
