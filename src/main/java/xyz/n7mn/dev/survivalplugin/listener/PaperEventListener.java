package xyz.n7mn.dev.survivalplugin.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.JDA;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.survivalplugin.event.DiscordonMessageReceivedEvent;

public class PaperEventListener implements Listener {

    private final Plugin plugin;
    private final JDA jda;

    public PaperEventListener(Plugin plugin, JDA jda){
        this.plugin = plugin;
        this.jda = jda;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerJoinEvent (PlayerJoinEvent e){

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerQuitEvent(PlayerQuitEvent e){

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncChatEvent (AsyncChatEvent e){

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void DiscordOnMessageReceivedEvent (DiscordonMessageReceivedEvent e){

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerDeathEvent(PlayerDeathEvent e){

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerInteractEvent (PlayerInteractEvent e){

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void EntityExplodeEvent (EntityExplodeEvent e){
        e.setCancelled(true);
    }


}
