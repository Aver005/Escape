package kiviuly.escape;

import java.io.Serializable;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

public class Trader implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String ID;
	private String name;
	
	private HashMap<ItemStack, Integer> trades = new HashMap<>();
	
	
	public Trader(String ID)
	{
		this.ID = ID;
		this.name = ID;
	}
	
	public String getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addTrade(int price, ItemStack item) 
	{
		trades.put(item, price);
	}

	public HashMap<ItemStack, Integer> getTrades() {
		return trades;
	}

	public void setTrades(HashMap<ItemStack, Integer> trades) {
		this.trades = trades;
	}
	
}
