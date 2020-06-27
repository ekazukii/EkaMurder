package fr.ekazuki.ekamurder.integration;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fr.ekazuki.ekamurder.EkaMurder;

public class MurderPluginMessage implements PluginMessageListener {
	
	private EkaMurder plugin;
	
	public MurderPluginMessage() {
		this.plugin = EkaMurder.INSTANCE;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
		System.out.println("RECEIVE PM");
		if(channel.equals("murder:info")) {
			final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
			final String sub = in.readUTF();
			
			switch(sub) {
				case "startGame":
					this.plugin.startGame(player);
					break;
				case "stopGame":
					this.plugin.stopGame();
					break;
				case "getState":
					this.sendState(player);
					break;
			}

		}
	}
	
	private void sendState(Player player) {
		 final ByteArrayDataOutput out = ByteStreams.newDataOutput();
		 out.writeUTF("getState");
		 out.writeBoolean(this.plugin.isPlaying);
		 player.sendPluginMessage(this.plugin, "murder:info", out.toByteArray());
	}

}
