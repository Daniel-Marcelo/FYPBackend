import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import mckee.daniel.model.Game;

public class DailyGameStatusCheck extends QuartzJobBean {

	private static Connection conn;

	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		try {
			conn = DriverManager.getConnection(Details.DB_HOST, Details.DB_USERNAME, Details.DB_PASSWORD);

		List<Game> games = getListOfGames();
		
		for(Game game: games){
			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			Date endDate = format.parse(game.getEndDate());
			System.out.println("\nComparing end date: "+endDate+ " vs todays date: "+new Date());
			
			if(new Date().after(endDate) && game.getStatus().equals("Active")){
				
				System.out.println("Game with ID: "+game.getGameID()+" has ended.");
				System.out.println("Updating status to ended");
				updateStatus(game.getGameID(), "Ended");
				System.out.println("Game: "+game.getGameName()+" updated");
			}
		}
		conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Game> getListOfGames() {

		List<Game> games = new ArrayList<Game>();
		try {

				PreparedStatement stmt1 = conn.prepareStatement("SELECT * FROM fyp_game");
				ResultSet rs = stmt1.executeQuery();

				while (rs.next()) {

					Game game = new Game();
					game.setGameID(rs.getInt("game_id"));
					game.setGameName(rs.getString("game_name"));
					game.setGameType(rs.getString("game_type"));
					game.setStartingCash(rs.getDouble("starting_cash"));
					game.setCreatorEmail(rs.getString("creator_email"));
					game.setStartDate((rs.getDate("start_date")).toString());
					game.setEndDate(rs.getDate("end_date").toString());
					game.setStatus(rs.getString("status"));

					if (game.getGameType().equals("Private")) 
						game.setJoinCode(rs.getString("join_code"));
					
					games.add(game);

					//List<User> usersInGame = getListOfUsersInThisGame(game.getGameID());
					//game.setUsersInGame(usersInGame);
					//game.setBoard(getDashboardStats(email, game));
				}
				rs.close();
				stmt1.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return games;
	}
	
	public void updateStatus(int gameID, String status) {

		try{
	
			PreparedStatement stmt3 = conn.prepareStatement("UPDATE fyp_game SET status = ? WHERE game_id = ?");
			stmt3.setString(1, status);
			stmt3.setInt(2, gameID);
			stmt3.execute();
			stmt3.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
