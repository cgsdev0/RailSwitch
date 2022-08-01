package sh.okx.railswitch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.network.chat.BaseComponent;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerListener implements Listener {

    private RailSwitchPlugin plugin;

    public PlayerListener(RailSwitchPlugin pl) {
        this.plugin = pl;
    }

    public BookMeta updateBookMeta(BookMeta book, Location loc, String playerDest) {
        HashSet<String> dests = playerDest == null ? new HashSet<String>() : new HashSet<String>(Arrays.asList(playerDest.split(" ")));
        book.setTitle("Railway Guide");
        book.setAuthor("Server");
        book.setPages();
        TextComponent.Builder builder = Component.text().append(Component.text("NEARBY\n--------\n"));
        int i = 0;
        for (String dest : plugin.listNearbyDestinations(loc, 500D)) {
            if (++i >= 12) {
              break;
            }
            NamedTextColor color = dests.contains(dest) ? NamedTextColor.GOLD : NamedTextColor.BLUE;
            builder.append(Component.text(" - "), Component.text(dest, color).decoration(TextDecoration.UNDERLINED, true).hoverEvent(HoverEvent.showText(Component.text().append(Component.text().content("Click to set destination").build()))).clickEvent(ClickEvent.runCommand("/dest " + dest)), Component.newline());
        }

        if (i == 0) {
          builder.append(Component.text("\n\n[none]\n\n"));
        }
        book.addPages(builder.build());
        builder = Component.text()
                .append(Component.text("ALL DESTINATIONS\n----------------\n"));

        i = 0;
        for (String dest : plugin.listAllDestinations()) {
            if (++i >= 12) {
                builder.append(Component.text("continued ->", NamedTextColor.GRAY));
                book.addPages(builder.build());
                builder = Component.text()
                        .append(Component.text("ALL DESTINATIONS\n----------------\n"));
                i = 0;
            }
            NamedTextColor color = dests.contains(dest) ? NamedTextColor.GOLD : NamedTextColor.DARK_GREEN;
            builder.append(Component.text(" - "), Component.text(dest, color).decoration(TextDecoration.UNDERLINED, true).hoverEvent(HoverEvent.showText(Component.text().append(Component.text().content("Click to set destination").build()))).clickEvent(ClickEvent.runCommand("/dest " + dest)), Component.newline());
        }
        book.addPages(builder.build());
        return book;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLecternInteract(PlayerInteractEvent event) {

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LECTERN) return;

        Lectern lectern = (Lectern) block.getState();
        ItemStack book = lectern.getInventory().getItem(0);
        if (book == null) return;


        ItemMeta meta = book.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(CraftableRailBook.tagKey, PersistentDataType.INTEGER)) {
            return;
        }

        BookMeta bookMeta = updateBookMeta((BookMeta)meta, lectern.getLocation(), plugin.getDestination(event.getPlayer()));

        ItemStack stack = book.clone();
        stack.setItemMeta(bookMeta);
        lectern.getInventory().setItem(0, stack);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.useItemInHand() == Event.Result.DENY)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getItem() == null || event.getItem().getType() != Material.WRITTEN_BOOK)
            return;

        if (!event.getItem().hasItemMeta())
            return;


        ItemMeta meta = event.getItem().getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(CraftableRailBook.tagKey, PersistentDataType.INTEGER)) {
            return;
        }

        BookMeta book = updateBookMeta((BookMeta)meta, event.getPlayer().getLocation(), plugin.getDestination(event.getPlayer()));

        ItemStack stack = event.getItem().clone();
        stack.setItemMeta(book);
        if (event.getHand() == EquipmentSlot.HAND) {
            event.getPlayer().getInventory().setItemInMainHand(stack);
        } else if(event.getHand() == EquipmentSlot.OFF_HAND) {
            event.getPlayer().getInventory().setItemInOffHand(stack);
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
      if (!Tag.BANNERS.isTagged(event.getBlock().getType())) {
        return;
      }
      plugin.removeBanner(event.getBlock().getLocation());
    }
}
