package fr.ekazuki.ekamurder.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.ekazuki.ekamurder.player.MurderPlayer;

public class MurderPlayerWin extends Event {
		
	private MurderPlayer player;

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    public MurderPlayerWin(MurderPlayer player) {
        this.player = player;
    }
    
    public MurderPlayer getMurderPlayer() {
    	return this.player;
    }

}
