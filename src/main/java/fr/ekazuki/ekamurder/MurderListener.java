package fr.ekazuki.ekamurder;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import fr.ekazuki.ekamurder.player.MurderFakePlayer;
import fr.ekazuki.ekamurder.player.MurderPlayer;
import fr.ekazuki.ekamurder.player.MurderRole;
import fr.ekazuki.ekamurder.player.MurderSkin;
 
public class MurderListener implements Listener {
	public EkaMurder plugin;
	
	public MurderListener(EkaMurder plugin) {
		this.plugin = plugin;
	}
	
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
    	
    	if(!this.plugin.isPlaying) {
    		return;
    	}
    	
    	this.plugin.debug("PlayerInteractEvent called [MurderListener#onPlayerUse()]");
        Player p = event.getPlayer();
    	Block block = event.getClickedBlock();
    	if (block != null) {
    		this.plugin.debug("Player click on block [MurderListener#onPlayerUse()]");
    		if (Tag.TRAPDOORS.isTagged(block.getType()) || Tag.DOORS.isTagged(block.getType())) {
    			this.plugin.debug("Player click on door or trapdoor [MurderListener#onPlayerUse()]");
    			
    			if(this.plugin.murderPlayerFromOnlinePlayer(p) == null) {
    				this.plugin.debug("Cancel event cause player is spectator [MurderListener#onPlayerUse()]");
        			event.setCancelled(true);
    			}
    		}
    	}
    	
