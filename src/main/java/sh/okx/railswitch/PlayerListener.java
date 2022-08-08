package sh.okx.railswitch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.dynmap.markers.MarkerSet;

import com.eatthepath.jvptree.*;

public class PlayerListener implements Listener {
  public class LocationDistFunc implements DistanceFunction<Location> {
    public double getDistance(final Location firstPoint, final Location secondPoint) {
      return firstPoint.distance(secondPoint);
    }
}
    private class RiderState {

      public enum Heading {
        NONE,
        MOVING_EAST,
        MOVING_WEST,
        MOVING_NORTH,
        MOVING_SOUTH
      }

      public Heading current;
      public Heading pending;
      public long changedAt;
      public Location changedAtLoc;
      public Intersection nearestIntersection;

      public void setNearest(Intersection i) {
        this.nearestIntersection = i;
      }
      public void reset() {
        current = Heading.NONE;
        pending = Heading.NONE;
        changedAt = 0;
        nearestIntersection = null;
      }
      public RiderState() {
        this.reset();
      }

      public void updateHeading(Heading heading, Location l) {
        if (pending != heading) {
          changedAt = System.currentTimeMillis();
          Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "DETECTED TURN TO " + heading.toString() + " FROM " + pending.toString());
          pending = heading;
          changedAtLoc = l;
          List<Intersection> nearby = vpTree.getNearestNeighbors(l, 1);
          if (nearby != null && nearby.size() > 0 && nearby.get(0).distance(l) < 10) {
            if (nearestIntersection != null && !nearestIntersection.equals(nearby.get(0))) {
              makeConnection(nearestIntersection, nearby.get(0));
            }
            nearestIntersection = nearby.get(0);
          }
        }
      }

      public boolean shouldApply() {
        return current != pending && System.currentTimeMillis() - changedAt > 1000;
      }

      public Location apply() {
        current = pending;
        return changedAtLoc;
      }
    public static Heading velocityToRiderState(Vector v) {
      if (v.lengthSquared() <= 0.1) return Heading.NONE;
      if (Math.abs(v.getX()) > Math.abs(v.getZ())) {
        if (v.getX() > 0) {

          return Heading.MOVING_EAST;
        }
        return Heading.MOVING_WEST;
      }
      else {
        if (v.getZ() > 0) {
          return Heading.MOVING_SOUTH;
        }
        return Heading.MOVING_NORTH;
      }
    }

    }


    Map<UUID, Map<UUID, String>> connections = new HashMap<>();

    private void makeConnection(Intersection from, Intersection to) {
      // Deterministic ordering
      if (from.uuid.compareTo(to.uuid) < 0) {
        Intersection temp = from;
        from = to;
        to = temp;
      }
      Map<UUID, String> connectedFrom = connections.get(from.uuid);
      if (connectedFrom == null) {
        connectedFrom = new HashMap<UUID, String>();
        connections.put(from.uuid, connectedFrom);
      }
      if (!connectedFrom.containsKey(to.uuid)) {
        connectedFrom.put(to.uuid, "default");
        MarkerSet mSet = plugin.getDynmapAPI().getMarkerAPI().getMarkerSet("railswitch2");
        mSet.createPolyLineMarker(from.uuid.toString() + to.uuid.toString(), from.uuid + " -> " + to.uuid, false, from.getWorld().getName(), new double[]{from.getX(), to.getX()}, new double[]{from.getY(), to.getY()}, new double[]{from.getZ(), to.getZ()}, true);
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "NEW CONN FROM " + from.uuid.toString() + " to " + to.uuid.toString());
      }
      else {
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "conn already exists");
      }
    }
    private RailSwitchPlugin plugin;

    public PlayerListener(RailSwitchPlugin pl) {
        this.plugin = pl;
    }

    Map<UUID, RiderState> riderStates = new HashMap<>();

    private class Intersection extends Location {
      public UUID uuid = UUID.randomUUID(); 
      public Intersection() {
        super(null, 0, 0, 0);
      }

      public Intersection(Location l, Location snap) {
        super(l.getWorld(), 
            (snap != null && Math.abs(snap.getX() - l.getX()) <= 5) ? snap.getX() : l.getX(), 
            (snap != null && Math.abs(snap.getY() - l.getY()) <= 5) ? snap.getY() : l.getY(), 
            (snap != null && Math.abs(snap.getZ() - l.getZ()) <= 5) ? snap.getZ() : l.getZ()
            );
      }
    };
