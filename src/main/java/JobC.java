

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mckee.daniel.model.Game;
import mckee.daniel.model.User;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class JobC extends QuartzJobBean {

	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {

		System.out.println("Year Simulation\n*******************************************");
		System.out.println("1) Fetching users in default game");
		int defaultGameID = 1;
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(Details.DB_HOST,
					Details.DB_USERNAME, Details.DB_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


}
