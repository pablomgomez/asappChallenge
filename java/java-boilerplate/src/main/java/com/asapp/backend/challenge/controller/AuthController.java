package com.asapp.backend.challenge.controller;

import com.asapp.backend.challenge.resources.LoginResource;
import com.asapp.backend.challenge.resources.UserResource;
import com.asapp.backend.challenge.utils.DatabaseConnection;
import com.asapp.backend.challenge.utils.JSONUtil;
import com.asapp.backend.challenge.utils.TokenGenerator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import spark.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;



public class AuthController {

    public static Route login = (Request req, Response resp) -> {
    	LoginResource response =new LoginResource();
    	
    	DatabaseConnection con = DatabaseConnection.getInstance();
    	UserResource user = new UserResource();
		JsonObject jsonReq = new Gson().fromJson(req.body(), JsonObject.class);

		String username = jsonReq.get("username").getAsString();
		String password = jsonReq.get("password").getAsString();
        try{  
    		String q = "SELECT id, password from users where username=\"" + username + "\";";

    		Statement pstmt = con.getConnection().createStatement();  
            ResultSet rs = pstmt.executeQuery(q);  
            
            //user not found
    		if (!rs.next()) {
    			JsonObject jsonErrUserNotFound = new JsonObject();
    			jsonErrUserNotFound.addProperty("result", "Please check your credentials and try again.");
    			JsonObject jsonErrUserNotFoundResult = new Gson().fromJson(jsonErrUserNotFound, JsonObject.class);
    			Spark.halt(401, jsonErrUserNotFoundResult.toString());
    		}
    		
    		//user/pass combination wrong
    		String pass = rs.getString("password");
    		Long id = rs.getLong("id");
    		
    		if(!(pass.equals(password))) {
    			JsonObject jsonErrUserPassCombination = new JsonObject();
    			jsonErrUserPassCombination.addProperty("result", "The combination for the given user/password is incorrect.");
    			JsonObject jsonErrUserPassCombinationResult = new Gson().fromJson(jsonErrUserPassCombination, JsonObject.class);
    			Spark.halt(401, jsonErrUserPassCombinationResult.toString());    			
    		}
    		
    		
    		//all ok, create an expiration timestamp 5 minutes (300 seconds) ahead of now 
    	    Calendar cal = Calendar.getInstance();
    	    cal.add(Calendar.SECOND, 300);
    	    Timestamp timestamp = new Timestamp(cal.getTime().getTime());
    		
    		
    	    //create a token
    	    String token = TokenGenerator.generateNewToken();
    	    
    	    //insert session into database
    		String iq = "INSERT INTO sessions(user_id, token, expire_datetime) VALUES(" + id +",\"" + token +"\", \"" + timestamp+"\" )";
    		PreparedStatement insertStatement = con.getConnection().prepareStatement(iq);  
            int res = (insertStatement.executeUpdate());
        
            
            
            //read id of the inserted record
            ResultSet generatedKeys = insertStatement.getGeneratedKeys();
            generatedKeys.next();
            
            //sets the login model with the result and send the response
            response.setId(generatedKeys.getLong(1));
            response.setToken(token);
            
		
    		
    		
        } catch (SQLException e) {  
        	JsonObject err = new JsonObject();
        	err.addProperty("Result", "Error");
        	JsonObject jsonObjectResult = new Gson().fromJson(err, JsonObject.class);
      		Spark.halt(401, jsonObjectResult.toString());
            System.out.println(e.getMessage());  
        }  
        return JSONUtil.dataToJson(response);   
    };

}
