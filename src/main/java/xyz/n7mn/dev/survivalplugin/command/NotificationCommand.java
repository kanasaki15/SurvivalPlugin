package xyz.n7mn.dev.survivalplugin.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class NotificationCommand implements CommandExecutor {

    private final Plugin plugin;
    private final JDA jda;
    public NotificationCommand(Plugin plugin, JDA jda){
        this.plugin = plugin;
        this.jda = jda;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED){
            if (sender instanceof Player){
                Player player = (Player) sender;

                TextChannel channel = jda.getTextChannelById(plugin.getConfig().getString("NotificationChannel"));
                channel.getHistoryAfter(1, 100).queue(messageHistory -> {
                    List<Message> list = messageHistory.getRetrievedHistory();

                    ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
                    BookMeta meta = (BookMeta) stack.getItemMeta().clone();
                    meta.setAuthor("ななみ生活鯖運営一同");
                    meta.setTitle("ななみ生活鯖おしらせ");
                    meta.setGeneration(BookMeta.Generation.ORIGINAL);

                    // System.out.println("list : " + list.size());

                    int x = 1;
                    for (Message message : list){
                        StringBuffer sb = new StringBuffer(message.getContentRaw());
                        sb.append("\n\n\n");
                        if (message.getMember().getNickname() != null){
                            sb.append(message.getMember().getNickname());
                        } else {
                            sb.append(message.getAuthor().getName());
                        }
                        sb.append("さんから\n");

                        Instant instant = message.getTimeCreated().toInstant();
                        sb.append("(");
                        sb.append(sdf.format(Date.from(instant)));
                        sb.append(")");

                        Component component = Component.text(sb.toString());

                        meta.addPages(Component.text(""));
                        meta.page(x, component);
                        x++;
                    }

                    stack.setItemMeta(meta);
                    player.openBook(stack);
                });

                player.sendMessage("もう一度表示させるには/notiを実行してください。");

            }
        }

        return true;
    }
}
