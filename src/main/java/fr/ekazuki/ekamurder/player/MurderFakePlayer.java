package fr.ekazuki.ekamurder.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import fr.ekazuki.ekamurder.EkaMurder;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.WorldServer;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class MurderFakePlayer {
	
	public MurderSkin skin;
	public String username;
	public EntityPlayer fp;
	
	public MurderFakePlayer(MurderSkin skin, String username) {
		this.skin = skin;
		this.username = username;
		
		this.appear();
	}
	
	private void appear() {
		String value = skin.getValue();
		String signature = skin.getSignature();
		GameProfile gameProfile;
		if(EkaMurder.INSTANCE.getConfig().getBoolean("custom-tablist")) {
			gameProfile = new GameProfile(UUID.fromString("00000000-0000-0000-0000-000000000000"), "§kUSERNAME");
		} else {
			gameProfile = new GameProfile(UUID.fromString("00000000-0000-0000-0000-000000000000"), this.username);
		}
		
		gameProfile.getProperties().removeAll("textures");
		gameProfile.getProperties().put("textures", new Property("textures", value, signature));
		CraftServer craftServer = (CraftServer) Bukkit.getServer();
		MinecraftServer minecraftServer = craftServer.getServer();
		WorldServer worldServer = ((CraftWorld) Bukkit.getWorld("world")).getHandle();
		EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));
		minecraftServer.getPlayerList().players.remove(entityPlayer);
		
		this.fp = entityPlayer;
		
        for(Player pl : Bukkit.getOnlinePlayers()) {
        	((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
        }
	}
	
	public void clear() {
		for(Player rp : Bukkit.getOnlinePlayers()){
			((CraftPlayer)rp).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, this.fp));
		}
	}
	
	public void changeSkin(MurderSkin skin) {
		this.clear();
		this.skin = skin;
		this.appear();
	}
}


