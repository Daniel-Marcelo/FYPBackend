import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mckee.daniel.model.Trade;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import yahoofinance.YahooFinance;

public class JobA extends QuartzJobBean {

	private Connection conn;

	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {

		try {
			conn = DriverManager.getConnection(Details.DB_HOST, Details.DB_USERNAME, Details.DB_PASSWORD);
			
			List<String> userEmails = getUsers();

			for (String email : userEmails) {
				List<Trade> limitOrdersList = getLimitOrdersForUser(email);
				checkToExecuteLimitOrders(email, limitOrdersList);
			}
			
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> getUsers() {
		List<String> emails = new ArrayList<String>();
		
		try {
			PreparedStatement stmt1 = conn.prepareStatement("SELECT DISTINCT email FROM fyp_user");
			ResultSet rs = stmt1.executeQuery();

			while (rs.next())
				emails.add(rs.getString("email"));

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return emails;
	}

	public List<Trade> getLimitOrdersForUser(String email) {
		
		List<Trade> limitOrdersList = new ArrayList<Trade>();

		try {

			// Gets list of active limit orders from trade table
			PreparedStatement stmt1 = conn.prepareStatement("SELECT * FROM fyp_trade WHERE email = ? AND trade_type = ? AND status = ?");
			stmt1.setString(1, email);
			stmt1.setString(2, "Limit");
			stmt1.setString(3, "Ongoing");
			ResultSet rs = stmt1.executeQuery();

			while (rs.next()) {

				Trade trade = new Trade();
				trade.setTradeID(rs.getInt("trade_id"));
				trade.setEmail(rs.getString("email"));
				trade.setSymbol(rs.getString("symbol"));
				trade.setDate(rs.getDate("date"));
				trade.setBuyOrSell(rs.getString("buy_or_sell"));
				trade.setTradeType(rs.getString("trade_type"));
				trade.setTransactionID(rs.getInt("transaction_id"));
				trade.setStatus(rs.getString("status"));
				trade.setGameID(rs.getInt("game_id"));
				limitOrdersList.add(trade);
			}
			
			System.out.println("\n\nNumber of ongoing limit orders for the user: "+ email + " = " + limitOrdersList.size());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return limitOrdersList;
	}

	public double checkToExecuteLimitOrders(String email,
			List<Trade> limitOrdersList) {

		try {
			boolean orderisActive = false;

			DateTime todaysDate = new DateTime();
			int quantity = 0, durationDays = 0;

			for (Trade trade : limitOrdersList) {
				System.out.println("\n\n***************************************");
				System.out.println("Trade ID: "+trade.getTradeID());

				DateTime dt = new DateTime(trade.getDate());
				DateTime startDate = dt.plusDays(1);
				int days = Days.daysBetween(startDate, todaysDate).getDays();

				ResultSet rs = getLimitOrderInfo(trade);
				double desiredPrice = rs.getDouble("desired_price");
				quantity = rs.getInt("quantity");
				durationDays = rs.getInt("duration_days");
				
				System.out.println("Different between start date and todats date: "+days+" is this less than "+durationDays);
				
				// Check if still valid
				if (days < durationDays){
					orderisActive = true;
					System.out.println("This order is active");
				}
				
				else {
					
					orderisActive = false;
					
					if (!trade.getStatus().equals("Expired")){
						updateStatus(trade, "Expired");
						System.out.println("This order needs to be updated to expired");
					}
				}
				


				if (orderisActive) {
					double currentPrice = YahooFinance.get(trade.getSymbol()).getQuote().getPrice().doubleValue();

					if (trade.getBuyOrSell().equals("Buy")) {
						
						System.out.print("Buy Limit Order:\nComparing: Is " + currentPrice + " <=  " + desiredPrice+ "? ");

						if (currentPrice <= desiredPrice) {
							
							System.out.println("Yes\nOrder to be executed ID: " + trade.getTradeID());
							
							updateStatus(trade, "Executed");
							double total = updateTransactionAndUpdateTransactionID(trade.getTradeID(), currentPrice, quantity);
							updateBalanceForUserInGame(trade.getEmail(), trade.getGameID(), -total);
							updateStocksOwned(trade);
							
							//1 update balance
							//2 calculate new average purchase price
							//3 Get old quantity
							//4 Get new quantity
							//5 update quantity
							//6 update average purchase price
							//4 Insert into table
							
							
							System.out.println("Executing trade on symbol: " + trade.getSymbol());
						}
						else{
							System.out.println("No");
						}
					} else {
						System.out.println("Sell Limit Order: Comparing: Is " + currentPrice + " >=  " + desiredPrice+"? ");

						if (currentPrice >= desiredPrice) {
							
							System.out.println("Yes\nOrder to be executed ID: " + trade.getTradeID());

							updateStatus(trade, "Executed");
							double total = updateTransactionAndUpdateTransactionID(trade.getTradeID(), currentPrice, quantity);
							updateBalanceForUserInGame(trade.getEmail(), trade.getGameID(), total);
							trade.setStatus("Executed");
							updateStocksOwned(trade);

						}
						else{
							System.out.println("No");
						}
					}
				}

			}

			double adjustment =0;
			return adjustment;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private void updateBalanceForUserInGame(String email, int gameID, double total) {

		double oldBal = 0;
		try {
			PreparedStatement stmt1 = conn.prepareStatement("SELECT balance FROM fyp_user_game_participation WHERE game_id = ? AND email = ?");
			stmt1.setInt(1, gameID);
			stmt1.setString(2, email);
			ResultSet rs = stmt1.executeQuery();
			
			while(rs.next())
				oldBal = rs.getDouble("balance");
			
			double newBal = oldBal+total;
			System.out.println("Old Bal: "+oldBal+ " - TOTAL: "+total+ " - NEW BAL "+newBal);
			
			
			PreparedStatement stmt2 = conn.prepareStatement("UPDATE fyp_user_game_participation SET balance = ? WHERE game_id = ? AND email = ?");
			stmt2.setDouble(1, newBal);
			stmt2.setInt(2, gameID);
			stmt2.setString(3, email);
			
			stmt2.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void updateStatus(Trade trade, String newStatus) {
		try {
			PreparedStatement stmt1 = conn.prepareStatement("UPDATE fyp_trade SET status = ? WHERE trade_id = ?");
			stmt1.setString(1, newStatus);
			stmt1.setInt(2, trade.getTradeID());
			stmt1.executeUpdate();

			stmt1.close();
			
			trade.setStatus("Executed");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private double updateTransactionAndUpdateTransactionID(int tradeID, double currentPrice, int quantity) {

		double total = currentPrice * (double) quantity;
		int transactionID = 0;
		System.out.println("About to write transaction: " + total);
		try {

			PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO fyp_trade_transaction (share_price, quantity, total) VALUES (?, ?, ?)");
			stmt1.setDouble(1, currentPrice);
			stmt1.setInt(2, quantity);
			stmt1.setDouble(3, total);

			stmt1.execute();
			stmt1.close();

			PreparedStatement stmt2 = conn.prepareStatement("Select last_insert_id()");
			ResultSet rs = stmt2.executeQuery();
			
			while(rs.next())
				transactionID = rs.getInt(1);
			
			System.out.println("Updating transaction ID to: " + transactionID + " for trade : " + tradeID);

			PreparedStatement stmt3 = conn.prepareStatement("UPDATE fyp_trade SET transaction_id = ? WHERE trade_id = ?");
			stmt3.setInt(1, transactionID);
			stmt3.setInt(2, tradeID);

			stmt3.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return total;
	}

	public double getBalance(String email) {

		double balance = 0;
		try {
			PreparedStatement stmt1 = conn.prepareStatement("Select balance FROM fyp_user WHERE email = ?");
			stmt1.setString(1, email);
			ResultSet rs = stmt1.executeQuery();

			while(rs.next())
				balance = rs.getDouble("balance");
			
			stmt1.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return balance;
	}

	public void autoUpdateBalance(String email, double newBalance) {

		try {
			PreparedStatement stmt1 = conn.prepareStatement("UPDATE fyp_user SET balance = ? WHERE email = ?");
			stmt1.setDouble(1, newBalance);
			stmt1.setString(2, email);
			stmt1.executeUpdate();

			stmt1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ResultSet getLimitOrderInfo(Trade trade) {
		
		try {
			PreparedStatement stmt1 = conn
					.prepareStatement("SELECT desired_price, quantity, duration_days FROM fyp_limit_order_details WHERE trade_id = ?");
			stmt1.setInt(1, trade.getTradeID());
			ResultSet rs = stmt1.executeQuery();
			rs.next();

			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	public void updateStocksOwned(Trade trade) {

		int orderQuantity = 0;

		try {
			PreparedStatement stmt3 = conn.prepareStatement("SELECT quantity FROM fyp_limit_order_details WHERE trade_id = ?");
			stmt3.setInt(1, trade.getTradeID()); 
			
			ResultSet rs2 = stmt3.executeQuery();
			
			while (rs2.next()) {
				
				if (trade.getBuyOrSell().equals("Buy"))
					orderQuantity = rs2.getInt("quantity");
				else
					orderQuantity = -(rs2.getInt("quantity"));
			}
			System.out.println("Order Type: " + trade.getBuyOrSell() + " order quantity: " + orderQuantity);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		try {
			double avgPurchasePrice = updateAvgPurchasePrice(trade);
			System.out.println("Average Purchase Price: "+avgPurchasePrice);
			
			PreparedStatement stmt1 = conn.prepareStatement("Select quantity FROM fyp_stock_owned WHERE email = ? AND symbol = ? ");
			stmt1.setString(1, trade.getEmail());
			stmt1.setString(2, trade.getSymbol());

			ResultSet rs = stmt1.executeQuery();

			if (rs.next() == false) {

				PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO fyp_stock_owned VALUES (?, ?, ?, ?, ?)");
				stmt2.setInt(1, trade.getGameID());
				stmt2.setString(2, trade.getEmail());
				stmt2.setString(3, trade.getSymbol());
				stmt2.setInt(4, orderQuantity);
				stmt2.setDouble(5, avgPurchasePrice);
				stmt2.execute();
				stmt2.close();

			} else {
				int oldQuantity = rs.getInt("quantity");
				
				PreparedStatement stmt2 = conn.prepareStatement("UPDATE fyp_stock_owned SET quantity = ?, avg_purch_price = ? WHERE email = ? AND symbol = ? AND game_id = ?");
				stmt2.setInt(1, (oldQuantity + orderQuantity));
				stmt2.setDouble(2, avgPurchasePrice);
				stmt2.setString(3, trade.getEmail());
				stmt2.setString(4, trade.getSymbol());
				stmt2.setInt(5, trade.getGameID());
				stmt2.executeUpdate();
				stmt2.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
private double updateAvgPurchasePrice(Trade trade) {
		
		List<Integer> transactionIDs = new ArrayList<Integer>();
		double total = 0;
		int quantity = 0;
		
		try {
			PreparedStatement stmt1 = conn.prepareStatement("SELECT transaction_id FROM fyp_trade WHERE email = ? AND symbol = ? AND game_id = ?");
			stmt1.setString(1, trade.getEmail());
			stmt1.setString(2,  trade.getSymbol());
			stmt1.setInt(3, trade.getGameID());
			
			
			ResultSet rs = stmt1.executeQuery();
			
			while(rs.next())
				transactionIDs.add(rs.getInt("transaction_id"));
			
			
			for(int tID: transactionIDs){
				
				PreparedStatement stmt2 = conn.prepareStatement("SELECT quantity, total FROM fyp_trade_transaction WHERE transaction_id = ?");
				stmt2.setInt(1, tID);
				
				ResultSet rs2 = stmt2.executeQuery();
				
				while (rs2.next()){
					quantity += rs2.getInt("quantity");
					total += rs2.getDouble("total");
				}
			}
			
			System.out.println("Quantity: "+quantity);
			System.out.println("total: "+total);
			
			double avgPurchPrice = total/ (double)quantity;
			return avgPurchPrice;	
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
