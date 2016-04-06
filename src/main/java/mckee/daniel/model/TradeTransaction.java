package mckee.daniel.model;


public class TradeTransaction {
	
	private int transactionID;
	private int quantity;	
	private double sharePrice;
	private double total;
	
	public int getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(int transactionID) {
		this.transactionID = transactionID;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public double getSharePrice() {
		return sharePrice;
	}
	public void setSharePrice(double sharePrice) {
		this.sharePrice = sharePrice;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double costOfTrade) {
		this.total = costOfTrade;
	}
}
