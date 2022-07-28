package sh.okx.railswitch;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.railswitch.switches.SwitchListener;
import com.google.common.base.CharMatcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * The Rail Switch plugin class
 */
public final class RailSwitchPlugin extends JavaPlugin implements Listener {

    private HashMap<UUID, String> playerDestinations;
    private HashSet<String> allDestinations;
    private File customConfigFile;
    private FileConfiguration customConfig;


    public void setDestination(Player p, String dest) {
        playerDestinations.put(p.getUniqueId(), dest);
    }

    public void addDestination(String dest) {
        allDestinations.add(dest);
        writeAllDestinations();
    }

    public boolean hasDestination(String dest) {
        return allDestinations.contains(dest);
    }

    public void removeDestination(String dest) {
        allDestinations.remove(dest);
        writeAllDestinations();
    }

    private void writeAllDestinations() {
        customConfig.set("destinations", allDestinations.stream().toList());
        try {
            customConfig.save(customConfigFile);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> listAllDestinations() {
        return allDestinations.stream().sorted().toList();
    }

    public String getDestination(Player p) {
        return playerDestinations.get(p.getUniqueId());
    }

    public boolean isValidDestination(String message) {
        // Each destination must be fewer than 19 characters.
        for (String dest : message.split(" ")) {
            if (dest.length() > 19) {
                return false;
            }
        }

        return CharMatcher.inRange('0', '9')
                .or(CharMatcher.inRange('a', 'z'))
                .or(CharMatcher.inRange('A', 'Z'))
                .or(CharMatcher.anyOf("!\"#$%&'()*+,-./;:<=>?@[]\\^_`{|}~ ")).matchesAllOf(message);
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
        this.createCustomConfig();
        Bukkit.addRecipe(new CraftableRailBook().getRecipe());
        this.getLogger().info("RailSwitch is now enabled!");
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
        allDestinations = new HashSet<String>(customConfig.getStringList("destinations"));
    }
}

