package xyz.n7mn.dev.survivalplugin.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;

import java.util.List;

public class SigenCommand implements CommandExecutor {

    private List<PlayerLocationData> locationDataList;
    private Boolean isMoveWorld;

    public SigenCommand(List<PlayerLocationData> locationDataList, Boolean isMoveWorld){
        this.locationDataList = locationDataList;
        this.isMoveWorld = isMoveWorld;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player){

            Player player = (Player) sender;

            if (player.getLocation().getWorld().getName().startsWith("sigen")){
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RESET + "今いるワールドは資源ワールドです！！");
                return true;
            }

            if (isMoveWorld){
                locationDataList.add(new PlayerLocationData(player, player.getLocation()));

                Inventory inventory = Bukkit.createInventory(player, 9);

                ItemStack sigen = new ItemStack(Material.GRASS_BLOCK);
                sigen.getEnchantmentLevel(Enchantment.BINDING_CURSE);
                sigen.displayName().append(Component.text("資源ワールド"));
                inventory.addItem(sigen);

                ItemStack nether = new ItemStack(Material.NETHER_WART_BLOCK);
                nether.displayName().append(Component.text("資源ネザー"));
                nether.getEnchantmentLevel(Enchantment.BINDING_CURSE);
                inventory.addItem(nether);

                ItemStack end = new ItemStack(Material.END_STONE);
                end.displayName().append(Component.text("資源エンド"));
                end.getEnchantmentLevel(Enchantment.BINDING_CURSE);
                inventory.addItem(end);


                player.openInventory(inventory);
            } else {
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RED + "現在資源ワールドへの移動は禁止されています。");
            }

        }

        return true;
    }
}
