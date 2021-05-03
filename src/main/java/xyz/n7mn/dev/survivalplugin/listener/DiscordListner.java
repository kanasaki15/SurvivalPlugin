package xyz.n7mn.dev.survivalplugin.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.event.DiscordonMessageReceivedEvent;

public class DiscordListner extends ListenerAdapter {

    private final Plugin plugin;
    public DiscordListner(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Bukkit.getScheduler().runTask(plugin, bu->{
            Bukkit.getPluginManager().callEvent(new DiscordonMessageReceivedEvent(event));
        });
    }

}
