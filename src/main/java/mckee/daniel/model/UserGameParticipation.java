package mckee.daniel.model;


public class UserGameParticipation {
	
	private String email;
	private int gameID;
	private double balance;
	
	/*
	 * Not fields in DB
	 */
	//private String gameName;
	private Game game;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getGameID() {
		return gameID;
	}
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
/*w*/
	public Game getGame() {
		return game;
	}
	public void setGame(Game game) {
		this.game = game;
	}

}
