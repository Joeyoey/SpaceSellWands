package com.joeyoey.sellwands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.joeyoey.sellwands.commands.SellWandsCommand;
import com.joeyoey.sellwands.events.BlockInteractEvent;
import com.joeyoey.sellwands.object.Wand;

import net.milkbowl.vault.economy.Economy;

public class SellWands extends JavaPlugin {

	public HashMap<String, Double> prices = new HashMap<String, Double>();
	public HashMap<String, Integer> playerscooldown = new HashMap<String, Integer>();
	public Economy economy = null;
	private HashMap<String, String> sellwandNames = new HashMap<String, String>();
	private HashMap<String, String> sellwandKeys = new HashMap<String, String>();
	private List<Wand> wands = new ArrayList<Wand>();
	public boolean shopguiplus;

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = (Economy) economyProvider.getProvider();
		}
		return economy != null;
	}

	public void onEnable() {
		setupEconomy();
		saveDefaultConfig();
		shopguiplus = getConfig().getBoolean("shopguiplus");
		if (!shopguiplus) {
			loadPrices();
		}
		cooldown();
		Bukkit.getPluginManager().registerEvents(new BlockInteractEvent(this), this);
		getServer().getPluginCommand("sellwands").setExecutor(new SellWandsCommand(this));
		getCommand("sellwands").setTabCompleter(new SellWandsCommand(this));
		loadWands();
	}

	public void onDisable() {
		getServer().getConsoleSender().sendMessage("[SuperSellWands] Clearing maps!");
		prices.clear();
		playerscooldown.clear();
		sellwandNames.clear();
		sellwandKeys.clear();
		getServer().getConsoleSender().sendMessage("[SuperSellWands] Maps cleared successfully!");
	}

	
	public void loadWands() {
		wands.clear();
		getConfig().getConfigurationSection("item").getKeys(false).forEach(s -> {
			String name = getConfig().getString("item." + s + ".name");
			List<String> lore = getConfig().getStringList("item." + s + ".lore");
			Material mat = Material.getMaterial(getConfig().getString("item." + s + ".type"));
			boolean glow = getConfig().getBoolean("item." + s + ".enchanted");
			int uses = getConfig().getInt("item." + s + ".uses");
			double priceMod = getConfig().getDouble("item." + s + ".price-multiplier");
			int cooldown = getConfig().getInt("item." + s + ".cooldown");
			
			wands.add(new Wand(name, lore, mat, uses, priceMod, cooldown, glow));
			
			getServer().getConsoleSender().sendMessage("[SpaceSellWands] " + s + " has been loaded.");
		});
	}
	
	public HashMap<String, String> getSellwandNames() {
		return sellwandNames;
	}

	public HashMap<String, String> getSellwandKeys() {
		return sellwandKeys;
	}

	public List<Wand> getWands() {
		return wands;
	}
	
	public void loadPrices() {
		File essentials = new File("plugins/Essentials/worth.yml");
		File config = new File("plugins/SpaceSellWands/config.yml");
		if (essentials.exists()) {
			getLogger().log(Level.INFO,
					"Essentials has been found. If you wish to use this enable it in your config.yml by setting essentials-price to TRUE.");
			if (getConfig().getBoolean("use-essentials-worth-yml")) {
				Bukkit.getLogger().log(Level.INFO,
						"This plugin is configured to use Essentials worth.yml");
			}
		} else {
			getLogger().log(Level.WARNING,
					"Essentials worth.yml has not been located. In your config.yml make sure essentials-price is set to FALSE.");
		}
		if (getConfig().getBoolean("use-essentials-worth-yml")) {
			parseConfigFile(essentials, "worth");
		} else {
			parseConfigFile(config, "prices");
		}
	}

	public void cooldown() {
		BukkitScheduler cooldown = getServer().getScheduler();
		cooldown.scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (SellWands.this.playerscooldown.containsKey(player.getName())) {
						int cooldown = ((Integer) SellWands.this.playerscooldown.get(player.getName())).intValue();
						cooldown--;
						if (cooldown <= 0) {
							SellWands.this.playerscooldown.remove(player.getName());
						} else {
							SellWands.this.playerscooldown.put(player.getName(), Integer.valueOf(cooldown));
						}
					}
				}
			}
		}, 0L, 20L);
	}

	public void parseConfigFile(File file, String rootKey) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		getLogger().log(Level.SEVERE, rootKey + " the key being used.");
		for (String s : config.getConfigurationSection(rootKey).getKeys(false)) {
			getLogger().log(Level.SEVERE, s + " the key being used.");

			if (config.isConfigurationSection(rootKey + "." + s)) {
				for (String itemdata : config.getConfigurationSection(rootKey + "." + s).getKeys(false)) {
					String itemReformat = s;
					Double price = Double.valueOf(config.getDouble(rootKey + "." + s + "." + itemdata));

					boolean isItemId = false;

					try {
						Integer.parseInt(itemReformat);
						isItemId = true;
					} catch (NumberFormatException localNumberFormatException) {
					}

					if (isItemId) {

						try {
							getLogger().log(Level.SEVERE, "Number based items are no longer supported, please use the Material name found online.");
						} catch (NullPointerException localNullPointerException) {
						}

					}

					itemReformat = itemReformat.replace("_", "").toLowerCase();

					this.prices.put(itemReformat + ":" + itemdata, price);
				}
			} else {
				String itemReformat = s;
				Double price = Double.valueOf(config.getDouble(rootKey + "." + s));

				boolean isItemId = false;

				try {
					Integer.parseInt(itemReformat);
					isItemId = true;
				} catch (NumberFormatException localNumberFormatException1) {
				}

				if (isItemId) {

					try {
						getLogger().log(Level.SEVERE, "Number based items are no longer supported, please use the Material name found online.");
					} catch (NullPointerException localNullPointerException1) {
					}

				}
				itemReformat = itemReformat.replace("_", "").toLowerCase();
				this.prices.put(itemReformat, price);
			}
		}
		System.out.println("[SpaceSellWands] A total of " + this.prices.size() + " items have been registered.");
	}
}