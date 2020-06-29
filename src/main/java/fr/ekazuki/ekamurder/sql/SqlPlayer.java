package fr.ekazuki.ekamurder.sql;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.ekazuki.ekamurder.EkaMurder;
import fr.ekazuki.ekamurder.player.MurderRole;

public class SqlPlayer {
	
	public int death;
	public int detectiveWin;
	public int murderWin;
	public Player player;
	public ItemStack murderItem;
	public ItemStack detectiveItem;
	
	public SqlPlayer(Player player, int death, int detectiveWin, int murderWin, ItemStack murderItem, ItemStack detectiveItem) {
		this.player = player;
		this.death = death;
		this.detectiveWin = detectiveWin;
		this.murderWin = murderWin;
		this.murderItem = murderItem;
		this.detectiveItem = detectiveItem;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public int getStat(MurderStatType statType) {
		
		switch(statType) {
			case DEATH:
				return this.death;
			case DETECTIVE_WIN:
				return this.detectiveWin;
			case MURDER_WIN:
				return this.murderWin;
		}
		
		return 0;
	}
	
	public void addStat(MurderStatType statType) {
		switch(statType) {
			case DEATH:
				this.death++;
				break;
			case DETECTIVE_WIN:
				this.detectiveWin++;
				break;
			case MURDER_WIN:
				this.murderWin++;
				break;
		}
	}
	
	public ItemStack getItem(MurderRole role) {
		if(role.equals(MurderRole.DETECTIVE)) {
			return this.detectiveItem;
		} else if(role.equals(MurderRole.MURDER)) {
			return this.murderItem;
		}
		
		return null;
	}
	
	public void setItem(ItemStack item, MurderRole role) {
		if(role.equals(MurderRole.DETECTIVE)) {
			this.detectiveItem = item;
		} else if(role.equals(MurderRole.MURDER)) {
			this.murderItem = item;
		}
	}
	
	public void chooseGun() { 
		
		EkaMurder plugin = EkaMurder.INSTANCE;
		Configuration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("guns");
        
        int size = section.getKeys(false).size();
        
        int rows = size / 9 + ((size % 9 == 0) ? 0 : 1);
		Inventory inv = plugin.getServer().createInventory(null, rows*9, "§3Choix du gun");
        
        for(String index : section.getKeys(false)) {
        	int minWin = config.getInt("guns."+index+".min-win");
        	
        	plugin.getLogger().info("ITEM : " + config.getString("guns."+index+".material"));
        	Material value = Material.matchMaterial(config.getString("guns."+index+".material"));
        	ItemStack item = new ItemStack(value);
    	    ItemMeta meta = item.getItemMeta();
    	    ArrayList<String> lore = new ArrayList<String>();
        	
    	    if(this.getStat(MurderStatType.MURDER_WIN) >= minWin) {
        	    lore.add("§2DISPONIBLE");
    	    } else {
    	    	lore.add("§4Il faut "+minWin+" victoire en meurtrier");
    	    }
    	    
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }
        
        this.player.openInventory(inv);
		
	}
	
	public void chooseKnife() {
		EkaMurder plugin = EkaMurder.INSTANCE;
		Configuration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("guns");
        
        int size = section.getKeys(false).size();
        
        int rows = size / 9 + ((size % 9 == 0) ? 0 : 1);
		Inventory inv = plugin.getServer().createInventory(null, rows*9, "§3Choix du knife");
        
        for(String index : section.getKeys(false)) {
        	int minWin = config.getInt("knifes."+index+".min-win");
        	
        	plugin.getLogger().info("ITEM : " + config.getString("knife."+index+".material"));
        	Material value = Material.matchMaterial(config.getString("knife."+index+".material"));
        	ItemStack item = new ItemStack(value);
    	    ItemMeta meta = item.getItemMeta();
    	    ArrayList<String> lore = new ArrayList<String>();
        	
    	    if(this.getStat(MurderStatType.MURDER_WIN) >= minWin) {
        	    lore.add("§2DISPONIBLE");
    	    } else {
    	    	lore.add("§4Il faut "+minWin+" victoire en meurtrier");
    	    }
    	    
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }
        
        this.player.openInventory(inv);
	}
}
