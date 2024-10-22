package fr.ekazuki.ekamurder.integration;
import org.bukkit.entity.Player;

import fr.ekazuki.ekamurder.EkaMurder;
import fr.ekazuki.ekamurder.player.MurderPlayer;
import fr.ekazuki.ekamurder.sql.MurderStatType;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class MurderPlaceholder extends PlaceholderExpansion {
	
	private EkaMurder plugin;
	
	public MurderPlaceholder() {
		this.plugin = EkaMurder.INSTANCE;
	}

	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return "ekamurder";
	}
	
    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }
    
    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

	@Override
	public String getVersion() {
        return plugin.getDescription().getVersion();
	}
	
    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        // %someplugin_placeholder1%
        if(identifier.equals("role")){
            MurderPlayer mpl = this.plugin.murderPlayerFromOnlinePlayer(player);
            if(mpl == null) return null;
            
            return mpl.role.getDisplayName();
        }

        // %someplugin_placeholder2%
        if(identifier.equals("alive_or_dead")){
        	MurderPlayer mpl = this.plugin.murderPlayerFromOnlinePlayer(player);
        	
        	if(mpl == null) {
        		return "dead";
        	} else {
            	return "alive";
        	}
        }
        
        // %someplugin_placeholder2%
        if(identifier.equals("get_detective")){
            MurderPlayer detective = this.plugin.getDetective();
            if(detective == null) return null;
            
            return detective.player.getName();
        }
        
        // %someplugin_placeholder2%
        if(identifier.equals("get_murder")){
            MurderPlayer murder = this.plugin.getMurder();
            if(murder == null) return null;
            
            return murder.player.getName();
        }
        
        if(identifier.equals("murder_win")) {
        	return String.valueOf(this.plugin.sqlManager.getStat(player, MurderStatType.MURDER_WIN));
        }
        
        if(identifier.equals("detective_win")) {
        	return String.valueOf(this.plugin.sqlManager.getStat(player, MurderStatType.DETECTIVE_WIN));
        }

		if(identifier.equals("death")) {
			return String.valueOf(this.plugin.sqlManager.getStat(player, MurderStatType.DEATH));
		}
 
        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%) 
        // was provided
        return null;
    }

}
