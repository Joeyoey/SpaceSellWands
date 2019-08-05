package com.joeyoey.sellwands.events;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.joeyoey.sellwands.SellWands;
import com.joeyoey.sellwands.object.Wand;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class BlockInteractEvent implements Listener {

	SellWands plugin;
	String key;
	int uses;
	boolean infinite;

	public BlockInteractEvent(SellWands instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			if (player.getItemInHand().getType() != null) {
				if (!player.getItemInHand().hasItemMeta() || !player.getItemInHand().getItemMeta().hasLore()
						|| !player.getItemInHand().getItemMeta().hasDisplayName()) {
					return;
				}
				ItemStack mainHand = player.getItemInHand();
				String name = mainHand.getItemMeta().getDisplayName();
				List<String> lore = mainHand.getItemMeta().getLore();
				Material mat = mainHand.getType();
				boolean glow = !mainHand.getEnchantments().isEmpty();

				Wand wand = new Wand(name, lore, mat, 0, 0, 0, glow);
				for (Wand s : plugin.getWands()) {
					if (s.equals(wand)) {

						// key = plugin.getSellwandKeys().get(s);
						// List<String> loreTest = plugin.getConfig().getStringList("item." + key +
						// ".lore");
						// loreTest.add(plugin.getConfig().getString("item." + key + ".uses"));
						// if (loreTest.size() ==
						// player.getInventory().getItemInMainHand().getItemMeta().getLore().size()
						// || loreTest.size() - 1 ==
						// player.getInventory().getItemInMainHand().getItemMeta()
						// .getLore().size()
						// || loreTest.size() + 2 ==
						// player.getInventory().getItemInMainHand().getItemMeta()
						// .getLore().size()
						// || loreTest.size() + 1 ==
						// player.getInventory().getItemInMainHand().getItemMeta()
						// .getLore().size()) {
						//
						List<String> playerLore = player.getItemInHand().getItemMeta().getLore();

						String usesLore = plugin.getConfig().getString("messages.uses-lore");
						String[] usesSplit = usesLore.split(" ");
						int oof = 0;
						for (int i = 0; i < usesSplit.length; i++) {
							if (usesSplit[i].contains("%uses%")) {
								oof = i;
								break;
							}
						}
						// for (String v : playerLore) {
						// v = ChatColor.stripColor(v);
						// if (v.startsWith("Uses:")) {
						// v = v.replace("Uses: ", "");
						// uses = Integer.parseInt(v);
						// break;
						// } else {
						// uses = 1000000;
						// }
						// }

						try {
							String useLore = playerLore.get(playerLore.size() - 1);
							String[] pLoreUse = useLore.split(" ");
							wand.setUses(Integer.parseInt(ChatColor.stripColor(pLoreUse[oof])));
						} catch (Exception e) {
							wand.setUses(1000000);
						}

						Double priceMultiplier = s.getPriceMulti();// plugin.getConfig().getDouble("item." + key +
																	// ".price-multiplier");
						event.setCancelled(true);
						Block block = event.getClickedBlock();
						if ((block.getType().equals(Material.CHEST))
								|| (block.getType().equals(Material.TRAPPED_CHEST))) {

							org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
							Inventory inventory = chest.getInventory();
							if (!player.hasPermission("sellwands.sell")) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&',
										plugin.getConfig().getString("messages.no-permission")));
								return;
							}
							if (plugin.playerscooldown.containsKey(player.getName())) {
								player.sendMessage(ChatColor
										.translateAlternateColorCodes('&',
												plugin.getConfig().getString("messages.cooldown"))
										.replaceAll("%seconds%",
												plugin.playerscooldown.get(player.getName()).toString()));
								return;
							}
							int slot = 0;
							double totalSale = 0.0D;
							NumberFormat numf = NumberFormat.getInstance();
							numf.setGroupingUsed(true);
							HashMap<Material, Integer> iAmounts = new HashMap<Material, Integer>();
							HashMap<Material, Double> iPrices = new HashMap<Material, Double>();
							String type;
							for (ItemStack ischest : inventory) {
								if ((ischest == null) || (ischest.getType().equals(Material.AIR))
										|| (ischest.getType() == null)) {
									slot++;
								} else {
									type = ischest.getType().toString().toLowerCase();
									type = type.replace("_", "");
									String dataCode = String.valueOf(ischest.getData().getData());
									if (plugin.prices.containsKey(type) && plugin.prices.get(type) > 0) {
										chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

										totalSale = totalSale + ((Double) plugin.prices.get(type)).doubleValue()
												* ischest.getAmount() * priceMultiplier;
										if (iPrices.containsKey(ischest.getType())) {
											iPrices.put(ischest.getType(),
													Double.valueOf(
															((Double) iPrices.get(ischest.getType())).doubleValue()
																	+ ((Double) plugin.prices.get(type)).doubleValue()
																			* ischest.getAmount() * priceMultiplier));
										} else {
											iPrices.put(ischest.getType(),
													Double.valueOf(((Double) plugin.prices.get(type)).doubleValue()
															* ischest.getAmount() * priceMultiplier));
										}
										if (iAmounts.containsKey(ischest.getType())) {
											iAmounts.put(ischest.getType(),
													Integer.valueOf((iAmounts.get(ischest.getType())).intValue()
															+ ischest.getAmount()));
										} else {
											iAmounts.put(ischest.getType(), Integer.valueOf(ischest.getAmount()));
										}
									} else if (plugin.prices.containsKey(type + ":" + dataCode)
											&& plugin.prices.get(type + ":" + dataCode) > 0) {
										chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

										totalSale = totalSale
												+ ((Double) plugin.prices.get(type + ":" + dataCode)).doubleValue()
														* ischest.getAmount() * priceMultiplier;
										if (iPrices.containsKey(ischest.getType())) {
											iPrices.put(ischest.getType(), Double
													.valueOf(((Double) iPrices.get(ischest.getType())).doubleValue()
															+ ((Double) plugin.prices.get(type + ":" + dataCode))
																	.doubleValue() * ischest.getAmount()
																	* priceMultiplier));
										} else {
											iPrices.put(ischest.getType(), Double.valueOf(
													((Double) plugin.prices.get(type + ":" + dataCode)).doubleValue()
															* ischest.getAmount() * priceMultiplier));
										}
										if (iAmounts.containsKey(ischest.getType())) {
											iAmounts.put(ischest.getType(),
													Integer.valueOf(iAmounts.get(ischest.getType())).intValue()
															+ ischest.getAmount());
										} else {
											iAmounts.put(ischest.getType(), Integer.valueOf(ischest.getAmount()));
										
										}
									} else if (plugin.shopguiplus) {
										if (ShopGuiPlusApi.getItemStackPriceSell(player, ischest) > 0) {
											chest.getInventory().setItem(slot, new ItemStack(Material.AIR));

											totalSale = totalSale
													+ ShopGuiPlusApi.getItemStackPriceSell(player, ischest)
															* priceMultiplier;
											if (iPrices.containsKey(ischest.getType())) {
												iPrices.put(ischest.getType(), Double
														.valueOf(((Double) iPrices.get(ischest.getType())).doubleValue()
																+ ShopGuiPlusApi.getItemStackPriceSell(player, ischest)
																		* priceMultiplier));
											} else {
												iPrices.put(ischest.getType(),
														Double.valueOf(
																ShopGuiPlusApi.getItemStackPriceSell(player, ischest)
																		* priceMultiplier));
											}
											if (iAmounts.containsKey(ischest.getType())) {
												iAmounts.put(ischest.getType(),
														Integer.valueOf(iAmounts.get(ischest.getType())).intValue()
																+ ischest.getAmount());
											} else {
												iAmounts.put(ischest.getType(), Integer.valueOf(ischest.getAmount()));
											}
										}

									}
									slot++;
								}
							}
							if (iAmounts.isEmpty()) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&',
										plugin.getConfig().getString("messages.chest-empty")));
								break;
							}
							EconomyResponse er = plugin.economy.depositPlayer(player, totalSale);
							if (er.transactionSuccess()) {
								player.sendMessage(ChatColor
										.translateAlternateColorCodes('&',
												plugin.getConfig().getString("messages.sale"))
										.replace("%amount%", numf.format(totalSale)));
								if (plugin.getConfig().getBoolean("breakdown")) {
									for (Iterator<Entry<Material, Integer>> itr = iAmounts.entrySet().iterator(); itr
											.hasNext();) {
										Entry<Material, Integer> entry = itr.next();
										Material material = entry.getKey();
										Integer value = entry.getValue();
										player.sendMessage(ChatColor
												.translateAlternateColorCodes('&',
														plugin.getConfig().getString("messages.breakdown"))
												.replaceAll("%amount%", value.toString())
												.replace("%item%",
														StringUtils.capitalize(
																material.name().toLowerCase().replace("_", " ")))
												.replace("%price%", numf.format(iPrices.get(material))));
									}
								}

								if (wand.getUses() != 1000000) {

									if (wand.getUses() > 0) {
										// uses--;

										wand.setUses(wand.getUses() - 1);

										if (wand.getUses() <= 0) {
											player.setItemInHand(new ItemStack(Material.AIR));
											player.sendMessage(ChatColor
													.translateAlternateColorCodes('&',
															plugin.getConfig().getString("messages.item-break"))
													.replace("%uses%", Integer.toString(wand.getUses())));
										} else {
											ItemStack toReplace = new ItemStack(
													s.getMat() /*
																 * Material.getMaterial(
																 * plugin.getConfig().getString("item." + key +
																 * ".type"))
																 */);
											ItemMeta toReplacem = toReplace.getItemMeta();
											List<String> toReplacel = new ArrayList<String>();
											for (String v : s.getLore()/*
																		 * plugin.getConfig().getStringList("item." +
																		 * key + ".lore")
																		 */) {
												toReplacel.add(ChatColor.translateAlternateColorCodes('&', v));
											}
											if (s.getGlow() /*
															 * plugin.getConfig().getBoolean("item." + key +
															 * ".enchanted")
															 */) {
												toReplacem.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
												toReplacem.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
												toReplacem.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
											}
											toReplacel.add(ChatColor
													.translateAlternateColorCodes('&',
															plugin.getConfig().getString("messages.uses-lore"))
													.replaceAll("%uses%", wand.getUses() + ""));
											toReplacem.setLore(toReplacel);
											toReplacem.setDisplayName(
													ChatColor.translateAlternateColorCodes('&', s.getName()));
											toReplace.setAmount(player.getItemInHand().getAmount());
											toReplace.setItemMeta(toReplacem);
											player.setItemInHand(toReplace);
											player.sendMessage(ChatColor
													.translateAlternateColorCodes('&',
															plugin.getConfig().getString("messages.uses"))
													.replace("%uses%",
															String.format("%,d", new Object[] { wand.getUses() })));
										}
									}
								}
							} else {
								System.out
										.println("[SuperSellWands] Transaction has failed for Inventory Sale (player: "
												+ player.getName() + " amount: " + totalSale + ")");
							}
							if (s.getCooldown()/* plugin.getConfig().getInt("item." + key + ".cooldown") */ > 0) {
								plugin.playerscooldown.put(player.getName(), s.getCooldown()
								/* Integer.valueOf(plugin.getConfig().getInt("item." + key + ".cooldown")) */);
								break;
							}
							break;
						}

					}
				}
			}

		}

	}

}
