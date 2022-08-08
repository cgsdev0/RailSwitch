package sh.okx.railswitch;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import sh.okx.railswitch.switches.SwitchListener;
import com.google.common.base.CharMatcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Rail Switch plugin class
 */
public final class RailSwitchPlugin extends JavaPlugin implements Listener {

    private HashMap<UUID, String> playerDestinations;
    private Map<String, Object> allDestinations;
    private HashMap<Location, String> allBanners;
    private File customConfigFile;
    private FileConfiguration customConfig;
    private DynmapCommonAPI dynmap;


    public DynmapCommonAPI getDynmapAPI() {
      return dynmap;
    }

    public void setDestination(Player p, String dest) {
        playerDestinations.put(p.getUniqueId(), dest);
    }

    public void addDestination(String dest) {
        allDestinations.put(dest, new Location(null, 0, 0, 0));
        writeAllDestinations();
    }

    public void updateDestination(String dest, Location loc) {
        if (loc == null) {
          allBanners.remove(allDestinations.get(dest));
          allDestinations.put(dest, new Location(null, 0,0,0));
        }
        else {
          allBanners.put(loc, dest);
          allDestinations.put(dest, loc);
        }
        writeAllDestinations();
    }

    public void removeBanner(Location loc) {
        if (!allBanners.containsKey(loc)) {
          return;
        }
        updateDestination(allBanners.get(loc), null);
    }

    public boolean hasDestination(String dest) {
        return allDestinations.containsKey(dest);
    }

    public void removeDestination(String dest) {
        Object loc = allDestinations.get(dest);
        if (loc != null && loc instanceof Location) {
          allBanners.remove(loc);
        }
        allDestinations.remove(dest);
        writeAllDestinations();
    }

    private void writeAllDestinations() {
        customConfig.createSection("destinations", allDestinations);
        try {
            customConfig.save(customConfigFile);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> listNearbyDestinations(Location origin, double dist) {
      return allDestinations.keySet().stream().filter(k -> {
        Object o = allDestinations.get(k);
        if(!(o instanceof Location)) return false;
        Location l = (Location)o;
        if (l.getWorld() == null) return false;
        if (!l.getWorld().equals(origin.getWorld())) return false;
        if (l.distance(origin) > dist) return false;
        return true;
      }).toList();
    }
    public List<String> listAllDestinations() {
        return allDestinations.keySet().stream().sorted().toList();
    }

    public String getDestination(Player p) {
        return playerDestinations.get(p.getUniqueId());
    }

    public boolean isValidDestination(String message) {
        // Each destination must be at most 15 characters.
        for (String dest : message.split(" ")) {
            if (dest.length() > 15) {
                return false;
            }
        }

        return CharMatcher.inRange('0', '9')
                .or(CharMatcher.inRange('a', 'z'))
                .or(CharMatcher.inRange('A', 'Z'))
                .or(CharMatcher.is('_')).matchesAllOf(message);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        playerDestinations = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(new SwitchListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getCommand("dest").setExecutor(new SetDestinationCommand(this));
        this.getCommand("dest").setTabCompleter(new DestTabCompleter(this));
        this.getCommand("destadd").setExecutor(new AddDestinationCommand(this));
        this.getCommand("destdel").setExecutor(new RemoveDestinationCommand(this));
        this.getCommand("destdel").setTabCompleter(new DestTabCompleter(this));
        this.getCommand("destbanner").setExecutor(new DestinationBannerCommand(this));
        this.getCommand("destbanner").setTabCompleter(new DestTabCompleter(this));
        this.createCustomConfig();
        Bukkit.addRecipe(new CraftableRailBook().getRecipe());
        this.getLogger().info("RailSwitch is now enabled!");
        PluginManager pm = getServer().getPluginManager();
        /* Get dynmap */
        Plugin dynmapPl = pm.getPlugin("dynmap");
        if (dynmapPl == null) {
          return;
        }
        dynmap = (DynmapCommonAPI) dynmapPl; /* Get API */
        dynmap.getMarkerAPI().createMarkerSet("railswitch", "Rail Debug", null, true);
        MarkerSet mSet = dynmap.getMarkerAPI().getMarkerSet("railswitch");
        mSet.setHideByDefault(true);
        for(Marker m : mSet.getMarkers()) {
          m.deleteMarker();
        }
        dynmap.getMarkerAPI().createMarkerSet("railswitch2", "Rail Lines", null, true);
        MarkerSet mSet2 = dynmap.getMarkerAPI().getMarkerSet("railswitch2");
        for(PolyLineMarker m : mSet2.getPolyLineMarkers()) {
          m.deleteMarker();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.removeRecipe(CraftableRailBook.craftingKey);
        this.getLogger().info("RailSwitch is now disabled.");
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "data.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("data.yml", false);
        }

        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
        ConfigurationSection section = customConfig.getConfigurationSection("destinations");
        if (section == null) {
          allDestinations = new HashMap<String, Object>();
        }
        else {
          allDestinations = section.getValues(false);
        }
        allBanners = new HashMap<Location, String>();
        // Re-hydrate banner map
        for(String key : allDestinations.keySet()) {
          Object o = allDestinations.get(key);
          if (o != null && o instanceof Location && ((Location)o).getWorld() != null) {
            allBanners.put((Location)o, key);
          }
        }
    }
}

