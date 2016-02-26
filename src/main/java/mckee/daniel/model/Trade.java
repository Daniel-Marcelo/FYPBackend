package mckee.daniel.model;

import java.sql.Date;

public class Trade {
	
	private int tradeID;
	private String email; //
	private String symbol;  //
	private Date date;		//
	private String buyOrSell;	//
	private String tradeType;	//
	private int transactionID;
	private String status;
	
	private int gameID;
	
	
	public int getTradeID() {
		return tradeID;
	}
	public void setTradeID(int tradeID) {
		this.tradeID = tradeID;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String userEmail) {
		this.email = userEmail;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getBuyOrSell() {
		return buyOrSell;
	}
	public void setBuyOrSell(String buyOrSell) {
		this.buyOrSell = buyOrSell;
	}
	public String getTradeType() {
		return tradeType;
	}
	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}
	public int getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(int transactionID) {
		this.transactionID = transactionID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void printDetails() {
		
		System.out.println("User: "+email);
		System.out.println("Symbol: "+symbol);
		System.out.println("Date: "+date);
		System.out.println("Buy Or Sell: "+buyOrSell);
		System.out.println("Trade Type: "+tradeType);
	}
	public int getGameID() {
		return gameID;
	}
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

}
