package sh.okx.railswitch;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.railswitch.switches.SwitchListener;

import java.util.HashMap;
import java.util.UUID;

/**
 * The Rail Switch plugin class
 */
public final class RailSwitchPlugin extends JavaPlugin implements Listener {

    private HashMap<UUID, String> destinations;

    public void setDestination(Player p, String dest) {
        destinations.put(p.getUniqueId(), dest);
    }

    public String getDestination(Player p) {
        return destinations.get(p.getUniqueId());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        destinations = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(new SwitchListener(this), this);
        this.getLogger().info("RailSwitch is now enabled!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.getLogger().info("RailSwitch is now disabled.");
    }

}
