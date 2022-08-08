package sh.okx.railswitch;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DestinationBannerCommand implements CommandExecutor {
    private final RailSwitchPlugin plugin;

    public DestinationBannerCommand(RailSwitchPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }


        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /destbanner [destination_name]");
            return true;
        }

        if (!plugin.isValidDestination(args[0])) {
            player.sendMessage(ChatColor.RED + "Destinations can each not be more than 15 characters and may only use alphanumerical characters and underscores.");
            return true;
        }

        if (!plugin.hasDestination(args[0])) {
            player.sendMessage(ChatColor.RED + "Destination does not exist.");
            return true;
        }

        List<Block> blocks = player.getLastTwoTargetBlocks(null, 6);
        if(blocks.size() == 0) {
            player.sendMessage(ChatColor.RED + "Look at the top of the banner and re-run this command.");
            return true;
        }
        if (!Tag.BANNERS.isTagged(blocks.get(blocks.size() - 1).getType() )) {
            player.sendMessage(ChatColor.RED + "Look at the top of the banner and re-run this command.");
            return true;
        }
        plugin.updateDestination(args[0], blocks.get(blocks.size() - 1).getLocation());

        player.sendMessage(ChatColor.GREEN + "Destination '"+ ChatColor.YELLOW + args[0] + ChatColor.GREEN +"' successfully linked to that banner.");
        return true;
    }
}

