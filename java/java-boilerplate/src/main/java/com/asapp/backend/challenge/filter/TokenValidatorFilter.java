package com.asapp.backend.challenge.filter;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.asapp.backend.challenge.utils.DatabaseConnection;
import com.asapp.backend.challenge.utils.ValidationFunctions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

public class TokenValidatorFilter {

    public static Filter validateUser = (Request req, Response resp) -> {

    	try {
    		
    		String authToken = "";
    		authToken = req.headers("Authorization");
        	DatabaseConnection con = DatabaseConnection.getInstance();
     	
    		String q = "SELECT user_id, expire_datetime from sessions where token =\"" + authToken + "\";";

    		Statement pstmt = con.getConnection().createStatement();  
            ResultSet rs = pstmt.executeQuery(q);  
            
            //user not found
    		if (!rs.next()) {
    			JsonObject jsonErrTokenNotFound = new JsonObject();
    			jsonErrTokenNotFound.addProperty("result", "Token not Found.");
    			JsonObject jsonErrTokenNotFoundResult = new Gson().fromJson(jsonErrTokenNotFound, JsonObject.class);
    			throw new Exception(jsonErrTokenNotFoundResult.toString());
    		}
    		else {
    			
            	//check that the user has a session with the specified token
    			Long userFromParams = -1L;
    			Long userIdFromDB = rs.getLong("user_id");
    			String expireDatetimeFromDB = rs.getString("expire_datetime");
    			

    			//GET and POST gets the id from different sides. get id (sender or recipient) and see if token is valid
    			if(req.requestMethod().equals("POST")) {
    	    		JsonObject jsonReq = new Gson().fromJson(req.body(), JsonObject.class);
    	    		userFromParams = jsonReq.get("sender").getAsLong();
    			}
    			else if(req.requestMethod().equals("GET")) {
    				String paramsString = req.queryString();
    				String[] paramsArray = paramsString.split("&");    		        
    				Map<String, String> map = new HashMap<String, String>();
    				for (String param : paramsArray) {
    				    try {
    				        String name = param.split("=")[0];
    				        String value = param.split("=")[1];
    				        map.put(name, value);
    				    }catch(Exception e) {
    				    	System.out.println("Error while parsing parameters");
    				    }
    				  }
    				userFromParams =Long.parseLong(map.get("recipient"));				

    			}
    			if (!(userIdFromDB.equals(userFromParams))) {
        			JsonObject jsonErrUserNotFound = new JsonObject();
        			jsonErrUserNotFound.addProperty("result", "Token is not valid for the given user_id");
        			JsonObject jsonErrUserNotFoundResult = new Gson().fromJson(jsonErrUserNotFound, JsonObject.class);
        			throw new Exception(jsonErrUserNotFoundResult.toString());
    			}
    			
    			
        	    
    			boolean isExpired = ValidationFunctions.isExpiredTime(expireDatetimeFromDB);
    			
    			if(isExpired) {
        			JsonObject jsonExpiredSession = new JsonObject();
        			jsonExpiredSession.addProperty("result", "The session corresponding with the provided token has expired");
        			JsonObject jsonExpiredSessionResult = new Gson().fromJson(jsonExpiredSession, JsonObject.class);
        			throw new Exception(jsonExpiredSessionResult.toString());
    			}
    		}
    		        	
	
    		
    	}
    	catch (Exception e) {
    		
        	JsonObject jsonObjectResult = new Gson().fromJson(e.getMessage(), JsonObject.class);
      		Spark.halt(401, jsonObjectResult.toString());
            System.out.println(e.getMessage());     		
    		
    		
    		
    	}

    };
}
