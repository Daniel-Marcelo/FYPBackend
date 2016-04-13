

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mckee.daniel.model.StockOwned;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;


public class EndOfDayAccountValue extends QuartzJobBean {

	@Override
	protected void executeInternal(JobExecutionContext arg0)

			throws JobExecutionException {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(Details.DB_HOST, Details.DB_USERNAME, Details.DB_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		List<String> emails = getAllUserEmails(conn);
		
		for(String email: emails){
			List<Integer> gameIDs = getGameIDsForUser(email, conn);
			
			for(int gameID: gameIDs){
				System.out.println("GameID: "+gameID);
				
				System.out.println("\n\nNEW GAME: "+gameID+" for user: "+email);
				double oldAccVal = getYesterdaysAccValue(gameID, conn, email);
				System.out.println("Yesterdays account value: "+oldAccVal);
				double valueOfStocks = getValueOfStocksOwned(email, conn, gameID);
				System.out.println("totalToBeAdded: "+valueOfStocks);

				double currentBalance = getBalance(gameID, email, conn);
				System.out.println("Current balance: "+currentBalance);
				double currentAccVal = valueOfStocks + currentBalance;
				System.out.println("currentAccVal: "+currentAccVal);

				double percentChange = ((currentAccVal/oldAccVal)*100)-100;
				System.out.println("Percent Change: "+percentChange);
				System.out.println("Divided "+currentAccVal+"/"+oldAccVal+": "+(currentAccVal/oldAccVal)+"\n Multiplied by 100: "+(currentAccVal/oldAccVal)*100+" and minus 100");
				insertClosingBalance(gameID, email, currentAccVal, percentChange, conn);

				
			}
		}
		
		
		
	}
	private void insertClosingBalance(int gameID, String email, double todaysBalance, double percentChange, Connection conn) {

		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        String date = DATE_FORMAT.format(new Date());

			try {
				PreparedStatement stmt1 = conn.prepareStatement("INSERT INTO fyp_user_game_account_history(game_id, email, date, closing_acc_value, percent_change) VALUES(?,?,?,?,?)");
				stmt1.setInt(1, gameID);
				stmt1.setString(2, email);
				stmt1.setString(3, date);
				stmt1.setDouble(4, todaysBalance);
				stmt1.setDouble(5, percentChange);
				
				stmt1.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
	}
	public double getValueOfStocksOwned(String email,Connection conn, int gameID) {

		List<StockOwned> stocksOwned = getStocksOwnedForThisGame(gameID, conn, email);
		
		double totalToBeAdded = 0;
		
		for(StockOwned s: stocksOwned)
			totalToBeAdded += s.getTotal();

		return totalToBeAdded;

	}
	
	public double getBalance(int gameID,String email, Connection conn){
		
		double balance = 0;
		try {
			PreparedStatement stmt1 = conn.prepareStatement("Select balance FROM fyp_user_game_participation WHERE email = ? AND game_id = ?");
			stmt1.setString(1, email);
			stmt1.setInt(2, gameID);
			ResultSet rs = stmt1.executeQuery();

			while(rs.next())
				balance = rs.getDouble(1);
			
			stmt1.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return balance;
	}
	
public List<StockOwned> getStocksOwnedForThisGame(int gameID,Connection conn, String activeUserEmail) {
		
		List<StockOwned> stocksOwned = new ArrayList<StockOwned>();
		
		try {
			PreparedStatement stmt1 = conn.prepareStatement("SELECT symbol, quantity, avg_purch_price FROM fyp_stock_owned WHERE email = ? AND game_id = ?");
			stmt1.setString(1, activeUserEmail);
			stmt1.setInt(2, gameID);
			ResultSet rs = stmt1.executeQuery();
			
			while(rs.next()){
				
				StockOwned stockOwned = new StockOwned();
				stockOwned.setEmail(activeUserEmail);
				stockOwned.setQuantity(rs.getInt("quantity"));
				stockOwned.setSymbol(rs.getString("symbol"));
				stockOwned.setAvgPurchPrice(rs.getDouble("avg_purch_price"));
				stockOwned.setGameID(gameID);

				Stock stock = YahooFinance.get(stockOwned.getSymbol());

				stockOwned.setCurrentPrice(stock.getQuote().getPrice().doubleValue());
				stockOwned.setTotal(stockOwned.getCurrentPrice() * (double) stockOwned.getQuantity());
				stockOwned.setGainOrLoss(stockOwned.getTotal()-(stockOwned.getAvgPurchPrice()*(double)stockOwned.getQuantity()));
				stockOwned.setCompanyName(stock.getName());
				
				stocksOwned.add(stockOwned);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return stocksOwned;
	}
	
	private double getYesterdaysAccValue(int gameID, Connection conn, String email) {
		
		double oldVal = 0;
		
		try {
			PreparedStatement stmt2 = conn.prepareStatement("select balance_id, closing_acc_value from fyp_user_game_account_history where game_id = ? AND email = ? "
							+ "AND balance_id = (SELECT max(balance_id) from fyp_user_game_account_history where game_id = ? AND email = ?)");
			
			stmt2.setInt(1, gameID);
			stmt2.setString(2, email);
			stmt2.setInt(3, gameID);
			stmt2.setString(4, email);
			ResultSet rs = stmt2.executeQuery();
			
			while(rs.next()){
				System.out.println("Results found");
				oldVal = rs.getDouble("closing_acc_value");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return oldVal;
	}
	
	public List<Integer> getGameIDsForUser(String activeUserEmail, Connection conn) {
		
		List<Integer> gameIDs = new ArrayList<Integer>();
		
		try {
			PreparedStatement stmt2 = conn.prepareStatement("SELECT game_id FROM fyp_user_game_participation WHERE email = ?");
			stmt2.setString(1, activeUserEmail);

			ResultSet rs2 = stmt2.executeQuery();
			
			while (rs2.next())
				gameIDs.add(rs2.getInt("game_id"));
			
			rs2.close();
			stmt2.close();
							
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return gameIDs;
	}
	
	
	public List<String> getAllUserEmails(Connection conn){
		List<String> emails = new ArrayList<String>();
		try {
			PreparedStatement stmt1 = conn.prepareStatement("SELECT DISTINCT email FROM fyp_user");
			ResultSet rs = stmt1.executeQuery();
			
			while(rs.next())
				emails.add(rs.getString("email"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return emails;
	}

}
