package fr.ekazuki.ekamurder;


import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class MurderPlayer {
	public Player player;
	public MurderRole role;
	private EkaMurder plugin;
	public Boolean canGetBow;
	public MurderSkin skin;
	
	public MurderPlayer(EkaMurder plugin, Player player, MurderRole role, MurderSkin skin) {
		this.plugin = plugin;
		this.player = player;
		this.role = role;
		this.canGetBow = true;
		
		this.plugin.debug("Creating new MurderPlayer "+player.getDisplayName()+ " with role "+role+"[MurderPlayer#MurderPlayer()]");
		
		if(role.equals(MurderRole.MURDER)) {
			this.plugin.debug("Giving stick to murder [MurderPlayer#MurderPlayer()]");
			player.sendTitle("§4Vous êtes Murder", null, 10, 70, 30);
			
			// Set slot 0 of player by a stick
	        ItemStack item = new ItemStack(Material.STICK, 1);
	        ItemMeta meta = item.getItemMeta();
	        meta.setDisplayName("Knife");
	        item.setItemMeta(meta);
	        player.getInventory().setItem(0, item);
	        
		} else if(role.equals(MurderRole.DETECTIVE)) {
			this.plugin.debug("Giving bow and arrow to detective [MurderPlayer#MurderPlayer()]");
			player.sendTitle("§2Vous êtes Detective", null, 10, 70, 30);
			
			//Set slot 0 of player by a bow
	        ItemStack item = new ItemStack(Material.BOW, 1);
	        ItemMeta meta = item.getItemMeta();
	        meta.setDisplayName("Gun");
	        item.setItemMeta(meta);
	        player.getInventory().setItem(0, item);
	        
	        this.giveArrow();
		} else {
			this.plugin.debug("Nothing he is innocent [MurderPlayer#MurderPlayer()]");
			player.sendTitle("§lVous êtes Innocent", null, 10, 70, 30);
		}
		
		this.setSkin(skin);
	}
	
	public void remove() {
		if (this.role == MurderRole.DETECTIVE) {
			this.plugin.dropBow();
		} 
	}
	
	public void changeRole(MurderRole newRole) {
		this.plugin.debug("Changing role of "+this.player.getDisplayName()+"he was "+this.role+" and now he is"+newRole+" [MurderPlayer#changeRole()]");
		if(this.role.equals(MurderRole.DETECTIVE)) {
			this.plugin.debug("Clearing inventory cause he was detective [MurderPlayer#changeRole()]");
			this.player.getInventory().clear();
			
			this.canGetBow = false;
			MurderPlayer me = this;
			
	        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
	            public void run() {
	            	me.canGetBow = true;
	            }
	        }, 20*10);
		}
		
		if(newRole.equals(MurderRole.DETECTIVE)) {
			this.plugin.debug("Giving item cause he will become detective [MurderPlayer#changeRole()]");
	        
	        this.giveArrow();
		}
		
		this.role = newRole;
	}
	
	public void shoot() {
		this.plugin.debug("Attempt to make "+this.player.getDisplayName()+" with role "+this.role+" shoot arrow [MurderPlayer#shoot()]");
		
		if(!this.role.equals(MurderRole.DETECTIVE)) {
			this.plugin.debug("Player is not detective return [MurderPlayer#shoot()]");
			return;
		}
		
		if(this.player.getInventory().contains(Material.ARROW)) {
			this.plugin.debug("Player has an arrow in inventory [MurderPlayer#shoot()]");
			this.player.getInventory().clear();
			this.giveBow();
			Vector playerDirection = this.player.getLocation().getDirection();
			Arrow arrow = this.player.launchProjectile(Arrow.class, playerDirection);
			arrow.setVelocity(player.getLocation().getDirection().normalize().multiply(2));
			arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
			
			MurderPlayer mpl = this;
	        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
	            public void run() {
	            	mpl.plugin.debug("Giving another arrow after 3 seconds [MurderPlayer#shoot()]");
	            	mpl.giveArrow();
	            }
	        }, 20*3);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void giveHeadOf(MurderPlayer p) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwningPlayer(this.plugin.getServer().getOfflinePlayer(p.skin.getUsername()));
		meta.setDisplayName(p.skin.getUsername());
		skull.setItemMeta(meta);
		
		this.player.getInventory().setItem(2, skull);
	}
	
	public void setSkin(MurderSkin skin) {
		this.plugin.debug("Attempt to change skin of "+this.player.getDisplayName() + " [EkaMurder#setSkin()]");
		this.skin = skin;
		
		String value = skin.getValue();
		String signature = skin.getSignature();
        for(Player pl : this.plugin.getServer().getOnlinePlayers()){
            if(pl == this.player) continue;
            CraftPlayer cpl = ((CraftPlayer)pl);
 
            //REMOVES THE PLAYER
            cpl.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer)this.player).getHandle()));
            //CHANGES THE PLAYER'S GAME PROFILE
    		GameProfile gameProfile = cpl.getHandle().getProfile();
    		gameProfile.getProperties().removeAll("textures");
    		gameProfile.getProperties().put("textures", new Property("textures", value, signature));
        	pl.setPlayerListName("§kUSERNAME");
    		
            //ADDS THE PLAYER
    		cpl.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer)this.player).getHandle()));
    		cpl.getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(this.player.getEntityId()));
    		cpl.getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer)this.player).getHandle()));
        }
	}
	
	private void giveArrow() {
		this.plugin.debug("Attempt to give arrow to "+this.player.getDisplayName()+" with role "+this.role+" [MurderPlayer#giveArrow()]");
		
		if (this.player.getInventory().contains(Material.ARROW)) {
			this.plugin.debug("Player has already an arrow in inventory [MurderPlayer#giveArrow()]");
			return;
		}
		
		//Set slot 1 of player by an arrow
        ItemStack item = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Bullet");
        item.setItemMeta(meta);
        this.player.getInventory().setItem(1, item);
	}
	
	public void giveBow() {
		this.plugin.debug("Attempt to give bow to "+this.player.getDisplayName()+" with role "+this.role+" [MurderPlayer#giveBow()]");
		//Set slot 1 of player by an arrow
        ItemStack item = new ItemStack(Material.BOW, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Gun");
        item.setItemMeta(meta);
        this.player.getInventory().setItem(0, item);
	}
}