private VPTree<Location, Intersection> vpTree =
        new VPTree<Location, Intersection>(new LocationDistFunc());
    private Intersection addIntersection(RiderState state, Location loc) {
      List<Intersection> nearby = vpTree.getNearestNeighbors(loc, 1);
      if (nearby == null || nearby.size() == 0 || nearby.get(0).distance(loc) > 10) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "DETECTED NEW INTERSECTION AT: " + loc.toString());
        Intersection i = new Intersection(loc, state.nearestIntersection);
        vpTree.add(i);
        MarkerSet mSet = plugin.getDynmapAPI().getMarkerAPI().getMarkerSet("railswitch");
        mSet.createMarker(i.uuid.toString(), i.uuid.toString(), i.getWorld().getName(), i.getX(), i.getY(), i.getZ(), mSet.getDefaultMarkerIcon(), true);
        if (state.nearestIntersection != null) {
          makeConnection(state.nearestIntersection, i);
        }
        return i;
      } else {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "DETECTED EXISTING INTERSECTION AT: " + nearby.get(0).toString());
      }
      return null;
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
      if (!(e.getExited() instanceof Player)) return;
      Player player = (Player)e.getExited();
      RiderState state = riderStates.get(player.getUniqueId());
      if(state == null) return;
      state.reset();
    }
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
      if (!(e.getEntered() instanceof Player)) return;
      Player player = (Player)e.getEntered();
      RiderState state = riderStates.get(player.getUniqueId());
      if(state == null) return;
      state.reset();
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {
      List<Entity> passengers = e.getVehicle().getPassengers();
      if (!(e.getVehicle() instanceof Minecart)) return;
      if (passengers.size() == 0) return;
      Entity passenger = passengers.get(0);
      if (!(passenger instanceof Player)) return;
      Player player = (Player)passenger;
      RiderState state = riderStates.get(player.getUniqueId());
      if (state == null) {
        state = new RiderState();
        riderStates.put(player.getUniqueId(), state);
      }
      RiderState.Heading fromVel = RiderState.velocityToRiderState(e.getVehicle().getVelocity());

      // Short circuit if we're going straight
      if (fromVel == state.current && fromVel == state.pending) return;

      state.updateHeading(fromVel, e.getVehicle().getLocation().subtract(e.getVehicle().getVelocity().multiply(3) ));

      // Debounce turns
      if (state.shouldApply()) {
        boolean isIntersection = state.current != RiderState.Heading.NONE && state.pending != RiderState.Heading.NONE;
        Location appliedAt = state.apply();
        if (isIntersection) {
          // This is probably an intersection!
          Intersection i = addIntersection(state, appliedAt);
          state.setNearest(i);
        }
      }
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

        builder = Component.text()
                .append(Component.text("Building Guide\n----------------\n\n"));
        builder.append(Component.text("Modifiers:\n"));
        builder.append(Component.text("- ")
            .append(Component.text("Gold", NamedTextColor.GOLD))
            .append(Component.text(": Speed boost\n")));
        builder.append(Component.text("- ")
            .append(Component.text("Copper", NamedTextColor.GREEN))
            .append(Component.text(": Slowdown\n")));
        builder.append(Component.text("- ")
            .append(Component.text("Lapis", NamedTextColor.BLUE))
            .append(Component.text(": Minor boost\n")));
        builder.append(Component.text("- ")
            .append(Component.text("Netherite", NamedTextColor.DARK_GRAY))
            .append(Component.text(": boost+fly\n")));
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
