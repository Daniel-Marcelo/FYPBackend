package mckee.daniel.model;

public class UserGameBalanceHistory {
	
	private int balanceID, gameID;
	private String email, date; 
	private double closingBal, percentChange;

	public int getBalanceID() {
		return balanceID;
	}
	public void setBalanceID(int balanceID) {
		this.balanceID = balanceID;
	}
	public int getGameID() {
		return gameID;
	}
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getClosingBal() {
		return closingBal;
	}
	public void setClosingBal(double closingBal) {
		this.closingBal = closingBal;
	}
	public double getPercentChange() {
		return percentChange;
	}
	public void setPercentChange(double percentChange) {
		this.percentChange = percentChange;
	}

}
