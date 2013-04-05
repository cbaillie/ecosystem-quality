package uk.ac.dotrural.getthere.quality.logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.mysql.jdbc.Statement;

public class ResultViewer {
	
	private Connection con;
	
	private String url = "jdbc:mysql://localhost:8889/irp_quality_logs";
	private String user = "root";
	private String pass = "root";

	public ResultViewer()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, pass);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		//System.out.println("ResultViewer: Connection established...");
	}
	
	public ResultSet getResults(String query)
	{
		ResultSet rs = null;
		try
		{
			Statement stmt = (Statement)con.createStatement();
			rs = stmt.executeQuery(query);
		}
		catch(Exception ex)
		{
			ex.printStackTrace(System.err);
		}
		return rs;
	}
	
}
