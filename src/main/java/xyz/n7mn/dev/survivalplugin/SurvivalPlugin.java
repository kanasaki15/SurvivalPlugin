package xyz.n7mn.dev.survivalplugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.survivalplugin.listener.*;


public final class SurvivalPlugin extends JavaPlugin {
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

        getServer().getPluginManager().registerEvents(new PaperEventListener(this, jda), this);
        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 起動しました。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (jda != null){
            jda.shutdownNow();
        }

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了しました。");
    }

}
