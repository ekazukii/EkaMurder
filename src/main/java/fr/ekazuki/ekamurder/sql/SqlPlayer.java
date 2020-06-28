package fr.ekazuki.ekamurder.sql;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
}
