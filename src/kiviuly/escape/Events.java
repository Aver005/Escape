package kiviuly.escape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Events implements Listener 
{
	private Main main;
	public Events(Main main) {this.main = main;}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e)
	{
		Player p = e.getPlayer();
		Block b = e.getBlock();
		if (main.isPlayerInGame(p)) {e.setCancelled(true);}
		ItemStack item = e.getItemInHand();
		if (item == null) {return;}
		if (!item.hasItemMeta()) {return;}
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasDisplayName()) {return;}
		
		if (meta.getDisplayName().startsWith("§eПоставьте, чтобы добавить"))
		{
			List<String> lore = meta.getLore();
			String arenaName = lore.get(0).replace("§fАрена: ", "");
			if (!main.isArena(arenaName)) {return;}
			Arena arena = main.getArena(arenaName);
			if (arena == null) {return;}
			e.setCancelled(true);
			Location loc = b.getLocation();
			String type = "";
			
			if (b.getType().equals(Material.BEACON)) {arena.addSpawnLocation(loc); type = "ИГРОКОВ";} else
			if (b.getType().equals(Material.WORKBENCH)) {arena.addVillager(loc, lore.get(1).replace("§fТип: ", "")); type = "ЖИТЕЛЕЙ";} else
			if (b.getType().equals(Material.LEVER)) {arena.addLever(loc, lore.get(1).replace("§fТип: ", "")); type = "РЫЧАГОВ"; e.setCancelled(false);} else
			if (b.getType().equals(Material.STONE)) {arena.addOreLocation(loc); type = "РУД"; e.setCancelled(false);} else
			if (b.getType().equals(Material.ENCHANTMENT_TABLE)) {arena.addTableLocation(loc); type = "СТОЛОВ";} else
			if (b.getType().equals(Material.CHEST)) {arena.addChestLocation(loc); type = "СУНДУКОВ";}
			else {p.sendMessage("§cПредмет неверный. Удалите его."); return;}
			
			p.sendMessage("§2Спавн §a"+type+"§2 добавлен: §e"+loc.getBlockX()+" "+loc.getBlockY()+" "+loc.getBlockZ()+" "+loc.getWorld().getName()+"§2.");
			return;
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e)
	{
		if (e.getInventory() == null) {return;}
		if (e.getView() == null) {return;}
		
		String title = e.getView().getTitle();
		
		if (title.equals("§8§lEscape"))
		{
			Player p = (Player) e.getWhoClicked();
			ItemStack is = e.getCurrentItem();
			e.setCancelled(true);
			if (is == null) {return;}
			if (is.getType().equals(Material.STAINED_GLASS_PANE)) {return;}
			
			ItemMeta meta = is.getItemMeta();
			if (is.getType().equals(Material.WOOL))
			{
				if (is.getData().getData() == 5)
				{
					Arena arena = main.getArenaByDisplayName(meta.getDisplayName());
					if (arena == null) {p.sendMessage("§cОшибка. Данная арена не найдена."); main.OpenGameMenu(p); return;}
					
					User user = main.getUsersByName().getOrDefault(p.getName(), null);
					if (user == null) 
					{
						user = new User(p); 
						HashMap<String, User> users = main.getUsersByName();
						users.put(p.getName(), user);
						main.setUsersByName(users);
					}
					
					if (arena.addPlayerToGame(user)) 
					{
						user.setArena(arena);
						user.setWaiting(true);
					}
					else {p.sendMessage("§eДанная арена заполнена. Попробуйте другую :(");}
				}
			}
		}
		
		if (title.startsWith("§8Добавление нового трейда: "))
		{
			Player p = (Player) e.getWhoClicked();
			String traderName = title.replace("§8Добавление нового трейда: ", "");
			if (!main.isTrader(traderName)) {p.closeInventory(); p.sendMessage("§cТакой трейдер не найден."); return;}
			Trader trader = main.getTrader(traderName);
			ItemStack item = e.getCurrentItem();
			if (item == null) {return;}
			if (!item.hasItemMeta()) {return;}
			if (!item.getItemMeta().hasDisplayName()) {return;}
			String itemName = item.getItemMeta().getDisplayName();
			ItemStack goldItem = e.getInventory().getItem(12);
			ItemMeta goldMeta = goldItem.getItemMeta();
			ItemStack resultItem = e.getInventory().getItem(16);
			
			switch (itemName)
			{
				case "§2Добавить 1":
					e.setCancelled(true);
					if (goldItem.getAmount() + 1 > 255) {p.sendMessage("§cДостигнута максимальная цена.");return;}
					goldItem.setAmount(goldItem.getAmount()+1);
					goldMeta.setDisplayName("§6Стоимость: §e"+goldItem.getAmount());
					goldItem.setItemMeta(goldMeta);
					e.getInventory().setItem(12, goldItem);
				break;
				
				case "§2Добавить 10":
					e.setCancelled(true);
					if (goldItem.getAmount() + 10 > 255) {p.sendMessage("§cДостигнута максимальная цена.");return;}
					goldItem.setAmount(goldItem.getAmount()+10);
					goldMeta.setDisplayName("§6Стоимость: §e"+goldItem.getAmount());
					goldItem.setItemMeta(goldMeta);
					e.getInventory().setItem(12, goldItem);
				break;
				
				case "§eУбрать 1":
					e.setCancelled(true);
					if (goldItem.getAmount() - 1 < 1) {p.sendMessage("§cДостигнута минимальная цена.");return;}
					goldItem.setAmount(goldItem.getAmount()-1);
					goldMeta.setDisplayName("§6Стоимость: §e"+goldItem.getAmount());
					goldItem.setItemMeta(goldMeta);
					e.getInventory().setItem(12, goldItem);
				break;
				
				case "§eУбрать 10":
					if (goldItem.getAmount() - 10 < 1) {p.sendMessage("§cДостигнута минимальная цена.");return;}
					goldItem.setAmount(goldItem.getAmount()-10);
					goldMeta.setDisplayName("§6Стоимость: §e"+goldItem.getAmount());
					goldItem.setItemMeta(goldMeta);
					e.getInventory().setItem(12, goldItem);
					e.setCancelled(true);
				break;
				
				case "§cОТМЕНИТЬ":
					e.setCancelled(true);
					p.closeInventory();
				break;
				
				case "§2СОХРАНИТЬ":
					e.setCancelled(true);
					if (resultItem == null) {p.sendMessage("§cВы не положили предмет для покупки."); return;}
					trader.addTrade(goldItem.getAmount(), resultItem);
					p.closeInventory();
					p.sendMessage("§2Трейд сохранён для трейдера §a"+trader.getID()+"§2.");
				break;
			}
		}
		
		if (title.startsWith("§9§lМагазин "))
		{
			Player p = (Player) e.getWhoClicked();
			String traderName = title.replace("§9§lМагазин ", "");
			Trader trader = main.getTraderByDisplayName(traderName);
			if (trader == null) {return;}
			ItemStack item = e.getCurrentItem().clone();
			e.setCancelled(true);
			if (item == null) {return;}
			if (e.getSlot() > 9 && e.getSlot() < e.getInventory().getSize()-10 && e.getSlot() % 9 != 0 && (e.getSlot()+1) % 9 != 0)
			{
				if (!item.hasItemMeta()) {return;}
				ItemMeta meta = item.getItemMeta();
				if (!meta.hasLore()) {return;}
				List<String> lore = meta.getLore();
				if (lore.size() < 2) {return;}
				Arena arena = main.getPlayerArena(p);
				int price = Integer.parseInt(lore.get(lore.size()-1).replace("§fЦена: §e", "").replace(" §6золота", ""));
				int amount = main.getAmountOfMaterial(p, Material.GOLD_INGOT);
				if (price > amount) {p.sendMessage("§cУ вас не хватает золота..."); return;}
				if (p.getInventory().firstEmpty() == -1) {p.sendMessage("§cУ вас не хватает места в инвентаре..."); return;}
				main.takeMaterial(Material.GOLD_INGOT, price, p);
				lore.remove(lore.size()-1); lore.remove(lore.size()-1); 
				meta.setLore(lore);
				item.setItemMeta(meta);
				p.getInventory().addItem(item);
				arena.addStat(p, "TradesCompleted", 1);
				p.sendMessage("§2Предмет куплен. §e-"+price+" §6золота. §2У вас осталось §e"+main.getAmountOfMaterial(p, Material.GOLD_INGOT)+" §6золота");
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e)
	{
		if (e.getInventory() == null) {return;}
		if (e.getView() == null) {return;}
		
		String title = e.getView().getTitle();
		
		if (title.startsWith("§8Редактор предметов арены "))
		{
			Player p = (Player) e.getPlayer();
			String arenaName = title.split(" ")[3];
			Arena arena = main.getArena(arenaName);
			if (arena == null) {return;}
			ArrayList<ItemStack> arr = new ArrayList<>();
			for(ItemStack is : e.getInventory().getContents()) {if (is == null) {return;} arr.add(is);}
			arena.setChestItems(arr);
			p.sendMessage("§2Предметы для сундуков для арены §b"+arenaName+"§2 сохранены.");
		}
		
		if (title.equals("container.chest"))
		{
			Player p = (Player) e.getPlayer();
			if (!main.isPlayerInGame(p)) {return;}
			Arena arena = main.getPlayerArena(p);
			
			// CONTRACT
			if (p.getInventory().contains(Material.PAPER))
			{
				for(ItemStack item : p.getInventory().getContents())
				{
					if (item == null) {continue;}
					if (item.getType().equals(Material.AIR)) {continue;}
					if (item.getType().equals(Material.PAPER)) {continue;}
					
					for(ItemStack is : p.getInventory().getContents())
					{
						if (is == null) {continue;}
						if (!is.getType().equals(Material.PAPER)) {continue;}
						if (!is.hasItemMeta()) {continue;}
						ItemMeta meta = is.getItemMeta();
						if (!meta.hasDisplayName()) {continue;}
						if (!meta.hasLore()) {continue;}
						String name = meta.getDisplayName();
						if (!name.equals("§6Задание")) {continue;}
						
						List<String> lore = meta.getLore();
						String contractName = ChatColor.stripColor(lore.get(0));
						if (!main.isContract(contractName)) {continue;}
						Contract contract = main.getContractsByName().get(contractName);
						if (!contract.getType().equals("FIND")) {continue;}
						if (Integer.parseInt(contract.getIdle()) != item.getTypeId()) {continue;}
						int progress = Integer.parseInt(lore.get(3).replace("§fПрогресс: §e", "")) + 1;
						lore.set(3, "§fПрогресс: §e"+progress);
						meta.setLore(lore);
						is.setItemMeta(meta);
						p.sendMessage("§2Контракт: §b"+contract.getDisplayName()+" §2Прогресс: §e"+progress+"/"+contract.getAmount());
						
						if (progress == contract.getAmount())
						{
							arena.addStat(p, "QuestsCompleted", 1);
							p.sendMessage("§8=====================================");
							p.sendMessage("§2 Контракт выполнен. Золото получено.");
							p.sendMessage("§8=====================================");
							
							ItemStack gold = new ItemStack(Material.GOLD_INGOT, contract.getPrice());
							p.getInventory().remove(is);
							p.getInventory().remove(item);
							p.getInventory().addItem(gold);
						}
						
						break;
					}
				}
			}
			
			// REFIL
			Block b = main.getLookingChest(p, 10);
			if (b == null) {return;}
			if (!b.getType().equals(Material.CHEST)) {return;}
			
			Chest chest = (Chest) b.getState();
			
			Chunk chunk = b.getChunk();
			ArmorStand as = null;
			
			if (chunk.getEntities().length > 0)
			{
				for(Entity ent : chunk.getEntities())
				{
					if (ent == null) {continue;}
					if (!ent.getType().equals(EntityType.ARMOR_STAND)) {continue;}
					
					Location entL = ent.getLocation();
					Location bL = b.getLocation();
					
					if (entL.getBlockX() == bL.getBlockX() && entL.getBlockZ() == bL.getBlockZ() && entL.getBlockY() == (bL.getBlockY()-1))
					{
						as = (ArmorStand) ent;
					}
				}
			}
			
			if (as == null) {return;}
			int freeSlots = 0;
			for(int i = 0; i < chest.getInventory().getSize(); i ++) {if (chest.getInventory().getItem(i) == null) {freeSlots++;}}
			if (freeSlots < chest.getInventory().getSize()) {as.setCustomNameVisible(false); return;}
			
			as.setCustomName("§eПустой"); as.setCustomNameVisible(true);
			if (main.getRefiledChests().contains(b.getLocation())) {return;}
			
			ArmorStand armorstand = as;
			ArrayList<Location> refLocs = main.getRefiledChests();
			refLocs.add(b.getLocation());
			main.setRefiledChests(refLocs);
			
			BukkitRunnable timer = new BukkitRunnable() 
			{
				@Override
				public void run() 
				{
					armorstand.setCustomNameVisible(true);
					armorstand.setCustomName("§2Refilled");
					main.generateChestLoot(chest, arena);
					
					ArrayList<Location> refLocs = main.getRefiledChests();
					refLocs.remove(b.getLocation());
					main.setRefiledChests(refLocs);
				}
			};
			
			timer.runTaskLater(main, 60 * 3 * 20L);
			main.addChestRifilRunnable(b.getLocation(), timer);
		}
	}
	
	@EventHandler
	public void onClickTrader(PlayerInteractEntityEvent e)
	{
		Player p = e.getPlayer();
		if (!main.isPlayerInGame(p)) {return;}
		Entity ent = e.getRightClicked();
		if (!ent.getType().equals(EntityType.VILLAGER)) {return;}
		if (!ent.isCustomNameVisible()) {return;}
		if (ent.getName() == null) {return;}
		String name = ent.getName();
		if (name.isEmpty()) {return;}
		Trader trader = main.getTraderByDisplayName(name);
		if (trader == null) {return;}
		e.setCancelled(true);
		
		int slots = trader.getTrades().keySet().size()/7 + 3;
		Inventory inv = Bukkit.createInventory(null, slots*9, "§9§lМагазин "+name);
		inv = main.fillFreeSlots(inv, (short) 10, "WALLS");
		int i = 10;
		for(ItemStack item : trader.getTrades().keySet())
		{
			if ((i+1)%9 == 0) {i++;}
			if (i%9 == 0) {i++;}
			int price = trader.getTrades().get(item);
			ItemStack is = item.clone();
			ItemMeta meta = is.getItemMeta();
			List<String> lore = new ArrayList<String>();
			if (meta.hasLore()) {lore = meta.getLore();}
			lore.add(""); lore.add("§fЦена: §e"+price+" §6золота");
			meta.setLore(lore);
			is.setItemMeta(meta);
			inv.setItem(i, is);
			i++;
		}
		
		p.openInventory(inv);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		Block b = e.getBlock();
		//Location loc = b.getLocation();
		if (!main.isPlayerInGame(p)) {return;}
		Arena arena = main.getPlayerArena(p);
		
		if (main.isPlayerInGame(p)) {if (!b.getType().name().toLowerCase().endsWith("_ore") && !b.getType().equals(Material.MONSTER_EGGS)) 
		{e.setCancelled(true);} else {e.setDropItems(false);}}
		if (b.getType().equals(Material.IRON_FENCE))
		{
			if (arena != null) {
				HashMap<Location, Material> edBlocks = arena.getEditedBlocks();
				edBlocks.put(b.getLocation(), b.getType());
				arena.setEditedBlocks(edBlocks);
				e.setCancelled(false);
				e.setDropItems(false);
			}
		}
		
		if (p.getInventory().contains(Material.PAPER))
		{
			for(ItemStack is : p.getInventory().getContents())
			{
				if (is == null) {continue;}
				if (!is.getType().equals(Material.PAPER)) {continue;}
				if (!is.hasItemMeta()) {continue;}
				ItemMeta meta = is.getItemMeta();
				if (!meta.hasDisplayName()) {continue;}
				if (!meta.hasLore()) {continue;}
				String name = meta.getDisplayName();
				if (!name.equals("§6Задание")) {continue;}
				
				List<String> lore = meta.getLore();
				String contractName = ChatColor.stripColor(lore.get(0));
				if (!main.isContract(contractName)) {continue;}
				Contract contract = main.getContractsByName().get(contractName);
				if (!contract.getType().equals("MINE") && !contract.getType().equals("BREAK")) {continue;}
				if (Integer.parseInt(contract.getIdle()) != b.getTypeId()) {continue;}
				int progress = Integer.parseInt(lore.get(3).replace("§fПрогресс: §e", "")) + 1;
				lore.set(3, "§fПрогресс: §e"+progress);
				meta.setLore(lore);
				is.setItemMeta(meta);
				p.sendMessage("§2Контракт: §b"+contract.getDisplayName()+" §2Прогресс: §e"+progress+"/"+contract.getAmount());
				
				if (progress == contract.getAmount())
				{
					arena.addStat(p, "QuestsCompleted", 1);
					p.sendMessage("§8=====================================");
					p.sendMessage("§2 Контракт выполнен. Золото получено.");
					p.sendMessage("§8=====================================");
					
					ItemStack gold = new ItemStack(Material.GOLD_INGOT, contract.getPrice());
					p.getInventory().remove(is);
					p.getInventory().addItem(gold);
				}
				
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlayerKillPlayer(EntityDamageByEntityEvent e)
	{
		if (e.getDamager() instanceof Player) 
		{
			Player killer = (Player) e.getDamager();
			Arena arena = main.getPlayerArena(killer);
			if (arena == null) {return;}
			
			if (e.getEntityType().equals(EntityType.ITEM_FRAME)) {e.setCancelled(true);}
			if (e.getEntityType().equals(EntityType.PLAYER))
			{
				Player p = (Player) e.getEntity();
				
				if (arena.isPlayerInLobby(p) && arena.isPlayerInLobby(killer)) 
				{
					HashMap<UUID, Double> lobbyDamagers = arena.getLobbyDamagers();
					double dmg = e.getDamage() + lobbyDamagers.getOrDefault(killer.getUniqueId(), 0.0);
					lobbyDamagers.put(killer.getUniqueId(), dmg);
					arena.setLobbyDamagers(lobbyDamagers);
					e.setDamage(0);
					return;
				}
				
				if (!main.isPlayerInGame(p) || !main.isPlayerInGame(killer)) {return;}
				if (e.getDamage() < p.getHealth()) {return;}
				arena.addStat(killer, "Kills", 1);
				
				for(ItemStack is : killer.getInventory().getContents())
				{
					if (is == null) {continue;}
					if (!is.getType().equals(Material.PAPER)) {continue;}
					if (!is.hasItemMeta()) {continue;}
					ItemMeta meta = is.getItemMeta();
					if (!meta.hasDisplayName()) {continue;}
					if (!meta.hasLore()) {continue;}
					String name = meta.getDisplayName();
					if (!name.equals("§6Задание")) {continue;}
					
					List<String> lore = meta.getLore();
					String contractName = ChatColor.stripColor(lore.get(0));
					if (!main.isContract(contractName)) {continue;}
					Contract contract = main.getContractsByName().get(contractName);
					if (!contract.getType().equals("KILLS")) {continue;}
					int progress = Integer.parseInt(lore.get(3).replace("§fПрогресс: §e", "")) + 1;
					lore.set(3, "§fПрогресс: §e"+progress);
					meta.setLore(lore);
					is.setItemMeta(meta);
					p.sendMessage("§2Контракт: §b"+contract.getDisplayName()+" §2Прогресс: §e"+progress+"/"+contract.getAmount());
					
					if (progress == contract.getAmount())
					{
						p.sendMessage("§8=====================================");
						p.sendMessage("§2 Контракт выполнен. Золото получено.");
						p.sendMessage("§8=====================================");
						
						ItemStack gold = new ItemStack(Material.GOLD_INGOT, contract.getPrice());
						p.getInventory().remove(is);
						p.getInventory().addItem(gold);
					}
					
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		Player p = e.getPlayer();
		String msg = e.getMessage();
		
		Arena arena = main.getPlayerArena(p);
		if (arena == null) {return;}
		
		if (arena.isPlayerInLobby(p))
		{
			e.setCancelled(true);
			Chat chat = arena.getLobbyChat();
			chat.SendMessage(msg, p);
		}
		
		if (arena.isPlayerPlaying(p))
		{
			e.setCancelled(true);
			Chat chat = arena.getPublicChat();
			chat.SendMessage(msg, p);
		}
		
		if (arena.isPlayerSpectating(p))
		{
			e.setCancelled(true);
			Chat chat = arena.getSpectatorChat();
			chat.SendMessage(msg, p);
		}
	}
	
	@EventHandler
	public void onPlayerUseCommand(PlayerCommandPreprocessEvent e)
	{
		Player p = e.getPlayer();
		String msg = e.getMessage();
		
		Arena arena = main.getPlayerArena(p);
		if (arena == null) {return;}
		
		if (arena.isPlayerInLobby(p) || arena.isPlayerPlaying(p) || arena.isPlayerSpectating(p))
		{
			e.setCancelled(true);
			if (msg.endsWith("leave") || msg.startsWith("es stop") || msg.endsWith("start"))  
			{e.setCancelled(false);}
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e)
	{
		if (e.getEntity() instanceof Player)
		{
			Player p = (Player) e.getEntity();
			Arena arena = main.getPlayerArena(p);
			if (arena == null) {return;}
			if (!main.isPlayerInGame(p)) {return;}
			
			if (e.getDamage() >= p.getHealth())
			{
				Location deathLoc = p.getLocation().clone();
				
				for(ItemStack is : p.getInventory()) 
				{
					if (is == null) {continue;}
					Item item = p.getWorld().dropItem(deathLoc.add(0,1.2,0), is);
					arena.getDrops().add(item);
				}
				p.getInventory().clear();
				p.setHealth(20);
				p.setGameMode(GameMode.SPECTATOR);
				
				e.setDamage(0);
				e.setCancelled(true);
				
				User user = main.getUsersByName().getOrDefault(p.getName(), null);
				if (user == null) 
				{
					user = new User(p); 
					HashMap<String, User> users = main.getUsersByName();
					users.put(p.getName(), user);
					main.setUsersByName(users);
				}
				
				user.setSpectating(true);
				arena.addDeadPlayer(p);
				//p.teleport(deathLoc);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		if (!main.isPlayerInGame(p)) {return;}
		Arena arena = main.getPlayerArena(p);
		for(ItemStack is : p.getInventory()) 
		{
			if (is == null) {continue;}
			Item item = p.getWorld().dropItem(p.getLocation().add(0,1.2,0), is);
			arena.getDrops().add(item);
		}
		p.getInventory().clear();
		p.setGameMode(GameMode.SPECTATOR);
		arena.addDeadPlayer(p);
		User user = main.getUsersByName().getOrDefault(p.getName(), null);
		if (user == null) 
		{
			user = new User(p); 
			HashMap<String, User> users = main.getUsersByName();
			users.put(p.getName(), user);
			main.setUsersByName(users);
		}
		user.setSpectating(true);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		if (!main.isPlayerInGame(p)) {return;}
		Arena arena = main.getPlayerArena(p);
		if (arena.isPlayerSpectating(p)) {return;}
		User user = main.getUsersByName().getOrDefault(p.getName(), null);
		if (user == null) {return;}
		user.setArena(null);
		user.setPlaying(false);
		user.setWaiting(false);
		user.setSpectating(false);
		main.loadPlayerData(p);
	}
	
	@EventHandler
	public void onLeverActivation(PlayerInteractEvent e)
	{
		Player p = (Player) e.getPlayer();
		Arena arena = main.getPlayerArena(p);
		if (arena == null) {return;}
		if (arena.isPlayerInLobby(p))
		{
			ItemStack item = p.getItemInHand();
			if (item == null) {return;}
			if (item.getType().equals(Material.MAGMA_CREAM)) 
			{
				User user = main.getUsersByName().getOrDefault(p.getName(), null);
				
				if (user == null) 
				{
					user = new User(p); 
					HashMap<String, User> users = main.getUsersByName();
					users.put(p.getName(), user);
					main.setUsersByName(users);
				}
				
				if (arena.removePlayer(p)) 
				{
					p.sendMessage("§2Вы успешно покинули матч."); 
					user.setPlaying(false); 
					user.setSpectating(false);
					user.setWaiting(false);
					return;
				}
			}
			return;
		}
		if (!main.isPlayerInGame(p)) {return;}
		if (e.getAction().equals(Action.PHYSICAL) && !e.getClickedBlock().getType().name().endsWith("PLATE")) {e.setCancelled(true);}
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {return;}
		Material type = e.getClickedBlock().getType();
		if (type.name().endsWith("SHULKER_BOX") 
			|| type.equals(Material.FURNACE) 
			|| type.equals(Material.ANVIL)
			|| type.equals(Material.WORKBENCH)
			|| type.equals(Material.ENDER_CHEST)) 
		{e.setCancelled(true); return;}
		if (!type.equals(Material.LEVER)) {return;}
		if (!p.getInventory().contains(Material.PAPER)) {return;}
		
		for(ItemStack is : p.getInventory().getContents())
		{
			if (is == null) {continue;}
			if (!is.getType().equals(Material.PAPER)) {continue;}
			if (!is.hasItemMeta()) {continue;}
			ItemMeta meta = is.getItemMeta();
			if (!meta.hasDisplayName()) {continue;}
			if (!meta.hasLore()) {continue;}
			String name = meta.getDisplayName();
			if (!name.equals("§6Задание")) {continue;}
			
			List<String> lore = meta.getLore();
			String contractName = ChatColor.stripColor(lore.get(0));
			if (!main.isContract(contractName)) {continue;}
			Contract contract = main.getContractsByName().get(contractName);
			if (!contract.getType().equals("ACTIVATE")) {continue;}
			String arenaName = ChatColor.stripColor(lore.get(6).split("/")[0]);
			if (!main.isArena(arenaName)) {continue;}
			arena = main.getArena(arenaName);
			String leverID = arena.getLever(e.getClickedBlock().getLocation());
			if (leverID == null) {continue;}
			if (leverID.isEmpty()) {continue;}
			if (!leverID.equals(contract.getIdle())) {continue;}
			int progress = Integer.parseInt(lore.get(3).replace("§fПрогресс: §e", "")) + 1;
			lore.set(3, "§fПрогресс: §e"+progress);
			meta.setLore(lore);
			is.setItemMeta(meta);
			p.sendMessage("§2Контракт: §b"+contract.getDisplayName()+" §2Прогресс: §e"+progress+"/"+contract.getAmount());
			
			if (progress == contract.getAmount())
			{
				p.sendMessage("§8=====================================");
				p.sendMessage("§2 Контракт выполнен. Золото получено.");
				p.sendMessage("§8=====================================");
				
				ItemStack gold = new ItemStack(Material.GOLD_INGOT, contract.getPrice());
				p.getInventory().remove(is);
				p.getInventory().addItem(gold);
			}
			return;
		}
	}
	
	@EventHandler
	public void BlockBurnEvent(BlockBurnEvent e)
	{
		String world = e.getBlock().getWorld().getName();
		for(String s : main.getArenasByName().keySet())
		{
			Arena arena = main.getArena(s);
			if (arena == null) {continue;}
			if (arena.getWorld().equals(world)) {e.setCancelled(true); return;}
		}
	}
	
	@EventHandler
	public void BlockIgniteEvent(BlockIgniteEvent e)
	{
		String world = e.getBlock().getWorld().getName();
		for(String s : main.getArenasByName().keySet())
		{
			Arena arena = main.getArena(s);
			if (arena == null) {continue;}
			if (arena.getWorld().equals(world)) 
			{
				if (arena.isStarted()) {continue;}
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void BlockSpreadEvent(BlockSpreadEvent e)
	{
		String world = e.getBlock().getWorld().getName();
		for(String s : main.getArenasByName().keySet())
		{
			Arena arena = main.getArena(s);
			if (arena == null) {continue;}
			if (arena.getWorld().equals(world)) {e.setCancelled(true); return;}
		}
	}
	
	@EventHandler
	public void LeavesDecayEvent(LeavesDecayEvent e)
	{
		String world = e.getBlock().getWorld().getName();
		for(String s : main.getArenasByName().keySet())
		{
			Arena arena = main.getArena(s);
			if (arena == null) {continue;}
			if (arena.getWorld().equals(world)) {e.setCancelled(true); return;}
		}
	}
	
	@EventHandler
	public void HangingBreakByEntityEvent(HangingBreakByEntityEvent e)
	{
		if (e.getRemover() instanceof Player)
		{
			Player p = (Player) e.getRemover();
			if (!main.isPlayerInGame(p)) {return;}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e)
	{
		if (!e.getEntityType().equals(EntityType.PLAYER)) {return;}
		Player p = (Player) e.getEntity();
		if (!main.isPlayerInGame(p)) {return;}
		Arena arena = main.getPlayerArena(p);
		if (arena.isPlayerInLobby(p)) {e.setCancelled(true);}
	}
	
	@EventHandler
	public void onPlayerDropItems(PlayerDropItemEvent e)
	{
		Player p = (Player) e.getPlayer();
		if (!main.isPlayerInGame(p)) {return;}
		Arena arena = main.getPlayerArena(p);
		if (arena.isPlayerPlaying(p)) {arena.addDrop(e.getItemDrop());}
		if (arena.isPlayerInLobby(p)) {e.setCancelled(true);}
	}
}
