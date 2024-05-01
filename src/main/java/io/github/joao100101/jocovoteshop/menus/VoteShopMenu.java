package io.github.joao100101.jocovoteshop.menus;

import io.github.joao100101.jocovoteshop.JocoVoteShop;
import io.github.joao100101.jocovoteshop.model.ShopItem;
import io.github.joao100101.jocovoteshop.sqlite.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VoteShopMenu implements Listener {

    private final Inventory inventory;
    private final List<ShopItem> shopItems = new ArrayList<>();
    private final SQLite sqLite = new SQLite();

    public VoteShopMenu() {
        loadShopItems();
        this.inventory = createInv();
    }

    private Inventory createInv() {
        int size = getConfig().getInt("Config.Menu.Linhas");
        if (size <= 0 || size > 6) {
            return Bukkit.createInventory(null, 9, "§cConfig Error");
        }
        return Bukkit.createInventory(null, 9 * size, "§aBrindes Vote");
    }

    private List<String> parseLore(Player p, ShopItem shopItem) {
        List<String> rawLore = shopItem.getLore();
        List<String> colorizedLore = new ArrayList<>();
        for (String line : rawLore) {
            colorizedLore.add(line.replace("&", "§").replace("@value", shopItem.getValue().toString()).replace("@player", p.getName()).replace("@votes", sqLite.getTotalVotes(p) + ""));
        }
        return colorizedLore;
    }

    private void enchantItemWithSerializedEnchantment(ItemMeta meta, String serializedEnchantment) {
        if (!serializedEnchantment.isEmpty()) {
            Enchantment enchantment = Enchantment.getByName(serializedEnchantment.split(":")[0]);
            int enchantmentLevel = Integer.parseInt(serializedEnchantment.split(":")[1]);
            if (enchantment != null) {
                meta.addEnchant(enchantment, enchantmentLevel, true);
            }
        }
    }

    private Integer getShopItemValue(Integer value) {
        if (value < 0) {
            return 0;
        }
        return value;
    }

    private void loadShopItems() {
        Set<String> configKeys = getConfig().getConfigurationSection("Config.Items").getKeys(false);
        String itemsPath = "Config.Items.";
        for (String slot : configKeys) {
            ShopItem si = new ShopItem();
            si.setSlot(Integer.parseInt(slot));
            si.setName(getConfig().getString(itemsPath + slot + ".name").replace("&", "§"));
            si.setPurchasable(getConfig().getBoolean(itemsPath + slot + ".purchasable"));
            si.setCommand(getConfig().getBoolean(itemsPath + slot + ".command"));
            si.setCommandLine(getConfig().getString(itemsPath + slot + ".command-line"));
            si.setValue(getShopItemValue(getConfig().getInt(itemsPath + slot + ".value")));
            si.setLore(getConfig().getStringList(itemsPath + slot + ".lore"));
            si.setItem(getConfig().getString(itemsPath + slot + ".item"));
            si.setEnchantments(getConfig().getStringList(itemsPath + slot + ".enchantments"));
            shopItems.add(si);
        }

    }


    private void loadItems(Player p) {
        for (ShopItem item : shopItems) {
            ItemStack itemStack = new ItemStack(Material.getMaterial(item.getItem()));
            ItemMeta meta = itemStack.getItemMeta();


            meta.setDisplayName(item.getName().replace("&", "§").replace("@player", p.getName()));
            for (String enchant : item.getEnchantments()) {
                enchantItemWithSerializedEnchantment(meta, enchant);
            }
            meta.setLore(parseLore(p, item));


            itemStack.setItemMeta(meta);


            if (item.getSlot() < inventory.getSize() && item.getSlot() >= 0) {
                inventory.setItem(item.getSlot(), itemStack);
            }
        }
    }

    public void openMenu(Player p) {
        p.openInventory(inventory);
        loadItems(p);
    }

    private FileConfiguration getConfig() {
        return JocoVoteShop.getInstance().getConfig();
    }

    private void giveItem(Player p, ItemStack itemStack) {
        int emptySlot = p.getInventory().firstEmpty();
        if (emptySlot == -1) {
            p.getLocation().getWorld().dropItem(p.getLocation(), itemStack);
        } else {
            p.getInventory().setItem(emptySlot, itemStack);
        }
    }


    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;

        e.setCancelled(true);

        ItemStack clickedItem = e.getCurrentItem();
        Player p = (Player) e.getWhoClicked();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;


        for (ShopItem item : shopItems) {
            if (item.getSlot() == e.getRawSlot()) {
                if (sqLite.getTotalVotes(p) >= item.getValue()) {
                    if (item.isPurchasable() && !(item.isCommand())) {
                        giveItem(p, clickedItem);
                        p.sendMessage("§aVocê reivindicou " + item.getName() + " §acom sucesso.");
                        sqLite.removeVotes(p, item.getValue());
                        p.closeInventory();
                    } else if (item.isCommand()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), item.getCommandLine().replace("@player", p.getName()));
                        p.sendMessage("§aVocê reivindicou " + item.getName() + " §acom sucesso.");
                        sqLite.removeVotes(p, item.getValue());
                        p.closeInventory();
                    }
                } else {
                    p.sendMessage("§cVocê não tem votos o suficiente para reivindicar isso.");
                }
            }
        }
    }

    @EventHandler
    public void dragItems(InventoryDragEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);
    }
}
