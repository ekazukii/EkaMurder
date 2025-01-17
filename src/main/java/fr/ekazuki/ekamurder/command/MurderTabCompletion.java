package fr.ekazuki.ekamurder.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MurderTabCompletion implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> complete = new ArrayList<String>();
		
		complete.add("start");
		complete.add("stop");
		complete.add("addspawn");
		complete.add("resetspawn");
		complete.add("deathpos");
		complete.add("help");
		complete.add("gun");
		complete.add("knife");
		
		return complete;
	}
}
