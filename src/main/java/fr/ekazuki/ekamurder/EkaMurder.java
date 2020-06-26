package fr.ekazuki.ekamurder;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class EkaMurder extends JavaPlugin{
	
	public Boolean isPlaying;
	public Collection<MurderPlayer> players;
	public Collection<MurderFakePlayer> fakePlayers;
    private final SecureRandom random = new SecureRandom();
	public boolean isDebugging;
	
	public static EkaMurder INSTANCE;


	@Override
	public void onEnable() {
		INSTANCE = this;
		
		this.isDebugging = true;
		this.saveDefaultConfig();
		this.getCommand("murder").setExecutor(new MurderCommand(this));
	    this.getServer().getPluginManager().registerEvents(new MurderListener(this), this);
	    this.getCommand("murder").setTabCompleter(new MurderTabCompletion());
		this.isPlaying = false;
		this.players = new ArrayList<MurderPlayer>();
		this.fakePlayers = new ArrayList<MurderFakePlayer>();
		
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "murder:info", new MurderPluginMessage());
	    //this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "murder:info");
		
        if(this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null){
            new MurderPlaceholder().register();
      }
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public MurderPlayer getDetective() {
		this.debug("Attempt to get the Murder [EkaMurder#getMurder()]");
		if (this.isPlaying) {
			for(MurderPlayer mpl : this.players) {
				if (mpl.role.equals(MurderRole.DETECTIVE)) {
					this.debug("Detective is "+mpl.player.getDisplayName()+" [EkaMurder#getDetective()]");
					return mpl;
				}
			}
		}
		
		this.debug("No murder found [EkaMurder#getMurder()]");
		return null;
	}
	
	public MurderPlayer getMurder() {
		this.debug("Attempt to get the Murder [EkaMurder#getMurder()]");
		if (this.isPlaying) {
			for(MurderPlayer mpl : this.players) {
				if (mpl.role.equals(MurderRole.MURDER)) {
					this.debug("Murder is "+mpl.player.getDisplayName()+" [EkaMurder#getMurder()]");
					return mpl;
				}
			}
		}
		
		this.debug("No murder found [EkaMurder#getMurder()]");
		return null;
	}
	
	public Collection<Player> getNonPlayer() {
		Collection<Player> nonPlayer = new ArrayList<Player>();
		for(Player pl : this.getServer().getOnlinePlayers()) {
			if (this.murderPlayerFromOnlinePlayer(pl) == null) {
				nonPlayer.add(pl);
			}
		}
		
		return nonPlayer;
	}
	
	@SuppressWarnings("unchecked")
	public void startGame(CommandSender sender) {
		this.players = new ArrayList<MurderPlayer>();
		this.debug("Attempt to start the game [EkaMurder#startGame()]");
		Collection<? extends Player> bukkitPlayers = this.getServer().getOnlinePlayers();
		
		if(bukkitPlayers.size() < 3) {
			sender.sendMessage("§4Erreur : §cVous ne pouvez pas lancer à murder à moins de 3 joueur !");
		}
		
		if(this.getConfig().getList("spawns").size() < bukkitPlayers.size()) {
			sender.sendMessage("§4Erreur : §cIl n'y a pas assez de points de spawn !");
			sender.sendMessage("§8§oPour les définir, merci de faire §7/murder addSpawn");
			return;
		}
		
		this.getServer().broadcastMessage("§4[§c§lMurder§r§4]§e La partie de Murder commence");
		
		List<MurderRole> roles = new ArrayList<MurderRole>();
		roles.add(MurderRole.MURDER);
		roles.add(MurderRole.DETECTIVE);
		for (int i = 0; roles.size() < bukkitPlayers.size(); i++) {
			this.debug("Adding an innocent to the game [EkaMurder#startGame()]");
			roles.add(MurderRole.INNOCENT);
		}
		
		List<?> original = this.getConfig().getList("spawns");
		List<Object> list = new ArrayList<Object>(original);
		this.clearItem();
		
		List<MurderSkin> skins = MurderSkin.getMultipleRandom(bukkitPlayers.size());
		
		for(Player player : bukkitPlayers) {
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().clear();
			for(Player online : this.getServer().getOnlinePlayers()) {
				online.showPlayer(this, player);
			}
			MurderRole role = roles.remove(random.nextInt(roles.size()));
			MurderSkin skin = skins.remove(skins.size() - 1);
			player.sendMessage("§4[§c§lMurder§r§4]§e Vous avez le skin : "+skin.getUsername());
			List<Double> location = (List<Double>) list.remove(random.nextInt(list.size()));
			player.teleport(new Location(player.getWorld(), location.get(0)+0.5, location.get(1), location.get(2)+0.5, location.get(3).floatValue(), location.get(4).floatValue()));
			this.players.add(new MurderPlayer(this, player, role, skin));
			this.debug(player.getDisplayName()+" will be a "+role+" [EkaMurder#startGame()]");
		}
		
		this.isPlaying = true;
	}
	
	public void stopGame() {
		this.getServer().broadcastMessage("§4[§c§lMurder§r§4]§e La partie de Murder est finie");
		this.debug("Stopping the game [EkaMurder#stopGame()]");
		
		@SuppressWarnings("unchecked")
		List<Double> location = (List<Double>) this.getConfig().getList("death");
		Location loc = new Location(this.getServer().getWorld("world"), location.get(0)+0.5, location.get(1), location.get(2)+0.5, location.get(3).floatValue(), location.get(4).floatValue());

		
		for (Player p : this.getServer().getOnlinePlayers()) {
			for(Player online : this.getServer().getOnlinePlayers()) {
				online.showPlayer(this, p);
			}
			
			p.setGameMode(GameMode.SURVIVAL);
			p.teleport(loc);
		}
		
		this.clearFakePlayers();
		this.isPlaying = false;
	}
	
	public void checkState() {
		
		if(!this.isPlaying) {
			return;
		}
		
		this.debug("Cheking end of game [EkaMurder#checkState()]");
		String title = null;
		
		if (this.players.size() == 1) {
			if (this.getDetective() == null) {
				title = "§4Victoire du Murder";
			} else {
				title = "§2Victoire du Détéctive";
			}
		} else {
			if (this.getMurder() == null) {
				title = "§2Victoire du Détéctive";
			}
		}
		
		if (title != null) {
			this.stopGame();
			for (Player p : this.getServer().getOnlinePlayers()) {
				p.sendTitle(title, null, 10, 70, 30);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removePlayer(MurderPlayer mpl) {
		this.debug("Attempt to remove player "+mpl.player.getDisplayName()+" with role "+mpl.role+" from the game [EkaMurder#removePlayer()]");
		
		mpl.player.sendMessage("§4[§c§lMurder§r§4]§e Vous êtes mort, les autres personne ne peuvent plus vous voir");
		
		this.addFakePlayer(mpl.skin, mpl.player.getDisplayName());
		
		if (mpl.role == MurderRole.DETECTIVE) {
			this.debug("Player was detective dropping the bow [EkaMurder#removePlayer()]");
			this.dropBow();
		} 
		
		this.players.remove(mpl);
		this.checkState();
		List<Double> location = (List<Double>) this.getConfig().getList("death");
		mpl.player.teleport(new Location(mpl.player.getWorld(), location.get(0)+0.5, location.get(1), location.get(2)+0.5, location.get(3).floatValue(), location.get(4).floatValue()));
		mpl.player.setGameMode(GameMode.SPECTATOR);
		
		for(Player online : this.getServer().getOnlinePlayers()) {
			online.hidePlayer(this, mpl.player);
		}
	}
	
	public void dropBow() {
		this.debug("Attempt to drop bow from detective [EkaMurder#dropBow()]");
		MurderPlayer detective = this.getDetective();
		
		if (detective == null) {
			this.debug("No detective found return [EkaMurder#dropBow()]");
			return;
		}
		
		detective.changeRole(MurderRole.INNOCENT);
		
        ItemStack item = new ItemStack(Material.BOW, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Gun");
        item.setItemMeta(meta);
		
		detective.player.getWorld().dropItem(detective.player.getLocation(), item);
		
		EkaMurder plugin = this;
		
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
            	plugin.debug("Attempt to give bow to random player after 30s [EkaMurder#dropBow()]");
            	plugin.giveBow();
            }
        }, 20*30);
	}
	
	public void giveBow() {
		this.debug("Attempt to give bow to detective [EkaMurder#giveBow()]");
		if(this.getDetective() == null) {
			this.debug("No detective found [EkaMurder#giveBow()]");
			List<MurderPlayer> tempPlayers = new ArrayList<MurderPlayer>(this.players);
			
			while(tempPlayers.size() != 0) {
				MurderPlayer mpl = tempPlayers.remove(random.nextInt(tempPlayers.size()));
				if (mpl.role == MurderRole.INNOCENT) {
					this.debug("Found innocent, changing his role [EkaMurder#giveBow()]");
					mpl.giveBow();
					mpl.changeRole(MurderRole.DETECTIVE);
					break;
				}
			}
			
			this.checkState();
			return;
		}
	}
	
	
	public MurderPlayer murderPlayerFromOnlinePlayer(Player player) {
		this.debug("Attempt to get MurderPlayer from "+player.getDisplayName()+" [EkaMurder#murderPlayerFromOnlinePlayer()]");
		for(MurderPlayer mpl : this.players) {
			if(mpl.player.equals(player)) {
				this.debug("Found MurderPlayer from "+player.getDisplayName()+" [EkaMurder#murderPlayerFromOnlinePlayer()]");
				return mpl;
			}
		}
		
		return null;
	}
	
	public void clearItem() {
		this.debug("Attempt to clear items [EkaMurder#clearItem()]");
		Iterator<World> it = this.getServer().getWorlds().iterator();
		while(it.hasNext()) {
			List<Entity> entities = it.next().getEntities();
			for(Entity current : entities) {
				if (current instanceof Item) {
					current.remove();
				}
			}
		}
	}
	
	public void addFakePlayer(MurderSkin skin, String username) {
		MurderFakePlayer fp = new MurderFakePlayer(skin, username);
		
		this.fakePlayers.add(fp);
	}
	
	public void clearFakePlayers() {
		for(MurderFakePlayer fp : this.fakePlayers) {
			fp.clear();
		}
		
		this.fakePlayers = new ArrayList<MurderFakePlayer>();
	}
	
	public void debug(String log) {
		if (this.isDebugging) {
			System.out.println(log);
		}
	}
}	
