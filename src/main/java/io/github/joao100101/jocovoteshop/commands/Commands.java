package io.github.joao100101.jocovoteshop.commands;

import io.github.joao100101.jocovoteshop.JocoVoteShop;
import io.github.joao100101.jocovoteshop.sqlite.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    private final SQLite sqLite = new SQLite();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if ((cmd.getName().equalsIgnoreCase("voteshop") || cmd.getName().equalsIgnoreCase("vs")) && isAuthorized(sender)) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    JocoVoteShop.getVoteShopMenu().openMenu(p);
                }
            } else if (args.length == 1) {
                if (!args[0].equalsIgnoreCase("admin")) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                    if (sqLite.existInTable(target.getUniqueId().toString())) {
                        sender.sendMessage("");
                        sender.sendMessage("§a" + target.getName() + "'s");
                        sender.sendMessage("§aVotes: §f" + sqLite.getTotalVotes(target));
                        sender.sendMessage("");
                    } else {
                        sender.sendMessage("§cEsse jogador não está cadastrado.");
                        return true;
                    }
                } else {
                    sendVrsHelp(sender);
                    return true;
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("admin")) {
                    adminCommands(sender, args);
                }
            } else {
                sendVrsHelp(sender);
                return true;
            }
        }
        return false;
    }


    private boolean isAuthorized(CommandSender sender) {
        if (!sender.hasPermission("jocovoteshop.admin") || !sender.isOp()) {
            sender.sendMessage("§cVocê não está autorizado a fazer isso.");
            return false;
        }
        return true;
    }

    private void adminCommands(CommandSender sender, String[] args) {
        if (args[1].equalsIgnoreCase("addvotes")) {
            Player target = Bukkit.getPlayer(args[2]);
            int quantidade = Integer.parseInt(args[3]);
            sqLite.addVotes(target, quantidade);
            sender.sendMessage("§aAdicionado §f" + quantidade + "§a votes para §f" + target.getName());
        } else if (args[1].equalsIgnoreCase("removevotes")) {
            Player target = Bukkit.getPlayer(args[2]);
            int quantidade = Integer.parseInt(args[3]);
            sqLite.removeVotes(target, quantidade);
            sender.sendMessage("§aRemovido §f" + quantidade + "§a votes de §f" + target.getName());
        } else if (args[1].equalsIgnoreCase("setvotes")) {
            Player target = Bukkit.getPlayer(args[2]);
            int quantidade = Integer.parseInt(args[3]);
            sqLite.setVotes(target.getUniqueId().toString(), quantidade);
            sender.sendMessage("§aVotes de §f" + target.getName() + "§a setados para §f" + quantidade);
        } else {
            sendVrsHelp(sender);
        }
    }

    private void sendVrsHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§a■ §7/vs [player] §8- §7Ver a quantidade de votos de um player.");
        sender.sendMessage("§a■ §7/vs admin addvotes [player] [quantidade] §8- §7Adiciona votos a um player");
        sender.sendMessage("§a■ §7/vs admin removevotes [player] [quantidade] §8- §7Remove votes de um player");
        sender.sendMessage("§a■ §7/vs admin setvotes [player] [quantidade] §8- §7Seta a quantidade de votes de um player");
        sender.sendMessage("");
    }
}
