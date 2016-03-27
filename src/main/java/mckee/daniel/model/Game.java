package mckee.daniel.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
	
	private int gameID;
	private String gameName, gameType, creatorEmail;
	private double startingCash;
	private String startDate, endDate;
	private String joinCode;
	private String status;
	
	
	/*Test COMMENT
	 * Not fields in database
	 */
	private List<User> usersInGame = new ArrayList<User>();
	
	
	
	public void printGameDetails(){
		
		System.out.println(gameName);
		System.out.println(gameType);
		System.out.println(creatorEmail);
		System.out.println(startDate);
		System.out.println(endDate);
		System.out.println(startingCash);
	}
	public int getGameID() {
		return gameID;
	}
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}
	public String getGameName() {
		return gameName;
	}
	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	public String getGameType() {
		return gameType;
	}
	public void setGameType(String gameType) {
		this.gameType = gameType;
	}
	public String getCreatorEmail() {
		return creatorEmail;
	}
	public void setCreatorEmail(String creatorEmail) {
		this.creatorEmail = creatorEmail;
	}
	public double getStartingCash() {
		return startingCash;
	}
	public void setStartingCash(double startingCash) {
		this.startingCash = startingCash;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public List<User> getUsersInGame() {
		return usersInGame;
	}
	public void setUsersInGame(List<User> usersInGame) {
		this.usersInGame = usersInGame;
	}
	public String getJoinCode() {
		return joinCode;
	}
	public void setJoinCode(String joinCode) {
		this.joinCode = joinCode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
