package io.github.joao100101.jocovoteshop;

import io.github.joao100101.jocovoteshop.commands.Commands;
import io.github.joao100101.jocovoteshop.menus.VoteShopMenu;
import io.github.joao100101.jocovoteshop.sqlite.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class JocoVoteShop extends JavaPlugin {

    private final SQLite sqLite = new SQLite(getDataFolder().getAbsolutePath() + "/data.db");
    private static VoteShopMenu voteShopMenu;

    @Override
    public void onEnable() {
        voteShopMenu = new VoteShopMenu();
        Bukkit.getPluginManager().registerEvents(voteShopMenu, this);
        getCommand("voteshop").setExecutor(new Commands());
        getCommand("vs").setExecutor(new Commands());
        // Plugin startup logic
        saveDefaultConfig();
        sqLite.createTable();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static VoteShopMenu getVoteShopMenu() {
        return voteShopMenu;
    }

    public static JocoVoteShop getInstance() {
        return (JocoVoteShop) Bukkit.getPluginManager().getPlugin("JocoVoteShop");
    }
}
