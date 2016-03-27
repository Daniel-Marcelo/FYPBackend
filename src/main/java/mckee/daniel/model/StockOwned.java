package mckee.daniel.model;


public class StockOwned {
	
	private int gameID;
	private String email;
	private String symbol;
	private int quantity;
	private double avgPurchPrice;
	
	/*
	 * Fields used In portfolio page that aren't in the DB
	 */
	private String companyName;
	private double currentPrice;
	private double total;
	private double gainOrLoss;
	
	
	public double getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public double getGainOrLoss() {
		return gainOrLoss;
	}
	public void setGainOrLoss(double gainOrLoss) {
		this.gainOrLoss = gainOrLoss;
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
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getAvgPurchPrice() {
		return avgPurchPrice;
	}
	public void setAvgPurchPrice(double avgPurchPrice) {
		this.avgPurchPrice = avgPurchPrice;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}


}
