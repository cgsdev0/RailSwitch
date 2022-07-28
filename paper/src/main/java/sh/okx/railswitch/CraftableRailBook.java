package sh.okx.railswitch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CraftableRailBook {
    public static final NamespacedKey craftingKey = new NamespacedKey("railswitch", "railway_book_recipe");
    public static final NamespacedKey tagKey = new NamespacedKey("railswitch", "railway_book_tag");
    public ShapelessRecipe getRecipe() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(tagKey, PersistentDataType.INTEGER, 1);
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Railway Guidebook");
        item.setItemMeta(meta);
        ShapelessRecipe recipe = new ShapelessRecipe(craftingKey, item);
        recipe.addIngredient(new ItemStack(Material.BOOK));
        recipe.addIngredient(new ItemStack(Material.RAIL));
        return recipe;
    }
}
