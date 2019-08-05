package com.joeyoey.sellwands.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.joeyoey.sellwands.SellWands;

import net.md_5.bungee.api.ChatColor;

public class SellWandsCommand implements CommandExecutor, TabCompleter {

	SellWands plugin;
	Player receiver;
	String key;

	public SellWandsCommand(SellWands instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sellwands")) {
			int length = args.length;
			if (length == 0 && (sender.hasPermission("sellwands.reload") || sender.hasPermission("sellwands.give"))) {
				sender.sendMessage(ChatColor.GREEN + "/sellwands give <player> <type> [uses]");
				sender.sendMessage(ChatColor.GREEN + "/sellwands reload");
			}
			if ((length == 1) && (sender.hasPermission("sellwands.reload"))) {
				plugin.reloadConfig();
				plugin.prices.clear();
				plugin.loadPrices();
				plugin.loadWands();
				sender.sendMessage(ChatColor.GREEN + "Reloaded SuperSellWands.");
			}
			if ((length >= 3) && (sender.hasPermission("sellwands.give"))) {
				if (args[0].equalsIgnoreCase("give")) {
					try {
						receiver = plugin.getServer().getPlayer(args[1]);
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "Not a valid player! Command /sellwands give %player% type");
						return false;
					}

					try {
						key = args[2];
						ItemStack is = new ItemStack(
								Material.getMaterial(plugin.getConfig().getString("item." + key + ".type")));
						ItemMeta ism = is.getItemMeta();
						List<String> isl = new ArrayList<String>();
						for (String s : plugin.getConfig().getStringList("item." + key + ".lore")) {
							isl.add(ChatColor.translateAlternateColorCodes('&', s));
						}
						if (plugin.getConfig().getBoolean("item." + key + ".enchanted")) {
							ism.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
							ism.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
							ism.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
						}
						int uses = 0;
						if (length > 3) {
							try {
								uses = Integer.parseInt(args[3]);
							} catch (NumberFormatException ex) {
								sender.sendMessage(ChatColor.RED + "That is not a number.");
							}
						} else {
							uses = plugin.getConfig().getInt("item." + key + ".uses");
						}
						if (uses > 0) {
							isl.add(ChatColor
									.translateAlternateColorCodes('&',
											plugin.getConfig().getString("messages.uses-lore"))
									.replace("%uses%", Integer.toString(uses)));
						}
						ism.setLore(isl);
						ism.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								plugin.getConfig().getString("item." + key + ".name")));
						is.setAmount(1);
						is.setItemMeta(ism);

						receiver.getInventory().addItem(is);
						sender.sendMessage(ChatColor
								.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.give"))
								.replace("%player%", receiver.getName()));
						receiver.sendMessage(ChatColor.translateAlternateColorCodes('&',
								plugin.getConfig().getString("messages.receive")));
					} catch (Exception e) {
						e.printStackTrace();
						sender.sendMessage(ChatColor.RED + "There isnt a sellwand with that name in your config.");
						sender.sendMessage(ChatColor.AQUA + "Some sellwands that you have:");
						plugin.getConfig().getConfigurationSection("item").getKeys(false).forEach(s -> {
							sender.sendMessage(s);
						});
					}
				} else {
					sender.sendMessage("Invalid argument, try 'give'");
				}
			}
			return true;
		}
		return false;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("sellwands")) {
			if (sender.hasPermission("sellwands.give") || sender.hasPermission("sellwands.reload")) {
				if (args.length == 1) {
					List<String> out = new ArrayList<String>();
					if (!args[0].equals("")) {
						if (args[0].toLowerCase().startsWith("r")) {
							out.add("reload");
						} else if (args[0].toLowerCase().startsWith("g")) {
							out.add("give");
						}
					} else {
						out.add("reload");
						out.add("give");
					}
					Collections.sort(out);
					
					return out;
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("give")) {
						List<String> out = new ArrayList<String>();
						
						if (!args[2].equals("")) {
							plugin.getConfig().getConfigurationSection("item").getKeys(false).forEach(s -> {
								if (s.toLowerCase().startsWith(args[2].toLowerCase())) {
									out.add(s);
								}
							});
						} else {
							plugin.getConfig().getConfigurationSection("item").getKeys(false).forEach(s -> {
								out.add(s);
							});
						}
						
						Collections.sort(out);
						
						return out;
					}
				}
			}
		}
		return null;
	}
	
	
	public static boolean getHideable() {

		try {
			String version = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
					.split(",")[3];
			if (version.equals("v1_8_R3")) {
				return true;
			}
			if (version.equals("v1_9_R2")) {
				return true;
			}
			if (version.equals("v1_9_R1")) {
				return true;
			}
			if (version.equals("v1_10_R1")) {
				return true;
			}
			if (version.equals("v1_11_R1")) {
				return true;
			}
		} catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
			return false;
		}
		return false;
	}
}
