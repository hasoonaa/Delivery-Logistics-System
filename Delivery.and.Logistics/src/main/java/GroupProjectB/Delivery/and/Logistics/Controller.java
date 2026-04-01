package GroupProjectB.Delivery.and.Logistics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Controller {

	public static void connect() throws ClassNotFoundException, SQLException
	{
		System.out.println("Connecting ...:)");
		
		Connection c = null;
		
		Class.forName("org.sqlite.JDBC");
		 
		c = DriverManager.getConnection("JDBC:sqlite:DRIVERS.db");
			
		String SQL  = "SELECT * FROM accounts";
			
		Statement stmnt = c.createStatement();
		ResultSet rs =stmnt.executeQuery(SQL);
			
			while (rs.next())
			{
				String Username = rs.getString("Username");
				String Password = rs.getString("Password");
				
				System.out.println(Username + " " + Password);
			}
			
	}		
			//_________________________________________________________
			
	public static void createAccount() throws ClassNotFoundException, SQLException
	{
		System.out.println("Connecting ...:)");
		
		Connection c = null;
		
		Class.forName("org.sqlite.JDBC");
		
		c = DriverManager.getConnection("JDBC:sqlite:DRIVERS.db");
	
		System.out.println("Connected.. ");
	
		String SQL = "INSERT INTO accounts ('Username', 'Password')\r\n"
				   + "VALUES ('variable-test@hotmail.com', '123');";
		
		String user = "test-email@hotmail.com";
		String password = "hasoonio2837";
		
		String SQL2 = String.format("%s %s", user, password);
		
		// ? params
		
		//we will do it the basic string manipulation way 
		
		
		Statement statement = c.createStatement();
		
		statement.executeUpdate(SQL);
		
		System.out.println("Updated.. ");
	}	
			
			
			
			
			
			
			
			
	
	
	
}
