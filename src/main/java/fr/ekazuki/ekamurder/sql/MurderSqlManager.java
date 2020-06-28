package fr.ekazuki.ekamurder.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import fr.ekazuki.ekamurder.EkaMurder;
import fr.ekazuki.ekamurder.event.MurderPlayerDeath;
import fr.ekazuki.ekamurder.event.MurderPlayerWin;
import fr.ekazuki.ekamurder.player.MurderPlayer;
import fr.ekazuki.ekamurder.player.MurderRole;

public class MurderSqlManager implements Listener {
	
	private EkaMurder plugin;
	private Connection connection;
	private Statement statement;
	private String host, database, username, password;
	private int port;
	public Collection<SqlPlayer> players;
	
	public void openConnection() throws SQLException, ClassNotFoundException {
	    if (connection != null && !connection.isClosed()) {
	        return;
	    }
	 
	    synchronized (this) {
	        if (connection != null && !connection.isClosed()) {
	            return;
	        }
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
	    }
	}
	
    public MurderSqlManager() {
    	
    	this.plugin = EkaMurder.INSTANCE;
    	
    	this.players = new ArrayList<SqlPlayer>();
    	
        host = this.plugin.getConfig().getString("host");
        port = this.plugin.getConfig().getInt("port");
        database = this.plugin.getConfig().getString("database");
        username = this.plugin.getConfig().getString("user");
        password = this.plugin.getConfig().getString("pass");
        
        try {
            openConnection();
            this.statement = connection.createStatement();    
            
            this.statement.executeUpdate("CREATE TABLE IF NOT EXISTS `ekamurder` (`death` int(11), `detectiveWin` int(11), `murderWin` int(11), `murderItem` varchar(255), `detectiveItem` varchar(255), `uuid` varchar(255));");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public int getStat(Player player, MurderStatType statType) {
    	return 0;
    }
    
    public ItemStack getItem(Player player) {
    	return null;
    }
    
    @EventHandler
    public void addPlayer(PlayerJoinEvent event) throws SQLException {
    	UUID uuid = event.getPlayer().getUniqueId();
    	PreparedStatement pstatement = this.connection.prepareStatement("SELECT * FROM ekamurder WHERE uuid=?");
    	pstatement.setString(1, uuid.toString());
    	ResultSet result = pstatement.executeQuery();
    	
    	int death, detectiveWin, murderWin;
    	ItemStack murderItem, detectiveItem;
    	
    	if (result.next() == false) { 
			death = 0;
			detectiveWin = 0;
			murderWin = 0;
			murderItem = new ItemStack(Material.STICK, 1);
			detectiveItem = new ItemStack(Material.BOW, 1);
			
			statement.executeUpdate("INSERT INTO ekamurder (death, detectiveWin, murderWin, murderItem, detectiveItem, uuid) VALUES ('"+death+"', '"+detectiveWin+"', '"+murderWin+"', '"+murderItem.getType().toString()+"', '"+detectiveItem.getType().toString()+"', '"+uuid.toString()+"');");
    	} else { 
			death = result.getInt("death");
			detectiveWin = result.getInt("detectiveWin");
			murderWin = result.getInt("murderWin");
			murderItem = new ItemStack(Material.matchMaterial(result.getString("murderItem")), 1);
			detectiveItem = new ItemStack(Material.matchMaterial(result.getString("detectiveItem")), 1);
    	}
    	
    	SqlPlayer sqlp = new SqlPlayer(event.getPlayer(), death, detectiveWin, murderWin, murderItem, detectiveItem);
    	this.players.add(sqlp);
    }
    
    @EventHandler
    public void removePlayer(PlayerQuitEvent event) {
    	SqlPlayer sqlp = this.getSqlPlayer(event.getPlayer());
    	this.savePlayer(sqlp);
    	this.players.remove(sqlp);
    }
    
    private void savePlayer(SqlPlayer player) {
    	UUID uuid = player.player.getUniqueId();
    	
    	int death = player.getStat(MurderStatType.DEATH);
    	int detectiveWin = player.getStat(MurderStatType.DETECTIVE_WIN);
    	int murderWin = player.getStat(MurderStatType.MURDER_WIN);
    	String murderItem = player.getItem(MurderRole.MURDER).getType().toString();
    	String detectiveItem = player.getItem(MurderRole.DETECTIVE).getType().toString();
    	try {
			statement.executeUpdate("UPDATE ekamurder SET death='"+death+"', detectiveWin='"+detectiveWin+"', murderWin='"+murderWin+"', murderItem='"+murderItem+"', detectiveItem='"+detectiveItem+"' WHERE uuid='"+uuid.toString()+"';");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @EventHandler
    public void addDeath(MurderPlayerDeath event) {
    	SqlPlayer sqlp = this.getSqlPlayer(event.getMurderPlayer().player);
    	sqlp.addStat(MurderStatType.DEATH);
    }
    
    @EventHandler
    public void addWin(MurderPlayerWin event) {
    	SqlPlayer sqlp = this.getSqlPlayer(event.getMurderPlayer().player);
    	MurderRole role = event.getMurderPlayer().role;
    	if(role.equals(MurderRole.DETECTIVE)) {
    		sqlp.addStat(MurderStatType.DETECTIVE_WIN);
    	} else if (role.equals(MurderRole.MURDER)) {
    		sqlp.addStat(MurderStatType.MURDER_WIN);
    	}
    }
    
    //@EventHandler
    //public void setItem()
    
    public SqlPlayer getSqlPlayer(Player player) {
    	for(SqlPlayer sqlp : this.players) {
    		if (sqlp.getPlayer() == player) {
    			return sqlp;
    		}
    	}
    	
    	return null;
    }
}
