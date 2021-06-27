package xyz.n7mn.dev.survivalplugin.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalplugin.data.PlayerLocationData;

import java.util.ArrayList;
import java.util.Collections;
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

                Inventory inventory = Bukkit.createInventory(player, 9, Component.text("ワールド選択"));

                ItemStack sigen = new ItemStack(Material.GRASS_BLOCK);
                ItemMeta meta1 = sigen.getItemMeta().clone();
                meta1.lore(Collections.singletonList(Component.text("資源通常ワールド")));
                sigen.setItemMeta(meta1);
                inventory.addItem(sigen);

                ItemStack nether = new ItemStack(Material.NETHER_WART_BLOCK);
                ItemMeta meta2 = sigen.getItemMeta().clone();
                meta2.lore(Collections.singletonList(Component.text("資源ネザーワールド")));
                nether.setItemMeta(meta2);
                inventory.addItem(nether);

                ItemStack end = new ItemStack(Material.END_STONE);
                ItemMeta meta3 = sigen.getItemMeta().clone();
                meta3.lore(Collections.singletonList(Component.text("資源エンドワールド")));
                end.setItemMeta(meta3);
                inventory.addItem(end);

                for (int i = 3; i < inventory.getSize(); i++){
                    inventory.addItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
                }

                player.openInventory(inventory);
            } else {
                player.sendMessage(ChatColor.YELLOW + "[ななみ生活鯖] " + ChatColor.RED + "現在資源ワールドへの移動は禁止されています。");
            }

        }

        return true;
    }
}
