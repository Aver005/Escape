package kiviuly.escape;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Chat implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String prefix = "";
	private String suffix = "";
	private String lastMessage = "";
	private String joinMessage = "";
	private String leaveMessage = "";
	
	private Integer messageLimit = -1;
	
	private ArrayList<UUID> players = new ArrayList<>();
	private ArrayList<String> messages = new ArrayList<>();
	private ArrayList<String> parsedMessages = new ArrayList<>();
	private ArrayList<String> playerPlaceHolders = new ArrayList<>();
	
	private World world = null;
	
	public Chat(String name) {setName(name);}
	
	public void SendMessage(String msg, Player pl)
	{
		msg = doColor(msg);
		
		for(UUID id : players)
		{
			Player p = Bukkit.getPlayer(id);
			if (p == null) {continue;}
			if (!p.isOnline()) {continue;}
			p.sendMessage(prefix + pl.getDisplayName() + suffix + msg);
		}
		
		messages.add(msg);
		parsedMessages.add(prefix + msg + suffix);
	}
	
	public void SendSystemMessage(String msg)
	{
		msg = doColor(msg);
		
		for(UUID id : players)
		{
			Player p = Bukkit.getPlayer(id);
			if (p == null) {continue;}
			if (!p.isOnline()) {continue;}
			p.sendMessage(msg);
		}
		
		messages.add(msg);
		parsedMessages.add(msg);
	}
	
	public void addPlayer(OfflinePlayer p)
	{
		if (players.contains(p.getUniqueId())) {return;}
		players.add(p.getUniqueId());
		if (joinMessage.isEmpty()) {return;}
		SendSystemMessage(parseMessage(joinMessage, p));
	}
	
	public void removePlayer(OfflinePlayer p)
	{
		if (!players.contains(p.getUniqueId())) {return;}
		players.remove(p.getUniqueId());
		if (leaveMessage.isEmpty()) {return;}
		SendSystemMessage(parseMessage(leaveMessage, p));
	}
	
	public String parseMessage(String msg, OfflinePlayer p)
	{
		for(String ph : playerPlaceHolders)
		{
			if (msg.contains(ph))
			{
				msg = msg.replace(ph, p.getName());
			}
		}
		
		return msg;
	}
	
	public void addPlayerPlaceHolder(String ph)
	{
		this.playerPlaceHolders.add(ph);
	}
	
	public String doColor(String msg)
	{
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public ArrayList<UUID> getPlayers() {
		return players;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public Integer getMessageLimit() {
		return messageLimit;
	}

	public void setMessageLimit(Integer messageLimit) {
		this.messageLimit = messageLimit;
	}

	public String getJoinMessage() {
		return joinMessage;
	}

	public void setJoinMessage(String joinMessage) {
		this.joinMessage = joinMessage;
	}

	public String getLeaveMessage() {
		return leaveMessage;
	}

	public void setLeaveMessage(String leaveMessage) {
		this.leaveMessage = leaveMessage;
	}
}