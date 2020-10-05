package kiviuly.escape;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Commands implements CommandExecutor 
{
	private Main main;
	public Commands(Main main) {this.main = main;}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) 
	{
		if (sender instanceof Player)
		{
			Player p = (Player) sender;
			if (args.length == 0) {main.OpenGameMenu(p); return true;}
			String sub = args[0].toLowerCase();
			
			if (sub.equals("help"))
			{
				SendHelpMessage(p);
				return true;
			}
			
			if (sub.equals("leave"))
			{
				Arena arena = main.getPlayerArena(p);
				User user = main.getUsersByName().getOrDefault(p.getName(), null);
				if (user == null) 
				{
					user = new User(p); 
					HashMap<String, User> users = main.getUsersByName();
					users.put(p.getName(), user);
					main.setUsersByName(users);
				}
				if (arena == null) {SM("§cВы не в игре.", p); return true;}
				if (arena.removePlayer(p)) 
				{
					SM("§2Вы успешно покинули матч.", p); 
					user.setPlaying(false); 
					user.setSpectating(false);
					user.setWaiting(false);
					return true;
				}
				SM("§cОшибка. Вас не выпустило из арены.", p);
				return true;
			}
			
			if (sub.equals("join"))
			{
				if (args.length == 2) 
				{
					String name = args[1].toUpperCase();
					if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
					Arena arena = main.getArena(name);
					User user = main.getUsersByName().getOrDefault(p.getName(), null);
					if (user == null) 
					{
						user = new User(p); 
						HashMap<String, User> users = main.getUsersByName();
						users.put(p.getName(), user);
						main.setUsersByName(users);
					}
					
					if (arena.addPlayerToGame(user)) {user.setArena(arena); user.setWaiting(true);}
					else {SM("§cОшибка. Вы не смогли зайти на арену.",p);}
				}
				else
				{
					main.OpenGameMenu(p);
				}
				
				return true;
			}
			
			if (!p.hasPermission("escape.admin")) {return true;}
			
			if (sub.equals("save"))
			{
				SM("§cВы указали не все арументы.", p);
				main.saveCFG();
				return true;
			}
			
			if (sub.equals("reload"))
			{
				SM("§cВы указали не все арументы.", p);
				main.reloadCFG();
				return true;
			}
			
			if (args.length < 2) {SM("§cВы указали не все арументы.", p); return true;}
			String name = args[1].toUpperCase();
			
			if (sub.equals("remove"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				main.removeArena(name);
				p.sendMessage("§eАрена §b"+name+"§e удалена.");
				return true;
			}
			
			if (sub.equals("stop"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				arena.Stop(5);
				p.sendMessage("§eАрена §b"+name+"§e остановлена...");
				return true;
			}
			
			if (sub.equals("savetoyml"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				File file = new File(main.getDataFolder()+File.separator+"Arenas");
				if (!file.exists()) {file.mkdir();}
				file = new File(main.getDataFolder()+File.separator+"Arenas"+File.separator+name+".yml");
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				config.set("ID", arena.getID());
				config.set("Name", arena.getName());
				config.set("Description", arena.getDescription());
				config.set("WorldName", arena.getWorld());
				config.set("Creator", arena.getCreator().toString());
				config.set("MaxPlayers", arena.getMaxPlayers());
				config.set("MinPlayers", arena.getMinPlayers());
				config.set("ChestCount", arena.getChestCount());
				config.set("TradersCount", arena.getVillagersCount());
				config.set("TablesCount", arena.getTablesCount());
				config.set("isEnabled", arena.IsEnabled());
				config.set("LobbyLocation", arena.getLobbyLocation());
				config.set("DeadMessages", arena.getDeadMessages());
				config.set("SpawnsLocations", arena.getSpawnsLocations());
				config.set("ChestsLocations", arena.getChestsLocations());
				config.set("TablesLocations", arena.getTablesLocations());
				config.set("OresLocations", arena.getOresLocations());
				config.set("Villagers", arena.getVillagers());
				config.set("Levers", arena.getLevers());
				config.set("ChestItems", arena.getChestItems());
				config.set("VillagersItems", arena.getVillagersItems());
				config.set("Contracts", arena.getContracts());
				config.set("CreatedDate", arena.getCreatedDate());
				try {config.save(file); SM("§2Арена сконвертирована в YML файл.",p); return true;} 
				catch (IOException e) {SM("§cОшибка. Арена не сохранена в YML.",p); return false;}
			}
			
			if (sub.equals("loadfromyml"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				File file = new File(main.getDataFolder()+File.separator+"Arenas");
				if (!file.exists()) {SM("§cОшибка. Папка с файлом не найдена.",p); file.mkdir(); return true;}
				file = new File(main.getDataFolder()+File.separator+"Arenas"+File.separator+name+".yml");
				if (!file.exists()) {SM("§cОшибка. Файл не найден.",p); return true;}
				YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
				arena.setName(config.getString("Name"));
				arena.setDescription(config.getString("Description"));
				arena.setWorld(config.getString("WorldName"));
				arena.setCreator(UUID.fromString(config.getString("Creator")));
				arena.setMaxPlayers(config.getInt("MaxPlayers"));
				arena.setMinPlayers(config.getInt("MinPlayers"));
				arena.setChestCount(config.getInt("ChestCount", arena.getChestCount()));
				arena.setVillagersCount(config.getInt("TradersCount", arena.getVillagersCount()));
				arena.setTablesCount(config.getInt("TablesCount", arena.getTablesCount()));
				arena.setEnabled(config.getBoolean("isEnabled", false));
				arena.setLobbyLocation((Location) config.get("LobbyLocation"));
				arena.setDeadMessages((ArrayList<String>) config.get("DeadMessages"));
				arena.setSpawnsLocations((ArrayList<Location>) config.get("SpawnsLocations"));
				arena.setChestsLocations((ArrayList<Location>) config.get("ChestsLocations"));
				arena.setTablesLocations((ArrayList<Location>) config.get("TablesLocations"));
				arena.setOresLocations((ArrayList<Location>) config.get("OresLocations"));
				arena.setVillagers((HashMap<String, Object>) config.getConfigurationSection("Villagers").getValues(false));
				arena.setLevers((HashMap<String, Object>) config.getConfigurationSection("Levers").getValues(false));
				arena.setVillagersItems((HashMap<String, Object>) config.getConfigurationSection("VillagersItems").getValues(false));
				arena.setPlayersStats((HashMap<String, Object>) config.getConfigurationSection("PlayerStats").getValues(false));
				arena.setChestItems((ArrayList<ItemStack>) config.get("ChestItems"));
				arena.setContracts((ArrayList<String>) config.get("Contracts"));
				arena.setCreatedDate((Date) config.get("CreatedDate"));
				SM("§2Арена выгружена из YML файла успешно.",p);
				return true;
			}
			
			if (sub.equals("additem"))
			{
				if (args.length < 3) {SM("§cВы указали не все арументы.", p); return true;}
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				if (!isInt(args[2])) {SM("§cВес должен быть числом.", p); return true;}
				if (p.getItemInHand() == null) {SM("§cВы не держите предмет в руках.", p); return true;}
				int chance = Integer.parseInt(args[2]);
				if (chance > 250 || chance < 1) {SM("§cВес должен быть числом от 1 до 250.", p); return true;}
				Arena arena = main.getArena(name);
				arena.addItemToChest(chance, p.getItemInHand());
				SM("§2Предмет добавлен в сундуки арены §b"+name+"§2 с шансом §e"+chance+".", p);
				return true;
			}
			
			if (sub.equals("addtrade"))
			{
				if (!main.isTrader(name)) {SM("§cТакой житель НЕ существует.", p); return true;}
				Inventory inv = Bukkit.createInventory(null, 27, "§8Добавление нового трейда: "+name);
				inv.setMaxStackSize(255);
				inv = main.fillFreeSlots(inv, (short) 15, "WALLS");
				inv.setItem(15,new ItemBuilder(Material.STAINED_GLASS_PANE).damage((short)15).build());
				inv.setItem(10,new ItemBuilder(Material.STAINED_GLASS_PANE).damage((short)1).displayname("§eУбрать 10").build());
				inv.setItem(14,new ItemBuilder(Material.STAINED_GLASS_PANE).damage((short)1).displayname("§2Добавить 10").build());
				inv.setItem(11,new ItemBuilder(Material.STAINED_GLASS_PANE).damage((short)3).displayname("§eУбрать 1").build());
				inv.setItem(13,new ItemBuilder(Material.STAINED_GLASS_PANE).damage((short)3).displayname("§2Добавить 1").build());
				inv.setItem(18,new ItemBuilder(Material.STAINED_GLASS_PANE).damage((short)14).displayname("§cОТМЕНИТЬ").build());
				inv.setItem(26,new ItemBuilder(Material.STAINED_GLASS_PANE).damage((short)5).displayname("§2СОХРАНИТЬ").build());
				inv.setItem(12,new ItemBuilder(Material.GOLD_INGOT).displayname("§6Стоимость: §e1").build());
				p.openInventory(inv);
				SM("§2Меню добавления нового трейда открыто.",p);
				SM("§2Нажмимайте на стекло, чтобы увеличить или уменьшить цену.",p);
				return true;
			}
			
			if (sub.startsWith("add"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				ItemStack is = null; String type = "";
				switch(sub)
				{
					case "addcontract":
						if (args.length < 3) {SM("§cВы указали не все арументы.", p); return true;}
						if (!main.isContract(args[2])) {SM("§cКонтракт не существует.", p); return true;}
						Contract contract = main.getContractsByName().get(args[2]);
						arena.addContract(contract);
						SM("§2Контракт добавлен на арену §b"+name+"§2.",p);
					return true;
				
					case "addspawn":
						is = new ItemBuilder(Material.BEACON)
							.displayname("§eПоставьте, чтобы добавить спавн")
							.lore("§fАрена: "+arena.getID())
							.build();
						type = "СПАВНОВ ИГРОКОВ";
					break;
					
					case "addvillager":
						if (args.length < 3) {SM("§cВы указали не все арументы.", p); return true;}
						is = new ItemBuilder(Material.WORKBENCH)
							.displayname("§eПоставьте, чтобы добавить жителя")
							.lore("§fАрена: "+arena.getID())
							.lore("§fТип: "+args[2].toUpperCase())
							.build();
						type = "СПАВНОВ ЖИТЕЛЕЙ";
					break;
					
					case "addlever":
						if (args.length < 3) {SM("§cВы указали не все арументы.", p); return true;}
						is = new ItemBuilder(Material.LEVER)
							.displayname("§eПоставьте, чтобы добавить рычаг")
							.lore("§fАрена: "+arena.getID())
							.lore("§fТип: "+args[2])
							.build();
						type = "СПАВНОВ РЫЧАГОВ";
					break;
					
					case "addore":
						is = new ItemBuilder(Material.STONE)
							.displayname("§eПоставьте, чтобы добавить спавн")
							.lore("§fАрена: "+arena.getID())
							.build();
						type = "СПАВНОВ РУД";
					break;
					
					case "addchest":
						is = new ItemBuilder(Material.CHEST)
							.displayname("§eПоставьте, чтобы добавить сундук")
							.lore("§fАрена: "+arena.getID())
							.build();
						type = "СПАВНОВ СУНДУКОВ";
					break;
					
					case "addtable":
						is = new ItemBuilder(Material.ENCHANTMENT_TABLE)
							.displayname("§eПоставьте, чтобы добавить спавн")
							.lore("§fАрена: "+arena.getID())
							.build();
						type = "СПАВНОВ СТОЛОВ";
					break;
					
					default:
						SM("§cНеизвестная подкоманда.",p);
					return true;
				}
				
				SM("§2Предмет для установки §b"+type+"§2 получен.", p);
				p.getInventory().addItem(is);
				return true;
			}
			
			if (sub.equals("create") || sub.equals("createarena"))
			{
				int maxPlayers = 12;
				if (main.isArena(name)) {SM("§cДанная арена уже существует.", p); return true;}
				if (args.length >= 3) 
				{
					if (!isInt(args[2])) {SM("§cМаксимальное количество игроков - число.", p); return true;}
					maxPlayers = Integer.parseInt(args[2]);
					if (maxPlayers > 128 || maxPlayers <= 1) {SM("§cСлишком маленькое или большое число. §e(Можно от 2 до 128)", p); return true;}
				}
				Arena arena = new Arena(name, maxPlayers, p.getUniqueId(), p.getWorld());
				arena.setLobbyLocation(p.getLocation());
				main.addArena(name, arena);
				SM("§2Арена §b"+name+"§2 создана.",p);
				SM("§2Установите точку лобби: §e/escape setlobby "+name,p);
				return true;
			}
			
			if (sub.equals("converttoyml") || sub.equals("yml"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				File file = new File(main.getDataFolder() + File.separator + "Arenas");
				if (!file.exists()) {file.mkdir();}
				file = new File(main.getDataFolder()+File.separator+"Arenas"+File.separator+name+".yml");
				FileConfiguration config = YamlConfiguration.loadConfiguration(file);
				config.set("Name", arena.getName());
				config.set("ID", arena.getID());
				config.set("Desc", arena.getDescription());
				config.set("WorldName", arena.getWorld());
				config.set("Creator", arena.getCreator());
				config.set("MaxPlayers", arena.getMaxPlayers());
				config.set("MaxPlayers", arena.getMaxPlayers());
				SM("§2Арена §b"+name+"§2 создана.",p);
				return true;
			}
			
			if (sub.equals("createvillager"))
			{
				if (main.isTrader(name)) {SM("§cТакой житель уже существует.", p); return true;}
				Trader trader = new Trader(name);
				main.addTrader(name, trader);
				SM("§2Житель §b"+name+"§2 создан.",p);
				SM("§2Установите ему имя: §e/escape villagername "+name+" <Имя...>",p);
				return true;
			}
			
			if (sub.equals("createcontract"))
			{
				if (main.isContract(name)) {SM("§cДанный контракт существует.", p); return true;}
				Contract contract = new Contract(name);
				main.addContract(name, contract);
				SM("§2Контракт §b"+name+" §2создан.",p);
				SM("§2Установите тип контракта: §e/escape contracttype "+name+" <Type>",p);
				return true;
			}
			
			if (sub.equals("setlobby"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				arena.setLobbyLocation(p.getLocation().clone().getBlock().getLocation().add(0.5,0,0.5));
				SM("§2Лобби для арены §b"+name+"§2 установлено.",p);
				SM("§2Установите название арены: §e/escape setname "+name+" <Имя...>",p);
				return true;
			}
			
			if (args.length < 3) {SM("§cВы указали не все арументы.", p); return true;}
			
			if (sub.equals("contracttype"))
			{
				String type = args[2].toUpperCase();
				if (!main.isContract(name)) {SM("§cДанный контракт НЕ существует.", p); return true;}
				if (!main.getContractTypes().contains(type)) {SM("§cНеверный тип контракта.", p); return true;}
				Contract contract = main.getContractsByName().get(name);
				contract.setType(type);
				SM("§2Для контракта §b"+name+" §2установлен тип §e"+type+"§2.",p);
				SM("§2Установите ID или НАЗВАНИЕ для задания: §e/escape contractidle "+name+" <IDle>",p);
				return true;
			}
			
			if (sub.equals("contractidle"))
			{
				String idle = args[2];
				if (!main.isContract(name)) {SM("§cДанный контракт НЕ существует.", p); return true;}
				Contract contract = main.getContractsByName().get(name);
				contract.setIdle(idle);
				SM("§2Контракт §b"+name+" §2обновлён.",p);
				SM("§2Установите описание задания: §e/escape contractdesc "+name+" <Описание>",p);
				return true;
			}
			
			if (sub.equals("contractdesc"))
			{
				if (!main.isContract(name)) {SM("§cДанный контракт НЕ существует.", p); return true;}
				Contract contract = main.getContractsByName().get(name);
				String str = "";
				for(int i = 2; i < args.length; i++) {str += args[i]+" ";}
				if (args.length > 3) {str = str.substring(0, str.length()-1);}
				str = ChatColor.translateAlternateColorCodes('&', str);
				contract.setDescription(str);
				SM("§2Описание для контракта §b"+name+" §2установлено: "+str,p);
				SM("§2Установите количество материала для выполнения: §e/escape contractamount "+name+" <Количество>",p);
				return true;
			}
			
			if (sub.equals("contractamount"))
			{
				if (!main.isContract(name)) {SM("§cДанный контракт НЕ существует.", p); return true;}
				if (!isInt(args[2])) {SM("§cКоличество должно быть числом.", p); return true;}
				Contract contract = main.getContractsByName().get(name);
				contract.setAmount(Integer.parseInt(args[2]));
				SM("§2Количество материала для выполнения контракта §b"+name+" §2установлено.",p);
				SM("§2Установите плату за выполнение: §e/escape contractprice "+name+" <Количество золота>",p);
				return true;
			}
			
			if (sub.equals("contractprice"))
			{
				if (!main.isContract(name)) {SM("§cДанный контракт НЕ существует.", p); return true;}
				if (!isInt(args[2])) {SM("§cПлата должна быть числом.", p); return true;}
				Contract contract = main.getContractsByName().get(name);
				contract.setPrice(Integer.parseInt(args[2]));
				SM("§2Плата за выполнение контракта §b"+name+" §2установлена.",p);
				return true;
			}
			
			if (sub.equals("setmaxplayers") || sub.equals("setmaxpl"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				if (!isInt(args[2])) {SM("§cЧисло игроков должно быть числом.", p); return true;}
				int count = Integer.parseInt(args[2]);
				if (count > 52 || count < 2) {SM("§cДанное число не дозволительно.", p); return true;}
				Arena arena = main.getArena(name);
				arena.setMaxPlayers(count);
				SM("§2Максимальное количество игроков для арены §b"+name+"§2 установлено на §e"+count+"§2.", p);
				return true;
			}
			
			if (sub.equals("setminplayers") || sub.equals("setminpl"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				if (!isInt(args[2])) {SM("§cЧисло игроков должно быть числом.", p); return true;}
				int count = Integer.parseInt(args[2]);
				if (count > 52 || count < 1) {SM("§cДанное число не дозволительно.", p); return true;}
				Arena arena = main.getArena(name);
				arena.setMinPlayers(count);
				SM("§2Минимальное количество игроков для арены §b"+name+"§2 установлено на §e"+count+"§2.", p);
				return true;
			}
			
			if (sub.equals("edititems"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				if (!isInt(args[2])) {SM("§cШанс должен быть числом от 1 до 100.", p); return true;}
				int chance = Integer.parseInt(args[2]);
				if (chance > 100 || chance < 1) {SM("§cШанс должен быть числом от 1 до 100.", p); return true;}
				Arena arena = main.getArena(name);
				ArrayList<ItemStack> arr = arena.getChestItems();
				Inventory inv = Bukkit.createInventory(null, 54, "§8Редактор предметов арены "+name);
				for(ItemStack is : arr) 
				{
					if (is == null) {continue;}
					inv.addItem(is);
				}
				p.openInventory(inv);
				SM("§2Редактор предметов для шанса §e"+chance+"§2 арены §b"+name+"§2.", p);
				return true;
			}
			
			if (sub.equals("villagername"))
			{
				if (!main.isTrader(name)) {SM("§cТакой житель НЕ существует.", p); return true;}
				Trader trader = main.getTrader(name);
				String str = "";
				for(int i = 2; i < args.length; i++) {str += args[i];}
				if (args.length > 3) {str = str.substring(0, str.length()-1);}
				str = ChatColor.translateAlternateColorCodes('&', str);
				trader.setName(str);
				SM("§2Житель §b"+name+"§2 переименован в "+str+"§2.",p);
				SM("§2Добавьте ему предметы для торговли: §e/escape addtrade "+name,p);
				return true;
			}
			
			if (sub.equals("setname"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				String str = "";
				for(int i = 2; i < args.length; i++) {str += args[i]+" ";}
				str = str.substring(0, str.length()-1);
				str = ChatColor.translateAlternateColorCodes('&', str);
				arena.setName(str);
				SM("§2Арена §b"+name+"§2 переименована в "+str+"§2.",p);
				SM("§2Добавьте точки спавна: §e/escape addspawn "+name,p);
				return true;
			}
			
			if (sub.equals("setdesc") || sub.equals("setdescription"))
			{
				if (!main.isArena(name)) {SM("§cДанная арена НЕ существует.", p); return true;}
				Arena arena = main.getArena(name);
				String str = "";
				for(int i = 2; i < args.length; i++) {str += args[i]+" ";}
				str = str.substring(0, str.length()-1);
				str = ChatColor.translateAlternateColorCodes('&', str);
				arena.setDescription(str);
				SM("§2Описание арены §b"+name+"§2 изменено: "+str+"§2.",p);
				return true;
			}
		}
		
		return false;
	}
	
	public void SendHelpMessage(Player p)
	{
		String msg = 
	          "§7╔═════ §2Команды мини-режима §7═════►" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §b/escape §7——— §2Открыть меню мини-игры" + "\n"
	        + "§7‖  §b/escape join §7——— §2Вступить в случайный матч" + "\n"
	        + "§7‖  §b/escape join [Арена] §7——— §2Вступить в открытый матч" + "\n"
	        + "§7‖  §b/escape leave §7——— §2Выйти из матча" + "\n"
	        + "§7‖  §b/escape stats §7——— §2Посмотреть статистику" + "\n"
	        + "§7‖  §b/escape stats [Игрок] §7——— §2Посмотреть статистику игрока" + "\n"
	        + "§7‖  §b/escape info §7——— §2Описание и правила режима" + "\n"
	        + "§7‖" + "\n"
	        + "§7╚═══════════════════════════════►";
	
	    if (p.hasPermission("escape.admin"))
	    {
	        msg +=
	          "\n\n"
	        + "§7╔═════ §eКоманды админа §7═════►" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/escape reload §7——— §eПерезагрузить настройки плагина" + "\n"
	        + "§7‖  §c/escape save §7——— §eСохранить настройки плагина" + "\n"
	        + "§7‖  §c/escape list §7——— §eСписок арен" + "\n"
	        + "§7‖  §c/escape stop <ID/all> §7——— §eОстановить игру" + "\n"
	        + "§7‖  §c/escape start <ID/all> §7——— §eЗапустить игру" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/escape create <ID> [Число макс. игроков] §7——— §eСоздать новую арену" + "\n"
	        + "§7‖  §c/escape setlobby <ID> §7——— §eУстановить лобби для арены" + "\n"
	        + "§7‖  §c/escape setmaxplayers <ID> <Число> §7——— §eУстановить макс. кол-во игроков" + "\n"
	        + "§7‖  §c/escape setminplayers <ID> <Число> §7——— §eУстановить мин. кол-во игроков" + "\n"
	        + "§7‖  §c/escape setname <ID> <Имя...> §7——— §eЗадать название арены" + "\n"
	        + "§7‖  §c/escape addspawn <ID> §7——— §eДобавить спавн" + "\n"
	        + "§7‖  §c/escape addchest <ID> §7——— §eДобавить сундук" + "\n"
	        + "§7‖  §c/escape addtable <ID> §7——— §eДобавить стол" + "\n"
	        + "§7‖  §c/escape addlever <ID> <Имя...> §7——— §eДобавить рычаг" + "\n"
	        + "§7‖  §c/escape additem <ID> <Вес предмета> §7——— §eДобавить предмет в сундуки" + "\n"
	        + "§7‖  §c/escape addcontract <ID> <CID> §7——— §eДобавить контракт" + "\n"
	        + "§7‖  §c/escape addvillager <ID> <VID> §7——— §eДобавить торговца" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/escape createcontract <CID> §7——— §eСоздать контракт" + "\n"
	        + "§7‖  §c/escape contracttype <CID> <Type> §7——— §eСоздать контракт" + "\n"
	        + "§7‖  §c/escape contractdesc <CID> <Desc> §7——— §eСоздать контракт" + "\n"
	        + "§7‖  §c/escape contractidle <CID> <IDLE> §7——— §eСоздать контракт" + "\n"
	        + "§7‖  §c/escape contractamount <CID> <Amount> §7——— §eСоздать контракт" + "\n"
	        + "§7‖  §c/escape contractprice <CID> <Price> §7——— §eСоздать контракт" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §eТипы контрактов:" + "\n"
	        + "§7‖  §сKILLS §7- §eУбить" + "\n"
	        + "§7‖  §сACTIVATE §7- §eАктивировать рычаг" + "\n"
	        + "§7‖  §сMINE §7- §eДобыть руду" + "\n"
	        + "§7‖  §сFIND §7- §eНайти предмет" + "\n"
	        + "§7‖  §сBREAK §7- §eСломать блоки" + "\n"
	        + "§7‖  §сLOOT §7- §eОблутать сундуки" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/escape createvillager <VID> §7——— §eДобавить тип жителя" + "\n"
	        + "§7‖  §c/escape villagername <VID> <Имя...> §7——— §eЗадать имя жителю" + "\n"
	        + "§7‖  §c/escape addtrade <VID> §7——— §eДобавить предмет в торговлю" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/escape enable <ID> §7——— §eВключить арену" + "\n"
	        + "§7‖  §c/escape disable <ID> §7——— §eВыключить арену" + "\n"
	        + "§7‖" + "\n"
	        + "§7╚═══════════════════════════════►";
	    }
	
	    p.sendMessage(msg);
	}
	
	public boolean isInt(String s)
	{
	    try {Integer.parseInt(s); return true;} 
	    catch (NumberFormatException ex) {return false;}
	}
	
	public void SM(String string, Player p) {p.sendMessage(string);}
}
