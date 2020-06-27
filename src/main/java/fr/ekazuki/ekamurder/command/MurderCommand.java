package fr.ekazuki.ekamurder.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.ekazuki.ekamurder.EkaMurder;
import fr.ekazuki.ekamurder.player.MurderFakePlayer;
import fr.ekazuki.ekamurder.player.MurderPlayer;
import fr.ekazuki.ekamurder.player.MurderSkin;

public class MurderCommand implements CommandExecutor {
	
	private EkaMurder plugin;
	
	public MurderCommand(EkaMurder plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.plugin.debug("Command receive [MurderCommand#onCommand()]");
		if (args.length == 1) {
			this.plugin.debug("Only one argument [MurderCommand#onCommand()]");
			switch(args[0]) {
				case "start":
					this.plugin.debug("/murder start [MurderCommand#onCommand()]");
					if(sender.hasPermission("murder.start")) {
						this.plugin.startGame(sender);
					} else {
						this.noPerm(sender);
					}
					break;
				case "stop":
					if(sender.hasPermission("murder.stop")) {
						this.plugin.stopGame();
					} else {
						this.noPerm(sender);
					}
					this.plugin.debug("/murder stop [MurderCommand#onCommand()]");
					break;
				case "addspawn":
					this.plugin.debug("/murder addspawn [MurderCommand#onCommand()]");
					if(sender.hasPermission("murder.addspawn")) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							this.addSpawnPoint(player.getLocation());
							sender.sendMessage("§4[§c§lMurder§r§4]§e La position de spawn à été ajouté");
						} else {
							this.onlyPlayer(sender);
						}
					} else {
						this.noPerm(sender);
					}
					break;
				case "resetspawn":
					if(sender.hasPermission("murder.resetspawn")) {
						this.plugin.debug("/murder resetspawn [MurderCommand#onCommand()]");
						sender.sendMessage("§4[§c§lMurder§r§4]§e Les positions de spawn ont été remises à zéro");
						this.resetSpawnPoint();
					} else {
						this.noPerm(sender);
					}

					break;
				case "deathpos":
					this.plugin.debug("/murder deathpos [MurderCommand#onCommand()]");
					if(sender.hasPermission("murder.setdeath")) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							this.setDeathPoint(player.getLocation());
							sender.sendMessage("§4[§c§lMurder§r§4]§e La position de mort à été ajouté");
						} else {
							this.onlyPlayer(sender);
						}
					} else {
						this.noPerm(sender);
					}
					break;
				case "help":
					this.plugin.debug("/murder help [MurderCommand#onCommand()]");
					this.help(sender);
					break;
				case "log":
					if(sender.hasPermission("murder.debug")) {
						for(MurderPlayer pl : this.plugin.players){
							this.plugin.debug(pl.player.getDisplayName() + " is a " + pl.role);
						}
					} else {
						this.noPerm(sender);
					}

					break;
				case "skin":
					if(sender.hasPermission("murder.debug")) {
						MurderFakePlayer player = new MurderFakePlayer(MurderSkin.getMultipleRandom(1).get(0), "USERNAME");
					} else {
						this.noPerm(sender);
					}
					
					break;
				case "failshoot":
					if(sender.hasPermission("murder.debug")) {
						this.plugin.dropBow();
					} else {
						this.noPerm(sender);
					}
					break;
				case "head":
					if(sender.hasPermission("murder.debug")) {
						MurderPlayer mpl = this.plugin.murderPlayerFromOnlinePlayer((Player) sender);
						mpl.giveHeadOf(mpl);
					} else {
						this.noPerm(sender);
					}
					break;
				case "tab":
					if(sender.hasPermission("murder.debug")) {
						this.plugin.addFakePlayer(MurderSkin.AYPIERRE, "jesuispasreel");
					} else {
						this.noPerm(sender);
					}
					break;
				case "tab2":
					if(sender.hasPermission("murder.debug")) {
						this.plugin.addFakePlayer(MurderSkin.AYPIERRE, "jesuispasreel22222222");
					} else {
						this.noPerm(sender);
					}
					break;
				case "resettab":
					if(sender.hasPermission("murder.debug")) {
						this.plugin.clearFakePlayers();
					} else {
						this.noPerm(sender);
					}
					break;
			}
			return true;
		} else {
			return false;
		}
	}
	
	private void noPerm(CommandSender sender) {
		sender.sendMessage("§4[§c§lMurder§r§4]§e Vous n'avez pas la permission de faire cette commande");
	}
	
	private void onlyPlayer(CommandSender sender) {
		sender.sendMessage("§4[§c§lMurder§r§4]§e La console ne peut pas effecuté cette commande");
	}
	
	private void help(CommandSender sender) {
		sender.sendMessage("§4[§c§lMurder§r§4]§e /murder start -> Lance une partie avec les joueurs sur le serveur");
		sender.sendMessage("§4[§c§lMurder§r§4]§e /murder stop -> Arrete la partie en cours s'il y en a une");
		sender.sendMessage("§4[§c§lMurder§r§4]§e /murder addspawn -> Rajoute la position du joueur à la liste des positions de départ");
		sender.sendMessage("§4[§c§lMurder§r§4]§e /murder resetspawn -> Remet à zéro la liste des positions de départ");
		sender.sendMessage("§4[§c§lMurder§r§4]§e /murder deathpos -> définit l'endroit ou seront téléporté les joueurs morts");
	}
	
	@SuppressWarnings("unchecked")
	private void addSpawnPoint(Location loc) {
		this.plugin.debug("Add spawn point [MurderCommand#addSpawnPoint()]");
		FileConfiguration data = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "spawns.yml"));
		List<Object> list = (List<Object>) data.getList("spawns");
		list.add(Arrays.asList((double)loc.getBlockX(), loc.getY(), (double)loc.getBlockZ(), (double)loc.getYaw(), (double)loc.getPitch()));
		try {
			data.save(new File(this.plugin.getDataFolder(), "spawns.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setDeathPoint(Location loc) {
		this.plugin.debug("Set death point [MurderCommand#setDeathPoint()]");
		FileConfiguration data = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "spawns.yml"));
		List<Double> list = new ArrayList<Double>();
		
		list.add((double)loc.getBlockX());
		list.add(loc.getY());
		list.add((double) loc.getBlockZ());
		list.add((double) loc.getYaw());
		list.add((double)loc.getPitch());
		
		data.set("death", list);
		try {
			data.save(new File(this.plugin.getDataFolder(), "spawns.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void resetSpawnPoint(){
		this.plugin.debug("Resetting Spawn points [MurderCommand#resetSpawnPoint()]");
		FileConfiguration data = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "spawns.yml"));
		data.set("spawns", new ArrayList<List<Double>>());
		try {
			data.save(new File(this.plugin.getDataFolder(), "spawns.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
