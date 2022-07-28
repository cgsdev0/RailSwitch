package sh.okx.railswitch;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.railswitch.switches.SwitchListener;
import com.google.common.base.CharMatcher;

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
    public boolean isValidDestination(String message) {
        // Each destination must be fewer than 40 characters.
        for (String dest : message.split(" ")) {
            if (dest.length() > 40) {
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
        destinations = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(new SwitchListener(this), this);
        this.getCommand("dest").setExecutor(new SetDestinationCommand(this));
        this.getLogger().info("RailSwitch is now enabled!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.getLogger().info("RailSwitch is now disabled.");
    }

}
