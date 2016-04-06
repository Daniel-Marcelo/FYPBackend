import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mckee.daniel.model.StockOwned;
import mckee.daniel.model.Trade;
import mckee.daniel.model.TradeTransaction;
import mckee.daniel.model.User;
import mckee.daniel.model.UserGameParticipation;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class JobC extends QuartzJobBean {

	private static Connection conn;

	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {

		int defaultGameID = 4;
		List<Date> dates = new ArrayList<Date>();

		boolean datesFull = false;
		List<User> users = new ArrayList<User>();
		String[] symbols = getSymbolsArray();
		
		User user = new User();
		user.setEmail("daniel.mcke@hotmail.com");
		users.add(user);
		
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
		from.add(Calendar.YEAR, -1);

		System.out.println("Year Simulation\n*******************************************");
		
		//System.out.println("1) Fetching users in default game");
		
		try {
			conn = DriverManager.getConnection(Details.DB_HOST, Details.DB_USERNAME, Details.DB_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Map<String, Stock> stocks = YahooFinance.get(symbols, from, to, Interval.DAILY);

		for (String key : stocks.keySet()) {
			
			Stock stock = stocks.get(key);
			//System.out.println(stock.getSymbol()+" - Number of quotes: " + stock.getHistory().size());
			List<HistoricalQuote> historicalQuotes = stock.getHistory();
			
			if(!datesFull){
				
				for(HistoricalQuote quote : historicalQuotes){
					
					Date date= new Date(quote.getDate().getTime().getTime());
					//System.out.println("Date added to list: "+date);
					dates.add(date);
					
				}
				datesFull = true;
			}
			
			if(historicalQuotes.size()!=0){
				
				for (int j = 0; j < 3; j++) {
					
	/*				HistoricalQuote quote = historicalQuotes
							.get(randomNumber(historicalQuotes.size()));*/
					
					HistoricalQuote quote = historicalQuotes.get(j);
	
					Date timeOfTrade = new Date(quote.getDate().getTime().getTime());
					//System.out.println("DATE OF TRADE: "+timeOfTrade);
					
					TradeTransaction transaction = new TradeTransaction();
					transaction.setQuantity(5);
					transaction.setSharePrice(quote.getClose().doubleValue());
					transaction.setTotal(transaction.getSharePrice()* (double) transaction.getQuantity());
					insertTransaction(transaction);
					int tID = getLastTransactionID();
					
					Trade trade = new Trade();
					trade.setEmail(user.getEmail());
					trade.setSymbol(quote.getSymbol());
					trade.setBuyOrSell("Buy");
					trade.setTradeType("Market");
					trade.getTransaction().setTransactionID(tID);
					trade.setStatus("Executed");
					trade.setGameID(defaultGameID);
					trade.setDate(timeOfTrade);
					insertTrade(trade);
	
					List<Integer> transactionIDs = getTransactionIDs(user.getEmail(), quote.getSymbol(), defaultGameID);
					StockOwned so = calculateNewAvgPurchPrice(transactionIDs, user.getEmail(), quote.getSymbol(), defaultGameID);
					
					updateBalance(user.getEmail(), transaction.getTotal(), defaultGameID);
					updateQuantity(user.getEmail(), quote.getSymbol(), defaultGameID, so);
					
					//System.out.println(i + ") Details: quantity - 5 " + quote.getDate().getTime() + " - " + quote.getClose().doubleValue() + " - \nafter being updated:Average purchase price is now " + so.getAvgPurchPrice());
	
					// Create Transaction Object
					// Insert Transaction
					// Get transaction ID
					// Create trade object
					// Insert transaction
	
					// Set quantity and total to minus if sell order.
					// Get transaction IDs for this symbol for this email for
					// this
					// stock
					// Get new average purchase price - StockOwned Object.
					// Update balance
					// update quantity of stock owned.
					// }
					/*
					 * } catch (SQLException e) { // TODO Auto-generated catch block
					 * e.printStackTrace(); }
					 */
	
				}

			}
		}
		
		Date previousDate = dates.get(0);
		insertIntoAccValHistoryTable(defaultGameID, user.getEmail(), previousDate, 200000, 0);

		for(int x = 1; x < dates.size() ; x++){
			
			Date date = dates.get(x);
			
			double oldValue = getYesterdaysAccValue(defaultGameID, user.getEmail(), date, previousDate);
			//System.out.println("Acc Value on this date: "+oldValue);
			
			previousDate = date;
			double percentChange = randomDouble()/100;
			double difference = oldValue*percentChange;
			//System.out.println("To be added/subtracted: "+difference);
			
			double newValue = difference+oldValue;
			insertIntoAccValHistoryTable(defaultGameID, user.getEmail(), date, newValue, percentChange*100);
		}
		
		System.out.println("DONE");

	}

	private void insertIntoAccValHistoryTable(int defaultGameID, String email, Date date, double newValue, double percentChange) {
		try {
			PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO fyp_user_game_account_history(game_id, email, date, closing_acc_value, percent_change) VALUES(?,?,?,?,?)");
			stmt1.setInt(1, defaultGameID);
			stmt1.setString(2, email);
			stmt1.setDate(3, date);
			stmt1.setDouble(4, newValue);
			stmt1.setDouble(5, percentChange);
			
			stmt1.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	public void insertTrade(Trade trade) {

		try {
			PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO fyp_trade (email, symbol, date, buy_or_sell, trade_type, status, game_id, transaction_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			stmt1.setInt(8, trade.getTransaction().getTransactionID());

			stmt1.setString(1, trade.getEmail());
			stmt1.setString(2, trade.getSymbol().toUpperCase());
			stmt1.setDate(3, trade.getDate());
			stmt1.setString(4, trade.getBuyOrSell());
			stmt1.setString(5, trade.getTradeType());
			stmt1.setInt(7, trade.getGameID());
			stmt1.setString(6, "Executed");

			stmt1.execute();
			stmt1.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getLastTransactionID() {

		int transactionID = 0;
		try {

			PreparedStatement stmt2 = conn.prepareStatement("Select Max(transaction_id) FROM fyp_trade_transaction");
			ResultSet rs = stmt2.executeQuery();

			while (rs.next())
				transactionID = rs.getInt(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return transactionID;
	}

	public void insertTransaction(TradeTransaction tradeTransaction) {

		try {
			
			PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO fyp_trade_transaction (share_price, quantity, total) VALUES (?, ?, ?)");
			stmt1.setDouble(1, tradeTransaction.getSharePrice());
			stmt1.setInt(2, tradeTransaction.getQuantity());
			stmt1.setDouble(3, tradeTransaction.getTotal());
			stmt1.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int randomNumber(int num) {
		Random rand = new Random();

		int n = rand.nextInt(num) + 0;
		return n;
	}
	
	private double randomDouble(){
		Random r = new Random(); 
	    double d = -4.0 + r.nextDouble() * 8.0;
		return d; 
	}

	private String[] getSymbolsArray() {

		String[] symbols = new String[20];
		
		for (int i = 0; i < 20; i++) {
			String symbol = Details.symbols[i];
			symbols[i] = symbol;

		}
		
		return symbols;
	}

	public List<UserGameParticipation> getListOfParticipants(int gameID) {

		List<UserGameParticipation> users = new ArrayList<UserGameParticipation>();
		
		try {
			
			PreparedStatement stmt1 = conn.prepareStatement("SELECT game_id, email, balance FROM fyp_user_game_participation WHERE game_id = ?");
			stmt1.setInt(1, gameID);
			ResultSet rs = stmt1.executeQuery();

			while (rs.next()) {
				
				UserGameParticipation user = new UserGameParticipation();
				user.setEmail(rs.getString("email"));
				user.setBalance(rs.getDouble("balance"));
				user.setGameID(rs.getInt("game_id"));
				users.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}

	public User getUser(String email) {
		
		User u = new User();
		
		try {
			PreparedStatement stmt1 = conn.prepareStatement("SELECT * FROM fyp_user WHERE email = ?");
			stmt1.setString(1, email);
			ResultSet rs = stmt1.executeQuery();

			while (rs.next()) {
				u.setEmail(rs.getString("email"));
				u.setPassword(rs.getString("password"));
				u.setFirstName(rs.getString("first_name"));
				u.setLastName(rs.getString("last_name"));
				u.setCountry(rs.getString("country"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return u;
	}

	public List<Integer> getTransactionIDs(String email, String symbol, int gameID) {

		List<Integer> ids = new ArrayList<Integer>();
		List<Trade> trades = getTradeList();

		for (Trade t : trades) {
			if (t.getEmail().equals(email) && t.getSymbol().equals(symbol)
					&& t.getGameID() == gameID)
				ids.add(t.getTransaction().getTransactionID());
		}
		return ids;
	}

	public List<Trade> getTradeList() {
		
		List<Trade> limitOrdersList = new ArrayList<Trade>();

		try {

			// Gets list of active limit orders from trade table
			PreparedStatement stmt1 = conn.prepareStatement("SELECT * FROM fyp_trade");
			ResultSet rs = stmt1.executeQuery();


			while (rs.next()) {

				Trade trade = new Trade();
				trade.setTradeID(rs.getInt("trade_id"));
				trade.setGameID(rs.getInt("game_id"));
				trade.setEmail(rs.getString("email"));
				trade.setSymbol(rs.getString("symbol"));
				trade.setDate(rs.getDate("date"));
				trade.setBuyOrSell(rs.getString("buy_or_sell"));
				trade.setTradeType(rs.getString("trade_type"));
				trade.getTransaction().setTransactionID(rs.getInt("transaction_id"));
				limitOrdersList.add(trade);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return limitOrdersList;
	}

	public StockOwned calculateNewAvgPurchPrice(List<Integer> transactionIDs, String email, String symbol, int defaultGameID) {

		int quantity = 0;
		double total = 0;

		TradeTransaction transaction = new TradeTransaction();

		for (int id : transactionIDs) {
			transaction = getTransaction(id);
			quantity += transaction.getQuantity();
			total += transaction.getTotal();
		}
		double avgPurchPrice = total / (double) quantity;

		StockOwned so = new StockOwned();
		so.setGameID(defaultGameID);
		so.setEmail(email);
		so.setQuantity(quantity);
		so.setSymbol(symbol);
		so.setAvgPurchPrice(avgPurchPrice);
		return so;

	}

	public TradeTransaction getTransaction(int transactionID) {
		TradeTransaction transaction = new TradeTransaction();

		try {

			PreparedStatement stmt2 = conn.prepareStatement("SELECT * FROM fyp_trade_transaction WHERE transaction_id = ?");
			stmt2.setInt(1, transactionID);
			ResultSet rs = stmt2.executeQuery();

			while (rs.next()) {
				transaction.setQuantity(rs.getInt("quantity"));
				transaction.setTransactionID(rs.getInt("transaction_id"));
				transaction.setSharePrice(rs.getDouble("share_price"));
				transaction.setTotal(rs.getDouble("total"));
			}

			stmt2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return transaction;
	}

	public void updateBalance(String email, double total, int gameID) {

		double balance = 0;
		// [1] Get existing balance
		try {

			PreparedStatement stmt1 = conn.prepareStatement("SELECT balance FROM fyp_user_game_participation WHERE email = ? and game_id = ?");
			stmt1.setString(1, email);
			stmt1.setInt(2, gameID);
			ResultSet rs = stmt1.executeQuery();
			
			while (rs.next())
				balance = rs.getDouble("balance");

			double newBal = balance - total;

			PreparedStatement stmt2 = conn.prepareStatement("UPDATE fyp_user_game_participation SET balance = ? WHERE email = ? AND game_id = ?");
			stmt2.setDouble(1, newBal);
			stmt2.setString(2, email);
			stmt2.setInt(3, gameID);

			stmt2.executeUpdate();

			stmt1.close();
			stmt2.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void updateQuantity(String email, String symbol, int defaultGameID,
			StockOwned so) {

		int oldQuantity = getQuantity(email, symbol, defaultGameID);

		if (oldQuantity == 0)
			insertStockOwned(so);
		else {
			so.setQuantity(so.getQuantity() + oldQuantity);
			updateStockOwned(so);
		}
	}

	public int getQuantity(String email, String symbol, int gameID) {

		List<StockOwned> stocks = getStockOwnedList();

		for (StockOwned so : stocks)
			if (so.getEmail().equals(email) && so.getSymbol().equals(symbol) && so.getGameID() == gameID)
				return so.getQuantity();

		return 0;
	}

	public List<StockOwned> getStockOwnedList() {
		List<StockOwned> stocksOwned = new ArrayList<StockOwned>();

		try {

			PreparedStatement stmt1 = conn.prepareStatement("SELECT email, game_id, symbol, quantity, avg_purch_price FROM fyp_stock_owned");
			ResultSet rs = stmt1.executeQuery();

			while (rs.next()) {
				StockOwned stockOwned = new StockOwned();
				stockOwned.setEmail(rs.getString("email"));
				stockOwned.setQuantity(rs.getInt("quantity"));
				stockOwned.setSymbol(rs.getString("symbol"));
				stockOwned.setAvgPurchPrice(rs.getDouble("avg_purch_price"));
				stockOwned.setGameID(rs.getInt("game_id"));

				stocksOwned.add(stockOwned);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stocksOwned;
	}

	public void insertStockOwned(StockOwned so) {

		try {

			PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO fyp_stock_owned VALUES (?, ?, ?, ?, ?)");
			stmt2.setInt(1, so.getGameID());
			stmt2.setString(2, so.getEmail());
			stmt2.setString(3, so.getSymbol());
			stmt2.setInt(4, so.getQuantity());
			stmt2.setDouble(5, so.getAvgPurchPrice());
			stmt2.execute();
			stmt2.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateStockOwned(StockOwned so) {

		try {

			PreparedStatement stmt2 = conn.prepareStatement("UPDATE fyp_stock_owned SET quantity = ?, avg_purch_price = ? WHERE email = ? AND symbol = ? AND game_id = ?");
			stmt2.setInt(1, so.getQuantity());
			stmt2.setDouble(2, so.getAvgPurchPrice());
			stmt2.setString(3, so.getEmail());
			stmt2.setString(4, so.getSymbol());
			stmt2.setInt(5, so.getGameID());
			stmt2.executeUpdate();
			stmt2.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private double getYesterdaysAccValue(int gameID, String email, Date date, Date previousDate) {
		double oldVal = 0;
		try {
			PreparedStatement stmt2 = conn.prepareStatement("select closing_acc_value from fyp_user_game_account_history where date = ?");
			stmt2.setDate(1, previousDate);
			ResultSet rs = stmt2.executeQuery();
			
			while(rs.next()){
				//System.out.println("Results found for date: "+previousDate);
				oldVal = rs.getDouble("closing_acc_value");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return oldVal;
	}
}
