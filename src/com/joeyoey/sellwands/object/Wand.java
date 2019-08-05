package com.joeyoey.sellwands.object;

import java.util.List;

import org.bukkit.Material;

import net.md_5.bungee.api.ChatColor;

public class Wand {

	private String name;
	private List<String> lore;
	private Material mat;
	private int uses;
	private double priceMulti;
	private int cooldown;
	private boolean glow;

	public Wand(String name, List<String> lore, Material mat, int uses, double priceMulti, int cooldown, boolean glow) {
		this.name = name;
		this.lore = lore;
		this.mat = mat;
		this.uses = uses;
		this.priceMulti = priceMulti;
		this.cooldown = cooldown;
		this.glow = glow;
	}

	public String getName() {
		return name;
	}

	public List<String> getLore() {
		return lore;
	}

	public Material getMat() {
		return mat;
	}

	public int getUses() {
		return uses;
	}
	
	public void setUses(int uses) {
		this.uses = uses;
	}

	public double getPriceMulti() {
		return priceMulti;
	}

	public int getCooldown() {
		return cooldown;
	}

	public boolean getGlow() {
		return glow;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this.getClass().equals(obj.getClass())) {
			Wand objTest = (Wand) obj;
			if (objTest.getMat().equals(this.getMat())) {
				if (ChatColor.translateAlternateColorCodes('&', this.getName()).equalsIgnoreCase(objTest.getName())) {
					if (this.getLore().size() == objTest.getLore().size() || this.getLore().size() + 1 == objTest.getLore().size()) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
