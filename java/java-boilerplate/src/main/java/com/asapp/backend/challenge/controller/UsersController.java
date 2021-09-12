package com.asapp.backend.challenge.controller;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.asapp.backend.challenge.resources.UserResource;
import com.asapp.backend.challenge.utils.DatabaseConnection;
import com.asapp.backend.challenge.utils.JSONUtil;
import com.asapp.backend.challenge.utils.ValidationFunctions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class UsersController {

    public static Route createUser = (Request req, Response resp) -> {
	

    	UserResource user = new UserResource();
		JsonObject jsonReq = new Gson().fromJson(req.body(), JsonObject.class);

		String username = jsonReq.get("username").getAsString();
		String password = jsonReq.get("password").getAsString();
		
		//check rules for username
		JsonObject userRulesResult =  ValidationFunctions.checkUsernameRules(username);
		JsonObject jsonUserRulesResult = new Gson().fromJson(userRulesResult, JsonObject.class);
		if (!jsonUserRulesResult.get("pass").getAsBoolean()) {
			Spark.halt(401, jsonUserRulesResult.toString());
		}
		
		//check rules for password
		JsonObject passwordRulesResult = ValidationFunctions.checkPasswprdRules(password);
		JsonObject jsonPasswordRulesResult = new Gson().fromJson(passwordRulesResult,JsonObject.class);
		if (!jsonPasswordRulesResult.get("pass").getAsBoolean()) {
			Spark.halt(401, jsonPasswordRulesResult.toString());
		}		
		
		
		//insert into database 
		DatabaseConnection con = DatabaseConnection.getInstance();
        try{  
    		String q = "INSERT INTO users (username, password) VALUES(\"" + username + "\", \"" + password + "\"); SELECT SCOPE_IDENTITY();";
            PreparedStatement pstmt = con.getConnection().prepareStatement(q);  
            int res = (pstmt.executeUpdate());
            
            //read id of the inserted record
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            generatedKeys.next();
            
            //set the user id of the recently created user and send response back to user
            user.setId(generatedKeys.getLong(1));
            
            
        } catch (Exception e) {  
        	JsonObject err = new JsonObject();
        	err.addProperty("Result", "Error");
        	
        	JsonObject jsonObjectResult = new Gson().fromJson(err, JsonObject.class);
        	if (e.getMessage().contains("UNIQUE constraint failed")) {
        		
        		JsonObject jsonObject = new JsonObject();
        		jsonObject.addProperty("result", "The username is already taken");
        		
        		 jsonObjectResult = new Gson().fromJson(jsonObject, JsonObject.class);
        	}

      		Spark.halt(401, jsonObjectResult.toString());
            System.out.println(e.getMessage());  
        }  
	
		
        return JSONUtil.dataToJson(user);
    };
    
    
}
