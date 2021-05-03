package xyz.n7mn.dev.survivalplugin.event;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DiscordonMessageReceivedEvent extends Event {

    private static HandlerList handlerList = new HandlerList();
    private final MessageReceivedEvent event;

    public DiscordonMessageReceivedEvent(MessageReceivedEvent e){
        this.event = e;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }
}
