package xyz.n7mn.dev.survivalplugin.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SigenEventListener implements Listener {

    private final Plugin plugin;
    public SigenEventListener(Plugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InventoryClickEvent(InventoryClickEvent e){
        Inventory inventory = e.getInventory();
        //System.out.println("test1");

        if (inventory.getSize() != 9){
            return;
        }

        ItemStack stack = e.getCurrentItem();
        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT){
            e.setCancelled(true);
            return;
        }

        //System.out.println("test2");
        List<Component> lore;
        if (stack.getItemMeta() != null){
            lore = stack.getItemMeta().lore();
        } else {
            lore = new ArrayList<>();
        }
/*
        for (Component component : lore){
            TextComponent text = (TextComponent) component;
            //System.out.println(text.content());
        }
*/

        boolean isTeleport = false;
        if (lore != null && lore.size() > 0){
            TextComponent text = (TextComponent) lore.get(0);

            if (text.content().startsWith("資源通常")){
                e.getWhoClicked().teleport(plugin.getServer().getWorld("sigen").getSpawnLocation());
                isTeleport = true;
            }

            if (text.content().startsWith("資源ネザー")){
                e.getWhoClicked().teleport(plugin.getServer().getWorld("sigen_nether").getSpawnLocation());
                isTeleport = true;
            }

            if (text.content().startsWith("資源エンド")){
                e.getWhoClicked().teleport(plugin.getServer().getWorld("sigen_end").getSpawnLocation());
                isTeleport = true;
            }
        }

        if (isTeleport){
            e.getView().close();
            e.getInventory().close();
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerAdvancementCriterionGrantEvent (PlayerAdvancementCriterionGrantEvent e){
        Location location = e.getPlayer().getLocation();
        if (location.getWorld().getName().startsWith("sigen")){
            e.setCancelled(true);
        }
    }

}
