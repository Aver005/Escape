package kiviuly.escape;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

public class User implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private UUID playerID;
	private OfflinePlayer player;
	
	private Arena arena = null;
	
	private Boolean inGame = false;
	private Boolean inLobby = false;
	private Boolean inSpectators = false;
	
	private Integer wins = 0;
	private Integer loses = 0;
	private Integer kills = 0;
	private Integer deaths = 0;
	private Integer oresMined = 0;
	private Integer questsCompleted = 0;
	private Integer mvpGames = 0;
	private Integer gamesPlayed = 0;
	private Integer lastGameKills = 0;
	private Integer bestGameKills = 0;
	
	private HashMap<Contract, Integer> contractsProgress = new HashMap<>();
	
	public User(OfflinePlayer p)
	{
		this.setPlayer(p);
		this.setPlayerID(p.getUniqueId());
	}

	public UUID getPlayerID() {
		return playerID;
	}

	public void setPlayerID(UUID playerID) {
		this.playerID = playerID;
	}

	public OfflinePlayer getPlayer() {
		return player;
	}

	public void setPlayer(OfflinePlayer player) {
		this.player = player;
	}

	public Boolean isPlaying() {
		return inGame;
	}

	public Boolean isWaiting() {
		return inLobby;
	}

	public Boolean isSpectate() {
		return inSpectators;
	}

	public Integer getWins() {
		return wins;
	}

	public void setWins(Integer wins) {
		this.wins = wins;
	}

	public Integer getLoses() {
		return loses;
	}

	public void setLoses(Integer loses) {
		this.loses = loses;
	}

	public Integer getKills() {
		return kills;
	}

	public void setKills(Integer kills) {
		this.kills = kills;
	}

	public Integer getDeaths() {
		return deaths;
	}

	public void setDeaths(Integer deaths) {
		this.deaths = deaths;
	}

	public Integer getOresMined() {
		return oresMined;
	}

	public void setOresMined(Integer oresMined) {
		this.oresMined = oresMined;
	}

	public Integer getQuestsCompleted() {
		return questsCompleted;
	}

	public void setQuestsCompleted(Integer questsCompleted) {
		this.questsCompleted = questsCompleted;
	}

	public Integer getMvpGames() {
		return mvpGames;
	}

	public void setMvpGames(Integer mvpGames) {
		this.mvpGames = mvpGames;
	}

	public Integer getGamesPlayed() {
		return gamesPlayed;
	}

	public void setGamesPlayed(Integer gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}

	public Integer getLastGameKills() {
		return lastGameKills;
	}

	public void setLastGameKills(Integer lastGameKills) {
		this.lastGameKills = lastGameKills;
	}

	public Integer getBestGameKills() {
		return bestGameKills;
	}

	public void setBestGameKills(Integer bestGameKills) {
		this.bestGameKills = bestGameKills;
	}

	public Arena getArena() {
		return arena;
	}

	public void setArena(Arena arena) {
		this.arena = arena;
		inLobby = true;
	}

	public void addContractProgress(Contract contract, int progress) 
	{
		int i = contractsProgress.getOrDefault(contract, 0);
		contractsProgress.put(contract, progress+i);
	}

	public HashMap<Contract, Integer> getContractsProgress() {
		return contractsProgress;
	}

	public void setContractsProgress(HashMap<Contract, Integer> contractsProgress) {
		this.contractsProgress = contractsProgress;
	}

	public int getContractProgress(Contract contract) 
	{
		return contractsProgress.getOrDefault(contract, 0);
	}

	public void setPlaying(boolean b) 
	{
		inGame = b;
		if (inGame) {setWaiting(false); setSpectating(false);}
	}
	
	public void setWaiting(boolean b) 
	{
		inLobby = b;
	}
	
	public void setSpectating(boolean b) 
	{
		inSpectators = b;
	}
}
