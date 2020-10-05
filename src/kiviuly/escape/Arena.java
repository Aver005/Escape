package kiviuly.escape;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Arena implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Main main = Main.getMain();
	
	private String ID;
	private String Name;
	private String Description;
	private String world;
	
	private UUID creator;
	
	private Integer maxPlayers = 12;
	private Integer minPlayers = 2;
	private Integer chestCount = 75;
	private Integer villagersCount = 32;
	private Integer tablesCount = 5;
	
	private Boolean isStarted = false;
	private Boolean isStarting = false;
	private Boolean isEnabled = true;
	
	private Location lobbyLocation;
	
	private ArrayList<UUID> playersInLobby =  new ArrayList<>();
	private ArrayList<UUID> playersInGame =  new ArrayList<>();
	private ArrayList<UUID> playersInSpectators =  new ArrayList<>();
	
	private ArrayList<String> deadMessages = new ArrayList<>(); 
	
	private ArrayList<Location> spawnsLocations =  new ArrayList<>();
	private ArrayList<Location> chestsLocations =  new ArrayList<>();
	private ArrayList<Location> tablesLocations =  new ArrayList<>();
	private ArrayList<Location> oresLocations =  new ArrayList<>();
	
	private HashMap<UUID, Double> lobbyDamagers = new HashMap<>();
	
	private HashMap<String, Object> villagers = new HashMap<>();
	private HashMap<String, Object> levers = new HashMap<>();
	
	private HashMap<Location, Material> editedBlocks = new HashMap<>();
	
	private ArrayList<ItemStack> chestItems = new ArrayList<>();
	private ArrayList<Location> tradersInGameLocations =  new ArrayList<>();
	private ArrayList<Location> chestsInGameLocations =  new ArrayList<>();
	private ArrayList<Location> tablesInGameLocations =  new ArrayList<>();
	private ArrayList<Location> armorstandsInGameLocations =  new ArrayList<>();
	
	private HashMap<String, Object> playersStats = new HashMap<>();
	private HashMap<String, Object> villagersItems = new HashMap<>();
	private ArrayList<String> contracts = new ArrayList<>();
	
	private ArrayList<Entity> drops = new ArrayList<>();
	
	private Date createdDate;
	
	private Chat publicChat;
	private Chat lobbyChat;
	private Chat spectatorChat;
	
	private Arena arena = this;
	
	//private BukkitRunnable startTimer = null;
	//private BukkitRunnable stopTimer = null;
	
	public boolean SaveToYML(File file)
	{
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		config.set("ID", getID());
		config.set("Name", getName());
		config.set("Description", getDescription());
		config.set("WorldName", getWorld());
		config.set("Creator", getCreator().toString());
		config.set("MaxPlayers", getMaxPlayers());
		config.set("MinPlayers", getMinPlayers());
		config.set("ChestCount", getChestCount());
		config.set("TradersCount", getVillagersCount());
		config.set("TablesCount", getTablesCount());
		config.set("isEnabled", isEnabled);
		config.set("LobbyLocation", lobbyLocation);
		config.set("DeadMessages", deadMessages);
		config.set("SpawnsLocations", spawnsLocations);
		config.set("ChestsLocations", chestsLocations);
		config.set("TablesLocations", tablesLocations);
		config.set("OresLocations", oresLocations);
		config.set("Villagers", villagers);
		config.set("Levers", levers);
		config.set("ChestItems", chestItems);
		config.set("VillagersItems", villagersItems);
		config.set("PlayerStats", playersStats);
		config.set("Contracts", contracts);
		config.set("CreatedDate", createdDate);
		try {config.save(file); return true;} 
		catch (IOException e) {e.printStackTrace(); return false;}
	}
	
	public boolean LoadFromYML(File file)
	{
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		this.ID = config.getString("ID");
		setName(config.getString("Name"));
		setDescription(config.getString("Description"));
		setWorld(config.getString("WorldName"));
		setCreator(UUID.fromString(config.getString("Creator")));
		setMaxPlayers(config.getInt("MaxPlayers"));
		setMinPlayers(config.getInt("MinPlayers"));
		chestCount = config.getInt("ChestCount", chestCount);
		villagersCount = config.getInt("TradersCount", villagersCount);
		tablesCount = config.getInt("TablesCount", tablesCount);
		isEnabled = config.getBoolean("isEnabled", false);
		lobbyLocation = (Location) config.get("LobbyLocation");
		deadMessages = (ArrayList<String>) config.get("DeadMessages");
		spawnsLocations = (ArrayList<Location>) config.get("SpawnsLocations");
		chestsLocations = (ArrayList<Location>) config.get("ChestsLocations");
		tablesLocations = (ArrayList<Location>) config.get("TablesLocations");
		oresLocations = (ArrayList<Location>) config.get("OresLocations");
		villagers = (HashMap<String, Object>) config.getConfigurationSection("Villagers").getValues(false);
		levers = (HashMap<String, Object>) config.getConfigurationSection("Levers").getValues(false);
		villagersItems = (HashMap<String, Object>) config.getConfigurationSection("VillagersItems").getValues(false);
		playersStats = (HashMap<String, Object>) config.getConfigurationSection("PlayerStats").getValues(false);
		chestItems = (ArrayList<ItemStack>) config.get("ChestItems");
		contracts = (ArrayList<String>) config.get("Contracts");
		createdDate = (Date) config.get("CreatedDate");
		return true;
	}
	
	public Arena(String ID, Integer maxPlayers, UUID creator, World w)
	{
		this.ID = ID;
		this.Name = ID;
		this.setMaxPlayers(maxPlayers);
		this.setCreator(creator);
		this.setCreatedDate(new Date());
		this.setWorld(w.getName());
		
		publicChat = new Chat("ArenaChat-"+ID);
		publicChat.setPrefix("§9[Всем] §e");
		publicChat.setSuffix("§8: §7");
		publicChat.addPlayerPlaceHolder("%p%");
		
		spectatorChat = new Chat("SpectatorChat-"+ID);
		spectatorChat.setPrefix("§9[Наблюдателям] §e");
		spectatorChat.setSuffix("§8: §7");
		
		lobbyChat = new Chat("LobbyChat-"+ID);
		lobbyChat.setPrefix("§d[Ожидание...] §e");
		lobbyChat.setSuffix("§8: §7");
		lobbyChat.addPlayerPlaceHolder("%p%");
		lobbyChat.setJoinMessage("§7Игрок §b%p%§7 зашёл на арену §b"+getName());
		lobbyChat.setLeaveMessage("§7Игрок §b%p%§7 вышел");
		
		deadMessages.add("§eИгрок §b%p%§e не справился с управлением");
		deadMessages.add("§eИгрок §b%p%§e споткнулся");
		deadMessages.add("§eИгрок §b%p%§e выбрал лёгкий путь");
	}
	
	public void Start(int time)
	{
		if (isStarted || isStarting) {return;}
		Arena arena = this;
		
		setStarting(true);
		new BukkitRunnable() 
		{	
			int i = time;
			
			@Override
			public void run() 
			{
				if (getPlayersInLobby().size() < minPlayers) {setStarting(false); cancel();}
				
				if (i % 10 == 0 || i < 10) {lobbyChat.SendSystemMessage("§e[Оповещение] §2Начало игры через "+i+" секунд...");}
				
				for(UUID id : getPlayersInLobby())
				{
					OfflinePlayer offP = Bukkit.getOfflinePlayer(id);
					if (!offP.isOnline()) {playersInLobby.remove(id); continue;}
					Player p = offP.getPlayer();
					p.getPlayer().setLevel(i);
				}
				
				i--;
				
				if (i == 5)
				{
					String winner = "";
					double maxDmg = 0.0;
					
					for(UUID id : getLobbyDamagers().keySet())
					{
						if (getLobbyDamagers().get(id) > maxDmg) 
						{
							maxDmg = getLobbyDamagers().get(id);
							if (Bukkit.getPlayer(id) == null) {continue;}
							winner = Bukkit.getPlayer(id).getName();
						}
					}
					
					lobbyChat.SendSystemMessage("§7=============================");
					lobbyChat.SendSystemMessage("");
					lobbyChat.SendSystemMessage("§2§lИзбиение в ЛОББИ:");
					lobbyChat.SendSystemMessage("§6§lПобедитель §d"+winner);
					lobbyChat.SendSystemMessage("§fУрона: §e"+Math.round(maxDmg/2)+" §fсердец");
					for(UUID id : getLobbyDamagers().keySet())
					{
						maxDmg = getLobbyDamagers().get(id); 
						if (Bukkit.getPlayer(id) == null) {continue;}
						winner = Bukkit.getPlayer(id).getName();
						lobbyChat.SendSystemMessage("§b"+winner+" §f— §e"+Math.round(maxDmg/2)+" §fсердец");
					}
					lobbyChat.SendSystemMessage("");
					lobbyChat.SendSystemMessage("§7=============================");
				}
				
				if (i == 0)
				{
					setStarted(true);
					setStarting(false);
					
					ArrayList<Integer> ints = new ArrayList<>();
					for(int k = 0; k < getChestCount(); k++)
					{
						if (chestsLocations.size() <= k) {continue;}
						int chestID = main.randomInt(0, chestsLocations.size()-1);
						while(ints.contains(chestID)) {chestID = main.randomInt(0, chestsLocations.size()-1);}
						Location loc = chestsLocations.get(chestID);
						loc.getBlock().setType(Material.CHEST);
						
						Chest chest = (Chest) loc.getBlock().getState();
						main.generateChestLoot(chest, arena);
						
						ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0.5,-1,0.5), EntityType.ARMOR_STAND);
						as.setAI(false);
						as.setGravity(false);
						as.setCanPickupItems(false);
						as.setInvulnerable(true);
						as.setCustomNameVisible(false);
						as.setVisible(false);
						//as.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
						as.setCustomName("§2Refiled");
						chestsInGameLocations.add(loc);
						armorstandsInGameLocations.add(loc);
					}
					
					ints.clear();
					for(int k = 0; k < getVillagersCount(); k++)
					{
						if (villagers.size() <= k) {continue;}
						int vilID = main.randomInt(0, villagers.size()-1);
						while(ints.contains(vilID)) {vilID = main.randomInt(0, villagers.size()-1);}
						List<Object> villLocs = Arrays.asList(villagers.keySet().toArray());
						Location l = (Location) main.convertStringToLocation(villLocs.get(vilID).toString());
						String type = (String) villagers.get(villLocs.get(vilID).toString());
						Trader trader = main.getTrader(type);
						if (trader == null) {continue;}
						Villager ent = (Villager) main.getServer().getWorld(world).spawnEntity(l, EntityType.VILLAGER);
						ent.setCustomNameVisible(true);
						ent.setCustomName(trader.getName());
						ent.setInvulnerable(true);
						ent.setAI(false);
						tradersInGameLocations.add(ent.getLocation());
						ints.add(vilID);
					}
					
					for(int k = 0; k < oresLocations.size(); k++)
					{
						Location l = oresLocations.get(k);
						if (l == null) {continue;}
						Material mat = Material.MONSTER_EGGS;
						
						switch(main.randomInt(0, 18))
						{
							case 0: case 1: case 2: case 4: case 5: mat = Material.COAL_ORE; break;
							case 6: case 7: case 8: case 9: mat = Material.IRON_ORE; break;
							case 10: case 11: case 12: mat = Material.GOLD_ORE; break;
							case 13: case 14: mat = Material.DIAMOND_ORE; break;
							case 15: mat = Material.EMERALD_ORE; break;
						}
						
						l.getBlock().setType(mat);
					}
					
					for(int k = 0; k < getPlayersInLobby().size(); k++)
					{
						UUID id = getPlayersInLobby().get(k);
						if (id == null) {continue;}
						OfflinePlayer offP = Bukkit.getOfflinePlayer(id);
						lobbyChat.removePlayer(offP);
						if (!offP.isOnline()) {continue;}
						Player p = offP.getPlayer();
						p.setHealth(20);
						p.setFoodLevel(20);
						publicChat.addPlayer(p);
						playersInGame.add(id);
						p.getPlayer().setLevel(0);
						p.teleport(spawnsLocations.get(k));
						p.setGameMode(GameMode.SURVIVAL);
						
						User user = main.getUsersByName().get(p.getName());
						user.setPlaying(true);
						
						/*
						 
						  	ВЫДАЧА ПРЕДМЕТОВ
						  
						*/
						
						p.getInventory().clear();
						p.getInventory().addItem(
							new ItemBuilder(Material.GOLD_PICKAXE, "§6Вилка...")
							.damage((short) 31)
							.lore("§fПрокопай путь к свободе...")
							.lore("§7§oСломай железную дверь...")
							.build());
						
						p.getInventory().addItem(new ItemBuilder(Material.GOLD_INGOT, 24).build());
					}
					
					playersInLobby.clear();
					getLobbyDamagers().clear();
					cancel();
				}
			}
			
		}.runTaskTimer(main, 20L, 20L);
		
		new BukkitRunnable() 
		{
			int time = 1800;
			
			@Override
			public void run() 
			{
				if (getPlayersInGame().size() < 2 || !isStarted) {setStarted(false); cancel();}
				
				for(int i = 0; i < getPlayersInGame().size(); i++)
				{
					UUID id = getPlayersInGame().get(i);
					if (id == null) {continue;}
					OfflinePlayer offPL = Bukkit.getOfflinePlayer(id);
					if (offPL == null) {getPlayersInGame().remove(id); continue;}
					if (!offPL.isOnline()) {getPlayersInGame().remove(id); continue;}
					Player p = offPL.getPlayer();
					p.setLevel(time);
					
					if (time == 1)
					{
						Location l = spawnsLocations.get(i);
						p.teleport(l);
					}
					
					if ((time-1) % 600 == 0) 
					{
						p.getWorld().strikeLightningEffect(p.getLocation());
						p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 16));
					}
					
					if (time == 601) 
					{
						p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
						p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 18));
					}
				}
				
				time--;
				
				if (time == 600) {publicChat.SendSystemMessage("§e[Оповещение] §2Все игроки подсвечены.");}
				if (time % 600 == 0 && time > 0) {publicChat.SendSystemMessage("§e[Оповещение] §2До финальной битвы §e"+time/60+" §2минут...");}
				if (time < 15) {publicChat.SendSystemMessage("§e[Оповещение] §2До финальной битвы §e"+time+" §2секунд...");}
				
				if (time == 0)
				{
					publicChat.SendSystemMessage("§e[Оповещение] §2Переносим на арену финальной битвы...");
					cancel();
				}
			}
			
		}.runTaskTimer(main, 20 * time, 20L);
	}

	public void Stop(int time)
	{
		playersInLobby.clear();
		if (!isStarted && !isStarting) {return;}
		
		new BukkitRunnable() 
		{	
			int i = time;
			
			@Override
			public void run() 
			{
				if (i % 10 == 0 || i < 6) {lobbyChat.SendSystemMessage("§e[Оповещение] §2Конец игры через "+i+" секунд...");}
				
				for(int k = 0; k < getPlayersInGame().size(); k++)
				{
					UUID id = getPlayersInGame().get(k);
					if (id == null) {continue;}
					OfflinePlayer offP = Bukkit.getOfflinePlayer(id);
					if (!offP.isOnline()) {playersInGame.remove(id); continue;}
					Player p = offP.getPlayer();
					p.getPlayer().setLevel(i);
				}
				
				for(int k = 0; k < getPlayersInSpectators().size(); k++)
				{
					UUID id = getPlayersInSpectators().get(k);
					if (id == null) {continue;}
					OfflinePlayer offP = Bukkit.getOfflinePlayer(id);
					if (!offP.isOnline()) {playersInSpectators.remove(id); continue;}
					Player p = offP.getPlayer();
					p.getPlayer().setLevel(i);
				}
				
				i--;
				
				if (i == 0)
				{
					setStarted(false);
					setStarting(false);
					
					for(Location l : main.getChestRefilRunnables().keySet())
					{
						if (l == null) {continue;}
						BukkitRunnable br = main.getChestRefilRunnables().get(l);
						if (br == null) {continue;}
						br.cancel();
					}
					
					for(int k = 0; k < chestsInGameLocations.size(); k++)
					{
						Location loc = chestsInGameLocations.get(k);
						if (loc == null) {continue;}
						if (!loc.getBlock().getType().equals(Material.CHEST)) {continue;}
						
						Chest chest = (Chest) loc.getBlock().getState();
						chest.getInventory().clear();
						loc.getBlock().setType(Material.AIR);
					}
					
					for(int k = 0; k < armorstandsInGameLocations.size(); k++)
					{
						Location loc = armorstandsInGameLocations.get(k);
						if (loc == null) {continue;}
						
						Chunk chunk = loc.getChunk(); chunk.load();
						if (chunk.getEntities().length == 0) {continue;}
						for(Entity e : chunk.getEntities()) {if (e.getType().equals(EntityType.ARMOR_STAND)) {e.remove();}}
						
					}
					
					for(int k = 0; k < tradersInGameLocations.size(); k++)
					{
						Location l = tradersInGameLocations.get(k);
						if (l == null) {continue;}
						Chunk chunk = l.getChunk(); chunk.load();
						if (chunk.getEntities().length == 0) {continue;}
						for(Entity e : chunk.getEntities()) {if (e.getType().equals(EntityType.VILLAGER)) {e.remove();}}
					}
					
					for(Location l : editedBlocks.keySet())
					{
						Material m = editedBlocks.get(l);
						l.getBlock().setType(m);
					}
					
					for(UUID id : getPlayersInGame())
					{
						OfflinePlayer offP = Bukkit.getOfflinePlayer(id);
						User user = main.getUsersByName().getOrDefault(offP.getName(), null);
						if (user == null) {user = new User(offP); main.addUser(offP.getName(),user);}
						user.setArena(null);
						user.setPlaying(false);
						user.setWaiting(false);
						user.setSpectating(false);
						publicChat.removePlayer(offP);
						if (!offP.isOnline()) {continue;}
						Player p = offP.getPlayer();
						main.clearPlayer(p);
						main.loadPlayerData(p);
					}
					
					for(UUID id : getPlayersInSpectators())
					{
						OfflinePlayer offP = Bukkit.getOfflinePlayer(id);
						User user = main.getUsersByName().getOrDefault(offP.getName(), null);
						if (user == null) {user = new User(offP); main.addUser(offP.getName(),user);}
						user.setArena(null);
						user.setPlaying(false);
						user.setWaiting(false);
						user.setSpectating(false);
						spectatorChat.removePlayer(offP);
						if (!offP.isOnline()) {continue;}
						Player p = offP.getPlayer();
						main.clearPlayer(p);
						main.loadPlayerData(p);
					}
					
					for(Entity item : getDrops())
					{
						if (item == null) {continue;}
						Chunk chunk = item.getLocation().getChunk(); chunk.load();
						if (chunk.getEntities().length == 0) {continue;}
						for(Entity e : chunk.getEntities()) {if (e.getType().equals(EntityType.DROPPED_ITEM)) {e.remove();}}
						item.remove();
					}
					
					editedBlocks.clear();
					tradersInGameLocations.clear();
					chestsInGameLocations.clear();
					tablesInGameLocations.clear();
					drops.clear();
					
					playersInGame.clear();
					playersInLobby.clear();
					playersInSpectators.clear();
					playersStats.clear();
					
					cancel();
				}
			}
			
		}.runTaskTimer(main, 20L, 20L);
	}
	
	public String getID() {
		return ID;
	}

	public boolean addPlayerToGame(User user) 
	{
		Player p = user.getPlayer().getPlayer();
		if (maxPlayers == playersInLobby.size()) {return false;}
		if (playersInLobby.contains(p.getUniqueId())) {return false;}
		if (isStarted) {return false;}
		
		user.setWaiting(true);
		user.setArena(arena);
		playersInLobby.add(p.getUniqueId());
		lobbyChat.addPlayer(p);
		main.savePlayerData(p);
		main.clearPlayer(p);
		ItemStack is = new ItemBuilder(Material.MAGMA_CREAM)
			.displayname("§cВыход")
			.lore("§fНажмите ПКМ, чтобы выйти")
			.build();
		p.getInventory().setItem(8, is);
		
		p.teleport(lobbyLocation);
		
		if (playersInLobby.size() == minPlayers) {Start(20);}
		return true;
	}

	public boolean addDeadPlayer(OfflinePlayer p) 
	{
		UUID id = p.getUniqueId();
		if (!playersInGame.contains(id)) {return false;}
		playersInGame.remove(id);
		playersInSpectators.add(id);
		
		if (playersInGame.size() > 1) 
		{
			playersInGame.remove(id);
			playersInSpectators.add(id);
			publicChat.SendSystemMessage("§c[Смерть] §e"+publicChat.parseMessage(getRandomDeadMessage(), p));
			publicChat.SendSystemMessage("§e[Оповещение] §6Осталось §c"+playersInGame.size()+"§6 игроков");
			publicChat.removePlayer(p);
			spectatorChat.addPlayer(p);
			return true;
		}
		
		if (playersInGame.size() == 1) 
		{
			Stop(10);
			id = playersInGame.get(0);
			Player pl = Bukkit.getPlayer(id);
			if (pl == null) {pl = p.getPlayer();}
			if (!pl.isOnline()) {return false;}
			
			publicChat.SendSystemMessage("§7========================================================================");
			publicChat.SendSystemMessage("");
			publicChat.SendSystemMessage("§d[ПОБЕДА] §6§lПобедителем становится §b§l"+pl.getName()+"§6§l!");
			publicChat.SendSystemMessage("§d[ПОБЕДА] §fКвестов выполнено: §e"+getStat(pl, "QuestsCompleted"));
			publicChat.SendSystemMessage("§d[ПОБЕДА] §fУбито: §e"+getStat(pl, "Kills")+"§f игроков");
			publicChat.SendSystemMessage("§d[ПОБЕДА] §fТрейдов сделано: §e"+getStat(pl, "TradesCompleted"));
			publicChat.SendSystemMessage("");
			publicChat.SendSystemMessage("§7========================================================================");
			
			if (pl.getPlayer().getAllowFlight()) {pl.getPlayer().setFlying(true);}
		}
		
		if (playersInGame.size() == 0) {Stop(3);}
		return true;
	}

	public boolean removePlayer(OfflinePlayer p) 
	{
		UUID id = p.getUniqueId();
		User user = main.getUsersByName().getOrDefault(p.getName(), null);
		
		if (user == null) 
		{
			user = new User(p); 
			HashMap<String, User> users = main.getUsersByName();
			users.put(p.getName(), user);
			main.setUsersByName(users);
		}
		
		user.setArena(null);
		user.setPlaying(false); 
		user.setSpectating(false);
		user.setWaiting(false);
		
		if (playersInLobby.contains(id)) 
		{
			if (p.isOnline()) {main.loadPlayerData(p.getPlayer());}
			lobbyChat.removePlayer(p);
			playersInLobby.remove(id); 
			return true;
		}
		
		if (playersInGame.contains(id)) 
		{
			if (p.isOnline()) {main.loadPlayerData(p.getPlayer());}
			playersInGame.remove(id);  
			if (playersInGame.size() == 1) 
			{
				Stop(10);
				id = playersInGame.get(0);
				Player pl = Bukkit.getPlayer(id);
				if (pl == null) {pl = p.getPlayer();}
				if (!pl.isOnline()) {return false;}
				
				publicChat.SendSystemMessage("§7========================================================================");
				publicChat.SendSystemMessage("");
				publicChat.SendSystemMessage("§d[ПОБЕДА] §6§lПобедителем становится §b§l"+pl.getName()+"§6§l!");
				publicChat.SendSystemMessage("§d[ПОБЕДА] §fКвестов выполнено: §e"+getStat(pl, "QuestsCompleted"));
				publicChat.SendSystemMessage("§d[ПОБЕДА] §fУбито: §e"+getStat(pl, "Kills")+"§f игроков");
				publicChat.SendSystemMessage("§d[ПОБЕДА] §fТрейдов сделано: §e"+getStat(pl, "TradesCompleted"));
				publicChat.SendSystemMessage("");
				publicChat.SendSystemMessage("§7========================================================================");
				
				if (pl.getPlayer().getAllowFlight()) {pl.getPlayer().setFlying(true);}
			}
			
			if (playersInGame.size() == 0)
			{
				Stop(3);
			}
			return true;
		} 
		
		if (playersInSpectators.contains(id))
		{
			if (p.isOnline()) {main.loadPlayerData(p.getPlayer());}
			spectatorChat.removePlayer(p);
			playersInSpectators.remove(id); 
			return true;
		} 
		
		return false;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		this.Name = name;
		lobbyChat.setJoinMessage("§7Игрок §b%p%§7 зашёл на арену §b"+getName());
	}
	
	public int getStat(OfflinePlayer p, String stat)
	{
		UUID id = p.getUniqueId();
		int amount = (int) playersStats.getOrDefault(id.toString()+"-"+stat, 0);
		return amount;
	}
	
	public boolean setStat(OfflinePlayer p, String stat, int amount)
	{
		UUID id = p.getUniqueId();
		playersStats.put(id.toString()+"-"+stat, amount);
		return true;
	}
	
	public boolean addStat(OfflinePlayer p, String stat, int amount)
	{
		UUID id = p.getUniqueId();
		int i = (int) playersStats.getOrDefault(id.toString()+"-"+stat, 0);
		playersStats.put(id.toString()+"-"+stat, i+amount);
		return true;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(Integer maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public Integer getMinPlayers() {
		return minPlayers;
	}

	public void setMinPlayers(Integer minPlayers) {
		this.minPlayers = minPlayers;
	}

	public ArrayList<UUID> getPlayersInLobby() {
		return playersInLobby;
	}

	public void setPlayersInLobby(ArrayList<UUID> playersInLobby) {
		this.playersInLobby = playersInLobby;
	}

	public ArrayList<UUID> getPlayersInGame() {
		return playersInGame;
	}

	public void setPlayersInGame(ArrayList<UUID> playersInGame) {
		this.playersInGame = playersInGame;
	}
	
	public ArrayList<UUID> getPlayersInSpectators() {
		return playersInSpectators;
	}

	public void setPlayersInSpectators(ArrayList<UUID> playersInSpectators) {
		this.playersInSpectators = playersInSpectators;
	}
	
	public ArrayList<Location> getSpawnsLocations() {
		return spawnsLocations;
	}

	public void setSpawnsLocations(ArrayList<Location> spawnsLocations) {
		this.spawnsLocations = spawnsLocations;
	}
	
	public void addSpawnLocation(Location l) {
		spawnsLocations.add(l);
	}
	
	public void addChestLocation(Location l) {
		chestsLocations.add(l);
	}
	
	public void addTableLocation(Location l) {
		tablesLocations.add(l);
	}

	public void addLever(Location l, String type) {
		String loc = main.convertLocationToString(l);
		levers.put(loc, type);
	}

	public void addOreLocation(Location l) {
		oresLocations.add(l);
	}
	
	public void addVillager(Location l, String type) {
		String loc = main.convertLocationToString(l);
		villagers.put(loc, type);
	}
	
	public void addItemToChest(Integer chance, ItemStack item) {
		for(int i = 0; i < chance; i++) {chestItems.add(item);}
	}
	
	public void addItemsToChest(Integer chance, ArrayList<ItemStack> items) {
		for(int i = 0; i < chance; i++) {chestItems.addAll(items);}
	}

	public ArrayList<Location> getChestsLocations() {
		return chestsLocations;
	}

	public void setChestsLocations(ArrayList<Location> chestsLocations) {
		this.chestsLocations = chestsLocations;
	}

	public ArrayList<Location> getTablesLocations() {
		return tablesLocations;
	}

	public void setTablesLocations(ArrayList<Location> tablesLocations) {
		this.tablesLocations = tablesLocations;
	}

	public ArrayList<Location> getOresLocations() {
		return oresLocations;
	}

	public void setOresLocations(ArrayList<Location> oresLocations) {
		this.oresLocations = oresLocations;
	}

	public ArrayList<ItemStack> getChestItems() {
		return chestItems;
	}

	public void setChestItems(ArrayList<ItemStack> chestItems) {
		this.chestItems = chestItems;
	}

	public HashMap<String, Object> getVillagersItems() {
		return villagersItems;
	}

	public void setVillagersItems(HashMap<String, Object> villagersItems) {
		this.villagersItems = villagersItems;
	}

	public UUID getCreator() {
		return creator;
	}

	public void setCreator(UUID creator) {
		this.creator = creator;
	}

	public Location getLobbyLocation() {
		return lobbyLocation;
	}

	public void setLobbyLocation(Location lobbyLocation) {
		this.lobbyLocation = lobbyLocation;
	}

	public HashMap<String, Object> getVillagers() {
		return villagers;
	}

	public void setVillagers(HashMap<String, Object> villagers) {
		this.villagers = villagers;
	}

	public ArrayList<String> getContracts() {
		return contracts;
	}

	public void setContracts(ArrayList<String> contracts) {
		this.contracts = contracts;
	}

	public void addContract(Contract contract) {
		contracts.add(contract.getID());
	}

	public Boolean IsEnabled() {
		return isEnabled;
	}

	public void setEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public Boolean IsStarting() {
		return isStarting;
	}

	public void setStarting(Boolean isStarting) {
		this.isStarting = isStarting;
	}

	public Boolean isStarted() {
		return isStarted;
	}

	public void setStarted(Boolean isStarted) {
		this.isStarted = isStarted;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public Integer getChestCount() {
		return chestCount;
	}

	public void setChestCount(Integer chestCount) {
		this.chestCount = chestCount;
	}

	public Integer getVillagersCount() {
		return villagersCount;
	}

	public void setVillagersCount(Integer villagersCount) {
		this.villagersCount = villagersCount;
	}

	public Integer getTablesCount() {
		return tablesCount;
	}

	public void setTablesCount(Integer tablesCount) {
		this.tablesCount = tablesCount;
	}

	public ArrayList<Location> getChestsInGameLocations() {
		return chestsInGameLocations;
	}

	public void setChestsInGameLocations(ArrayList<Location> chestsInGameLocations) {
		this.chestsInGameLocations = chestsInGameLocations;
	}

	public ArrayList<Location> getTablesInGameLocations() {
		return tablesInGameLocations;
	}

	public void setTablesInGameLocations(ArrayList<Location> tablesInGameLocations) {
		this.tablesInGameLocations = tablesInGameLocations;
	}

	public HashMap<Location, Material> getEditedBlocks() {
		return editedBlocks;
	}

	public void setEditedBlocks(HashMap<Location, Material> editedBlocks) {
		this.editedBlocks = editedBlocks;
	}
	
	public String getRandomDeadMessage() 
	{
		String msg = deadMessages.get(main.randomInt(0, deadMessages.size()-1));
		return msg;
	}

	public Chat getLobbyChat() {return lobbyChat;}
	public Chat getSpectatorChat() {return spectatorChat;}
	public Chat getPublicChat() {return publicChat;}

	public boolean isPlayerInLobby(Player p) {return getPlayersInLobby().contains(p.getUniqueId());}
	public boolean isPlayerPlaying(Player p) {return getPlayersInGame().contains(p.getUniqueId());}
	public boolean isPlayerSpectating(Player p) {return getPlayersInSpectators().contains(p.getUniqueId());}

	public ArrayList<Location> getArmorStandsLocations() 
	{
		return armorstandsInGameLocations;
	}

	public String getLever(Location location) 
	{
		String loc = main.convertLocationToString(location);
		return (String) levers.get(loc);
	}

	public Arena getArena() {
		return arena;
	}

	public void setArena(Arena arena) {
		this.arena = arena;
	}

	public HashMap<UUID, Double> getLobbyDamagers() {
		return lobbyDamagers;
	}

	public void setLobbyDamagers(HashMap<UUID, Double> lobbyDamagers) {
		this.lobbyDamagers = lobbyDamagers;
	}

	public ArrayList<Entity> getDrops() {
		return drops;
	}

	public void setDrops(ArrayList<Entity> drops) {
		this.drops = drops;
	}

	public ArrayList<String> getDeadMessages() 
	{
		return deadMessages;
	}

	public HashMap<String, Object> getLevers() 
	{
		return levers;
	}

	public void setDeadMessages(ArrayList<String> arrayList) 
	{
		this.deadMessages = arrayList;
	}

	public void setLevers(HashMap<String, Object> levers) 
	{
		this.levers = levers;
	}

	public void setPlayersStats(HashMap<String, Object> playersStats) 
	{
		this.playersStats = playersStats;
	}

	public void addDrop(Item itemDrop) 
	{
		drops.add(itemDrop);
	}
}