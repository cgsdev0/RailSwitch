package sh.okx.railswitch;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddDestinationCommand implements CommandExecutor {
    private final RailSwitchPlugin plugin;

    public AddDestinationCommand(RailSwitchPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }


        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /destadd [destination_name]");
            return true;
        }

        if (!plugin.isValidDestination(args[0])) {
            player.sendMessage(ChatColor.RED + "Destinations can each not be more than 15 characters and may only use alphanumerical characters and underscores.");
            return true;
        }

        if (plugin.hasDestination(args[0])) {
            player.sendMessage(ChatColor.RED + "Destination already exists.");
            return true;
        }
        plugin.addDestination(args[0]);

        player.sendMessage(ChatColor.GREEN + "Destination '"+ ChatColor.YELLOW + args[0] + ChatColor.GREEN +"' successfully added.");
        return true;
    }
}