        if(event.getItem() != null) {
        	this.plugin.debug(p.getDisplayName()+" has an item in hand [MurderListener#onPlayerUse()]");
            if(event.getItem().getItemMeta().getDisplayName().equals("Gun")){
            	this.plugin.debug(p.getDisplayName()+" has an bow in hand [MurderListener#onPlayerUse()]");
            	MurderPlayer mpl = this.plugin.murderPlayerFromOnlinePlayer(p);
            	if (mpl == null) {
            		return;
            	}
            	this.plugin.debug(p.getDisplayName()+" is a MurderPlayer [MurderListener#onPlayerUse()]");
            	
            	mpl.shoot();
            } else if (event.getItem().getType() == Material.PLAYER_HEAD) {
            	
            	MurderPlayer mpl = this.plugin.murderPlayerFromOnlinePlayer(p);
            	if (mpl == null) {
            		return;
            	}
            	
            	ItemMeta meta = event.getItem().getItemMeta();
            	String skinName = meta.getDisplayName();
            	MurderSkin skin = MurderSkin.getMurderSkin(skinName);
            	
            	for(Player nonPlayer : this.plugin.getNonPlayer()) {
            		nonPlayer.sendMessage("§4[§c§lMurder§r§4] §7[§8Spec§7]§e Le joueur a prit le skin : "+skin.getUsername());
            	}
            	
            	// Switch skin of fake player to old skin of player
            	for(MurderFakePlayer fpl : this.plugin.fakePlayers) {
            		if(fpl.skin == skin) {
            			fpl.changeSkin(mpl.skin);
            		}
            	}
            	
            	mpl.setSkin(skin);
            }
        }
    }
    
    @EventHandler
    public void onEntityShoot(EntityShootBowEvent event) {
    	if(!this.plugin.isPlaying) {
    		return;
    	}
    	
    	this.plugin.debug("onEntityShoot called [MurderListener#onEntityShoot()]");
    	event.setCancelled(false);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
    	this.plugin.debug("onEntityDamage called [MurderListener#onEntityDamage()]");
    	
    	if (!this.plugin.isPlaying) {
    		System.out.println("NOT PLAYING");
    		event.setCancelled(true);
    		return;
    	}
    	
    	if (event.getEntityType() == EntityType.PLAYER) {
    		this.plugin.debug("Entity is a Player [MurderListener#onEntityDamage()]");
    		if (event.getCause() == DamageCause.PROJECTILE) {
    			this.plugin.debug("Player is shoot by arrow [MurderListener#onEntityDamage()]");
        		Arrow arrow = (Arrow) event.getDamager();
    			
        		Player victim = (Player) event.getEntity();
    			Player damager = (Player) arrow.getShooter();
				MurderPlayer mpVictim = this.plugin.murderPlayerFromOnlinePlayer(victim);
				MurderPlayer mpDamager = this.plugin.murderPlayerFromOnlinePlayer(damager);
				
    			ItemStack item = damager.getInventory().getItemInMainHand();
    			if (item == null || mpVictim == null || mpDamager == null) {
    				this.plugin.debug("item, victim, or damager is null return [MurderListener#onEntityDamage()]");
    				return;
    			}
    			
    			if(item.getType().equals(Material.AIR)) {
    				return;
    			}
    			
    			if (mpVictim.equals(mpDamager)) {
    				System.out.println("MYSELF");
    			}
    			
    			if (item.getItemMeta().getDisplayName().equals("Gun") && mpDamager.role == MurderRole.DETECTIVE) {
    				this.plugin.debug(mpDamager.player.getDisplayName()+" is detective and has bow [MurderListener#onEntityDamage()]");
    				if(mpVictim.role == MurderRole.INNOCENT) {
    					this.plugin.debug("The player shoot on innocent [MurderListener#onEntityDamage()]");
    					this.plugin.dropBow();
    				} 
    				
					this.plugin.removePlayer(mpVictim);
					this.plugin.checkState();
     			}
    			
    		} else if (event.getCause() == DamageCause.ENTITY_ATTACK) {
    			this.plugin.debug("Player is hit [MurderListener#onEntityDamage()]");
        		Player victim = (Player) event.getEntity();
    			Player damager = (Player) event.getDamager();
				MurderPlayer mpVictim = this.plugin.murderPlayerFromOnlinePlayer(victim);
				MurderPlayer mpDamager = this.plugin.murderPlayerFromOnlinePlayer(damager);
				
    			ItemStack item = damager.getInventory().getItemInMainHand();
    			if (item == null || mpVictim == null || mpDamager == null) {
    				this.plugin.debug("item, victim, or damager is null return [MurderListener#onEntityDamage()]");
    				return;
    			}
    			
    			if(item.getType().equals(Material.AIR)) {
    				return;
    			}
    			
    			if (mpVictim.equals(mpDamager)) {
    				return;
    			}
    			
    			if (item.getItemMeta().getDisplayName().equals("Knife") && mpDamager.role == MurderRole.MURDER) {
    				this.plugin.debug(mpDamager.player.getDisplayName()+" is murder and has stick [MurderListener#onEntityDamage()]");
    				
    				if(mpVictim.role == MurderRole.DETECTIVE) {
    					this.plugin.debug("The player hit DETECTIVE [MurderListener#onEntityDamage()]");
    				}
    				
    				mpDamager.giveHeadOf(mpVictim);
    				
    				this.plugin.removePlayer(mpVictim);
    				this.plugin.checkState();
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
    	
    	if(!this.plugin.isPlaying) {
    		return;
    	}
    	
    	//this.plugin.debug("onPickUp called [MurderListener#onPickUp()]");
    	if(!event.getEntityType().equals(EntityType.PLAYER)) {
    		event.setCancelled(true);
    		this.plugin.debug("Entity is not a player [MurderListener#onPickUp()]");
    		return;
    	}
    	
    	if(!event.getItem().getItemStack().getType().equals(Material.BOW)) {
    		event.setCancelled(true);
    		this.plugin.debug("Item is not a bow [MurderListener#onPickUp()]");
    		return;
    	}
    	
    	Player player = (Player) event.getEntity();
    	MurderPlayer mpl = this.plugin.murderPlayerFromOnlinePlayer(player);
    	
    	if(mpl == null) {
    		event.setCancelled(true);
    		this.plugin.debug("Player is not in game [MurderListener#onPickUp()]");
    		return;
    	}
    	
    	if(this.plugin.getDetective() != null) {
    		event.setCancelled(true);
    		this.plugin.debug("Already a detective in game [MurderListener#onPickUp()]");
    		return;
    	}
    	
    	if(mpl.role.equals(MurderRole.MURDER)) {
    		event.setCancelled(true);
    		this.plugin.debug("Player is murder [MurderListener#onPickUp()]");
    		return;
    	}
    	
    	if(mpl.canGetBow) {
        	this.plugin.debug("Changing role of "+mpl.player.getDisplayName()+" to detective [MurderListener#onPickUp()]");
        	mpl.changeRole(MurderRole.DETECTIVE);
        	return;
    	}
    	
		event.setCancelled(true);
		return;
    	
    
    }
    
    @EventHandler
    public void onHit(ProjectileHitEvent e) {
    	this.plugin.debug("onHit called [MurderListener#onHit()]");
        Projectile p = e.getEntity();
        if(p instanceof Arrow) {
            p.remove();
        }
        return;
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
    	if (!this.plugin.isPlaying) {
    		return;
    	}
    	
    	this.plugin.debug("onDrop called [MurderListener#onDrop()]");
    	
    	event.setCancelled(true);
    }
    
    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
    	this.plugin.debug("onBreakBlock called [MurderListener#onBreakBlock()]");
    	
    	if (!event.getPlayer().hasPermission("murder.break") || this.plugin.isPlaying) {
    		event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("§4[§c§lMurder§r§4]§e §7Le joueur §8"+ event.getPlayer().getDisplayName() +"§7 a rejoint le §lMurder");
        int minPlayer = this.plugin.getConfig().getInt("min-player");
        int maxPlayer = this.plugin.getConfig().getInt("max-player");
        
        this.plugin.getServer().broadcastMessage("min player " + minPlayer + " max player " + maxPlayer);
        
        if(minPlayer == plugin.getServer().getOnlinePlayers().size()) {
        	this.plugin.getServer().broadcastMessage("min = player");
        	int countdownTime = this.plugin.getConfig().getInt("countdown-time");
        	
            new BukkitRunnable() {
            	int counter = countdownTime;
            	public void run() {
        			if(plugin.isPlaying) {
        				this.cancel();
        			}
        			
            		counter--;
            		
          			EkaMurder plugin = EkaMurder.INSTANCE;
        			for(Player online : plugin.getServer().getOnlinePlayers()) {
        				online.setLevel(counter);
        			}
        			
            		if(counter == 0) {
            			plugin.startGame(null);	
            			this.cancel();
            		} else if(counter % 10 == 0) {
            			for(Player online : plugin.getServer().getOnlinePlayers()) {
            				online.sendTitle("§2Plus que "+counter+" secondes", null, 5, 25, 10);
            			}
            		}
            	}
            }.runTaskTimer(this.plugin, 20L, 20L);
        }
        
        if(maxPlayer < plugin.getServer().getOnlinePlayers().size()) {
        	this.plugin.getServer().broadcastMessage("max < player");
        	if(!event.getPlayer().hasPermission("murder.join.bypass")) {
            	event.getPlayer().kickPlayer("Le serveur est plein");
        	}
        }
       
        		
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
    	event.setQuitMessage("§4[§c§lMurder§r§4]§e §7Le joueur §8"+ event.getPlayer().getDisplayName() +"§7 a quitté le §lMurder");
    }
}

