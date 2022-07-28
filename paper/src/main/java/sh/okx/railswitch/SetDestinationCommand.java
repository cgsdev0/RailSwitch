package sh.okx.railswitch;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetDestinationCommand implements CommandExecutor {
    private final RailSwitchPlugin plugin;

    public SetDestinationCommand(RailSwitchPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }


        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(ChatColor.GREEN + "Unset rail destination (use /" + label + " <destination> to set your destination).");
            plugin.setDestination(player, "");
            return true;
        }

        String dest = String.join(" ", args);
        if (!plugin.isValidDestination(dest)) {
            player.sendMessage(ChatColor.RED + "Destinations can each not be more than 40 characters and may only use alphanumerical characters, ASCII symbols, and spaces.");
            return true;
        }

        plugin.setDestination(player, dest);

        player.sendMessage(ChatColor.GREEN + "Set your rail destination to: " + dest);
        return true;
    }
}

