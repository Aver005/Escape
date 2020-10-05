package kiviuly.escape;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class Main extends JavaPlugin implements Listener, CommandExecutor
{
	public static Main main;
	public FileConfiguration config = null;
	private HashMap<String, Arena> arenasByName = new HashMap<>();
	private HashMap<String, User> usersByName = new HashMap<>();
	private HashMap<String, Trader> villagersByName = new HashMap<>();
	private HashMap<String, Contract> contractsByName = new HashMap<>();
	private HashMap<Location, BukkitRunnable> chestRefilRunnables = new HashMap<>();
	
	private ArrayList<String> aliases = new ArrayList<>();
	private ArrayList<String> contractTypes = new ArrayList<>();
	private ArrayList<Location> refiledChests = new ArrayList<>();
	
	@Override
	public void onEnable() 
	{
		aliases.add("ecs"); aliases.add("es"); 
		main = this;
		
		contractTypes.add("KILLS");
		contractTypes.add("ACTIVATE");
		contractTypes.add("MINE");
		contractTypes.add("FIND");
		contractTypes.add("BREAK");
		contractTypes.add("LOOT");
		
		getServer().getPluginManager().registerEvents(new Events(main), this);
		getCommand("escape").setExecutor(new Commands(main));
		getCommand("escape").setAliases(aliases);
		config = this.getConfig();
		
		reloadCFG();
	}
	
	@Override
	public void onDisable() {saveCFG();}

	public FileConfiguration getConfig() {return config;}
	public static Main getMain() {return main;}
	public HashMap<String, Arena> getArenasByName() {return arenasByName;}
	public void setArenasByName(HashMap<String, Arena> arenasByName) {this.arenasByName = arenasByName;}
	public void addArena(String name, Arena arena) {this.arenasByName.put(name, arena);}
	public void addTrader(String name, Trader trader) {this.villagersByName.put(name, trader);}
	public void removeArena(String name) {this.arenasByName.remove(name);}
	public Boolean isArena(String name) {return arenasByName.containsKey(name);}
	public Boolean isTrader(String name) {return villagersByName.containsKey(name);}
	public Arena getArena(String name) {return arenasByName.getOrDefault(name,null);}
	public Trader getTrader(String name) {return villagersByName.getOrDefault(""+name,null);}
	public HashMap<String, Trader> getVillagersByName() {return villagersByName;}
	public void setVillagersByName(HashMap<String, Trader> villagersByName) {this.villagersByName = villagersByName;}
	
	public HashMap<String, User> getUsersByName() {
		return usersByName;
	}

	public void setUsersByName(HashMap<String, User> usersByName) {
		this.usersByName = usersByName;
	}

	public Inventory fillFreeSlots(Inventory inv, short data, String type)
	{
		for(int i = 0; i < inv.getSize(); i++) 
		{
			if (inv.getItem(i) != null) {continue;}
			
			if (type.equals("WALLS") && (i > 9 && i < inv.getSize()-10 && i % 9 != 0 && (i+1) % 9 != 0)) {continue;}
			ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(" ");
			item.setItemMeta(meta);
			inv.setItem(i,item);
		}
		return inv;
	}

	public void reloadCFG()
	{
		File temp = getDataFolder();
		if (!temp.exists()) {temp.mkdir();}
		temp = new File(getDataFolder() + File.separator + "config.yml");
		if (!temp.exists()) {try {
			temp.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}}
		
		config = YamlConfiguration.loadConfiguration(temp);
		
		temp = new File(this.getDataFolder() + File.separator + "Arenas");
		if (!temp.exists()) {temp.mkdir();}
		
		for(File f : temp.listFiles())
		{
			if (!f.getName().endsWith(".arena")) {continue;}
			
			try 
			{
				ObjectInputStream ois = new BukkitObjectInputStream(new FileInputStream(f));
				Arena arena = (Arena) ois.readObject();
				ois.close();
				getLogger().info("Arena "+f.getName().replace(".arena", "")+" is loaded!");
				arenasByName.put(f.getName().replace(".arena", ""), arena);
			} 
			catch (ClassNotFoundException | IOException e) {e.printStackTrace();}
		}
		
		temp = new File(this.getDataFolder() + File.separator + "Traders");
		if (!temp.exists()) {temp.mkdir();}
		
		for(File f : temp.listFiles())
		{
			if (!f.getName().endsWith(".trader")) {continue;}
			
			try 
			{
				ObjectInputStream ois = new BukkitObjectInputStream(new FileInputStream(f));
				Trader trader = (Trader) ois.readObject();
				ois.close();
				getLogger().info("Trader "+f.getName().replace(".trader", "")+" is loaded!");
				villagersByName.put(f.getName().replace(".trader", ""), trader);
			} 
			catch (ClassNotFoundException | IOException e) {e.printStackTrace();}
		}
		
		temp = new File(getDataFolder() + File.separator + "Users");
		if (!temp.exists()) {temp.mkdir();}
		
		for(File f : temp.listFiles())
		{
			if (!f.getName().endsWith(".user")) {continue;}
			
			try 
			{
				ObjectInputStream ois = new BukkitObjectInputStream(new FileInputStream(f));
				User user = (User) ois.readObject();
				ois.close();
				getLogger().info("User "+f.getName().replace(".user", "")+" is loaded!");
				usersByName.put(f.getName().replace(".user", ""), user);
			} 
			catch (ClassNotFoundException | IOException e) {e.printStackTrace();}
		}
		
		temp = new File(getDataFolder() + File.separator + "Contracts");
		if (!temp.exists()) {temp.mkdir();}
		
		for(File f : temp.listFiles())
		{
			if (!f.getName().endsWith(".contract")) {continue;}
			
			try 
			{
				ObjectInputStream ois = new BukkitObjectInputStream(new FileInputStream(f));
				Contract user = (Contract) ois.readObject();
				ois.close();
				getLogger().info("Contract "+f.getName().replace(".contract", "")+" is loaded!");
				contractsByName.put(f.getName().replace(".contract", ""), user);
			} 
			catch (ClassNotFoundException | IOException e) {e.printStackTrace();}
		}
	}
	
	public void saveCFG()
	{
		File temp = new File(getDataFolder() + File.separator + "config.yml");
		if (!temp.exists()) {try {temp.createNewFile();} catch (IOException e) {e.printStackTrace();}}
		try {config.save(temp);} catch (IOException e) {e.printStackTrace();}
		getLogger().info("Config Saved!");
		
		if (!arenasByName.isEmpty())
		{
			temp = new File(getDataFolder() + File.separator + "Arenas");
			if (!temp.exists()) {temp.mkdir();}
			for(String s : arenasByName.keySet())
			{
				File f = new File(getDataFolder() + File.separator + "Arenas" + File.separator + s + ".arena");
				if (!f.getName().endsWith(".arena")) {continue;}
				
				try 
				{
					ObjectOutputStream ois = new BukkitObjectOutputStream(new FileOutputStream(f));
					ois.writeObject(arenasByName.get(s)); ois.flush(); ois.close();
				} 
				catch (IOException e) {e.printStackTrace();}
			}
			
			getLogger().info("ARENAS Saved!");
		}
		
		if (!villagersByName.isEmpty())
		{
			temp = new File(getDataFolder() + File.separator + "Traders");
			if (!temp.exists()) {temp.mkdir();}
			for(String s : villagersByName.keySet())
			{
				File f = new File(getDataFolder() + File.separator + "Traders" + File.separator + s + ".trader");
				if (!f.getName().endsWith(".trader")) {continue;}
				
				try 
				{
					ObjectOutputStream ois = new BukkitObjectOutputStream(new FileOutputStream(f));
					ois.writeObject(villagersByName.get(s)); ois.flush(); ois.close();
				} 
				catch (IOException e) {e.printStackTrace();}
			}
			
			getLogger().info("TRADERS Saved!");
		}
		
		if (!usersByName.isEmpty())
		{
			temp = new File(getDataFolder() + File.separator + "Users");
			if (!temp.exists()) {temp.mkdir();}
			for(String s : usersByName.keySet())
			{
				File f = new File(getDataFolder() + File.separator + "Users" + File.separator + s + ".user");
				if (!f.getName().endsWith(".user")) {continue;}
				
				try 
				{
					ObjectOutputStream ois = new BukkitObjectOutputStream(new FileOutputStream(f));
					ois.writeObject(usersByName.get(s)); ois.flush(); ois.close();
				} 
				catch (IOException e) {e.printStackTrace();}
			}
			
			getLogger().info("USERS Saved!");
		}
		
		if (!usersByName.isEmpty())
		{
			temp = new File(getDataFolder() + File.separator + "Contracts");
			if (!temp.exists()) {temp.mkdir();}
			for(String s : contractsByName.keySet())
			{
				File f = new File(getDataFolder() + File.separator + "Contracts" + File.separator + s + ".contract");
				if (!f.getName().endsWith(".contract")) {continue;}
				
				try 
				{
					ObjectOutputStream ois = new BukkitObjectOutputStream(new FileOutputStream(f));
					ois.writeObject(contractsByName.get(s)); ois.flush(); ois.close();
				} 
				catch (IOException e) {e.printStackTrace();}
			}
			
			getLogger().info("CONTRACTS Saved!");
		}
	}

	public boolean isPlayerInGame(Player p) 
	{
		User user = usersByName.getOrDefault(p.getName(), null);
		if (user == null) {return false;}
		return user.isPlaying();
	}
	
	public boolean isPlayerWaiting(Player p) 
	{
		User user = usersByName.getOrDefault(p.getName(), null);
		if (user == null) {return false;}
		return user.isWaiting();
	}

	public Arena getPlayerArena(Player p) 
	{
		User user = usersByName.getOrDefault(p.getName(), null);
		if (user == null) {return null;}
		return user.getArena();
	}

	public Trader getTraderByDisplayName(String name) 
	{
		for(String s : villagersByName.keySet())
		{
			Trader trader = villagersByName.get(s);
			if (trader.getName().equals(name)) {return trader;}
		}
		
		return null;
	}
	
	public int getAmountOfMaterial(Player p, Material mat)
	{
		int amount = 0;
		Inventory inv = p.getInventory();
		for(ItemStack is : inv.getContents())
		{
			if (is == null) {continue;}
			if (is.getType().equals(mat))
			{
				amount += is.getAmount();
			}
		}
		return amount;
	}
	
	public void takeMaterial(Material mat, int amount, Player p)
	{
		Inventory inv = p.getInventory();
		for(ItemStack is : inv.getContents())
		{
			if (is == null) {continue;}
			if (is.getType().equals(mat))
			{
				if (is.getAmount() >= amount) 
				{
					is.setAmount(is.getAmount()-amount);
					amount = 0;
					return;
				}
				else
				{
					amount -= is.getAmount();
					is.setAmount(0);
				}
			}
		}
	}

	public static String randomString(int targetStringLength) 
	{
	    int leftLimit = 48; // numeral '0'
	    int rightLimit = 122; // letter 'z'
	    Random random = new Random();
	 
	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(targetStringLength)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();
	 
	    return generatedString;
	}
	
	public int randomInt(int min, int max) 
	{
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}

	public ArrayList<String> getContractTypes() {
		return contractTypes;
	}

	public HashMap<String, Contract> getContractsByName() {
		return contractsByName;
	}

	public void setContractsByName(HashMap<String, Contract> contractsByName) {
		this.contractsByName = contractsByName;
	}
	
	public void addContract(String name, Contract contract) {
		this.contractsByName.put(name, contract);
	}
	
	public boolean isContract(String name) {
		return contractsByName.containsKey(name);
	}
	
	public void savePlayerData(Player p)
	{
		File temp = new File(getDataFolder() + File.separator + "Players");
		if (!temp.exists()) {temp.mkdir();}
		
		temp = new File(getDataFolder() + File.separator + "Players" + File.separator + p.getUniqueId() + ".data");
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("Location", p.getLocation());
		data.put("DisplayName", p.getDisplayName());
		data.put("InventoryContents", p.getInventory().getContents());
		data.put("ArmorContents", p.getInventory().getArmorContents());
		data.put("HP", p.getHealth());
		data.put("FOOD", p.getFoodLevel());
		data.put("isFlying", p.isFlying());
		data.put("GameMode", p.getGameMode());
		data.put("PotionEffects", p.getActivePotionEffects());
		data.put("LEVEL", p.getLevel());
		data.put("EXP", p.getExp());
		data.put("WalkSpeed", p.getWalkSpeed());
		data.put("FlySpeed", p.getFlySpeed());
		data.put("AllowFlight", p.getAllowFlight());
		
		try 
		{
			ObjectOutputStream ois = new BukkitObjectOutputStream(new FileOutputStream(temp));
			ois.writeObject(data); ois.flush(); ois.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void loadPlayerData(Player p)
	{
		File temp = new File(getDataFolder() + File.separator + "Players");
		
		temp = new File(getDataFolder() + File.separator + "Players" + File.separator + p.getUniqueId() + ".data");
		if (temp.exists())
		{
			try 
			{
				ObjectInputStream ois = new BukkitObjectInputStream(new FileInputStream(temp));
				HashMap<String, Object> data = (HashMap<String, Object>) ois.readObject();
				ois.close();
				
				p.setDisplayName((String) data.getOrDefault("DisplayName", p.getDisplayName()));
				p.getInventory().setContents((ItemStack[]) data.getOrDefault("InventoryContents", p.getInventory().getContents()));
				p.getInventory().setArmorContents((ItemStack[]) data.getOrDefault("ArmorContents", p.getInventory().getArmorContents()));
				p.setHealth((double) data.getOrDefault("HP", p.getHealth()));
				p.setFoodLevel((int) data.getOrDefault("FOOD", p.getFoodLevel()));
				p.setGameMode((GameMode) data.getOrDefault("GameMode", p.getGameMode()));
				p.addPotionEffects((Collection<PotionEffect>) data.getOrDefault("PotionEffects", p.getActivePotionEffects()));
				p.setLevel((int) data.getOrDefault("LEVEL", p.getLevel()));
				p.setExp((float) data.getOrDefault("EXP", p.getExp()));
				p.setWalkSpeed((float) data.getOrDefault("WalkSpeed", p.getWalkSpeed()));
				p.setFlySpeed((float) data.getOrDefault("FlySpeed", p.getFlySpeed()));
				p.teleport((Location) data.getOrDefault("Location", p.getBedSpawnLocation()));
				p.setAllowFlight((boolean) data.getOrDefault("AllowFlight", p.getAllowFlight()));
				if (p.getAllowFlight()) {p.setFlying((boolean) data.getOrDefault("isFlying", p.isFlying()));}
			} 
			catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
		}
	}
	
	public void clearPlayer(Player p)
	{
		p.setDisplayName(p.getName());
		p.getInventory().setArmorContents(null);
		p.getInventory().clear();
		p.setMaxHealth(20);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setGameMode(GameMode.ADVENTURE);
		p.setLevel(60);
		p.setExp(0);
		p.setFlying(false);
		p.setWalkSpeed(0.2F);
		p.setFlySpeed(0.2F);
		for(PotionEffect pi : p.getActivePotionEffects()) {p.removePotionEffect(pi.getType());}
	}

	public void addUser(String name, User user) 
	{
		usersByName.put(name, user);
	}

	public void generateChestLoot(Chest chest, Arena arena) 
	{
		ArrayList<String> contracts = arena.getContracts();
		ArrayList<ItemStack> chestItems = arena.getChestItems();
		
		for(int j = 0; j < main.randomInt(2, 4); j++)
		{
			ItemStack item = null;
			int slot = main.randomInt(0, 26);
			while(chest.getInventory().getItem(slot) != null) {slot = main.randomInt(0, 26);}
			if (main.randomInt(0, 3) == 0 && contracts.size() != 0)
			{
				int rndItem = main.randomInt(0, contracts.size()-1);
				String contractName = contracts.get(rndItem);
				Contract contract = contractsByName.get(contractName);
				item = new ItemBuilder(Material.PAPER, "§6Çàäàíèå")
					.lore("§8§k"+contract.getID()) //0
					.lore("")
					.lore("§f"+contract.getDescription())
					.lore("§fÏðîãðåññ: §e0")
					.lore("§fÖåíà: §e"+contract.getPrice())
					.lore("")
					.lore("§8§k"+arena.getID()+"/"+contract.getIdle()+"/"+j+slot)
					.build();
			}
			else
			{
				if (chestItems.size() == 0) {continue;}
				int rndItem = main.randomInt(0, chestItems.size()-1);
				item = chestItems.get(rndItem);
			}
			
			chest.getInventory().setItem(slot, item);
		}
	}
	
	public Block getLookingChest(Player p, int range) 
	{
        BlockIterator iter = new BlockIterator(p, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) 
        {
	        lastBlock = iter.next();
	        if (lastBlock.getType() == Material.CHEST) {return lastBlock;}
        }
        
        return null;
    }

	public String convertLocationToString(Location l) 
	{
		return l.getBlockX()+"/"+l.getBlockY()+"/"+l.getBlockZ()+"/"+l.getWorld().getName();
	}

	public Location convertStringToLocation(String s) 
	{
		Location l = new Location(
			Bukkit.getWorld(s.split("/")[3]), 
			Integer.parseInt(s.split("/")[0]), 
			Integer.parseInt(s.split("/")[1]), 
			Integer.parseInt(s.split("/")[2]));
		return l;
	}

	public ArrayList<Location> getRefiledChests() {
		return refiledChests;
	}

	public void setRefiledChests(ArrayList<Location> refilledChests) {
		this.refiledChests = refilledChests;
	}

	public HashMap<Location, BukkitRunnable> getChestRefilRunnables() {
		return chestRefilRunnables;
	}

	public void setChestRefilRunnables(HashMap<Location, BukkitRunnable> chestRefilRunnables) {
		this.chestRefilRunnables = chestRefilRunnables;
	}
	
	public void addChestRifilRunnable(Location l, BukkitRunnable br)
	{
		chestRefilRunnables.put(l, br);
	}
	
	public void removeChestRifilRunnable(Location l)
	{
		chestRefilRunnables.remove(l);
	}

	public void OpenGameMenu(Player p)
	{
		Inventory inv = Bukkit.createInventory(null, 54, "§8§lEscape");
		inv = fillFreeSlots(inv, (short)15, "WALLS");
		ArrayList<Arena> startedArenas = new ArrayList<>();
		for(String s : arenasByName.keySet())
		{
			Arena arena = arenasByName.get(s);
			if (!arena.IsEnabled()) {continue;}
			if (arena.isStarted()) {startedArenas.add(arena); continue;}
			ItemStack is = new ItemBuilder(Material.WOOL)
				.damage((short)5).lore("")
				.lore("§fÈãðîêîâ: §2"+arena.getPlayersInLobby().size()+" / "+arena.getMaxPlayers())
				.lore("§fÎïèñàíèå:")
				.lore("§e"+arena.getDescription())
				.lore("").lore("§2§lÍàæìèòå, §2÷òîáû âîéòè")
				.displayname(arena.getName()).build();
			
			inv.addItem(is);
		}
		p.openInventory(inv);
	}

	public Arena getArenaByDisplayName(String displayName) 
	{
		for(String s : arenasByName.keySet())
		{
			Arena arena = arenasByName.get(s);
			if (arena == null) {continue;}
			if (arena.getName().equals(displayName)) {return arena;}
		} 
		return null;
	}
}