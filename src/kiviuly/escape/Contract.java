package kiviuly.escape;

import java.io.Serializable;

public class Contract implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String ID;
	private String displayName;
	private String type;
	private String idle;
	private String description;
	
	private int amount = 0;
	private int price = 0;
	
	public Contract(String ID) 
	{
		this.setDisplayName(ID);
		this.ID = ID;
	}

	public String getID() {
		return ID;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getAmount() {
		return amount;
	}
	
	public int getPrice() {
		return price;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}

	public void setIdle(String idle) 
	{
		this.idle = idle;
	}
	
	public String getIdle() 
	{
		return idle;
	}
	
	public void setDescription(String desc) 
	{
		description = desc;
	}

	public String getDescription() 
	{
		return description;
	}
}
