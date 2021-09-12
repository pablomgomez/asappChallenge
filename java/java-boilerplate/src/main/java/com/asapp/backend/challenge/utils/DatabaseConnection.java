package com.asapp.backend.challenge.utils;

import java.sql.Connection;  
import java.sql.DriverManager;
import java.sql.SQLException;  



public class DatabaseConnection {

	
	private static DatabaseConnection instance;
	private static Connection conn;
	
	private DatabaseConnection() {
		

	    conn = null;  
	    try {  
	        String url = "jdbc:sqlite:" + db.DB_FILENAME;  
	        conn = DriverManager.getConnection(url);  
	        System.out.println("Connection to SQLite has been established.");  
	          
	    } catch (SQLException e) {  
	        System.out.println(e.getMessage());  
	    }    
	}
	
	
	public Connection getConnection() {
		return conn;
	}
	
	public static DatabaseConnection getInstance() {
		
		if (instance == null) instance = new DatabaseConnection();
		return instance;
	}
	
	
	
}
