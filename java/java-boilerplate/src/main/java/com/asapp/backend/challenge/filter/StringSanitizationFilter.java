package com.asapp.backend.challenge.filter;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import com.google.gson.*;


import com.asapp.backend.challenge.utils.ValidationFunctions;
public class StringSanitizationFilter {
	
	public static Filter sanitizeEntries = (Request req, Response resp) -> {
		try {


			JsonObject jsonReq = new Gson().fromJson(req.body(), JsonObject.class);

			String username = jsonReq.get("username").getAsString();
			String password = jsonReq.get("password").getAsString();
			
			boolean userIsValid = ValidationFunctions.checkStringSanity(username);
			boolean passwordIsValid = ValidationFunctions.checkStringSanity(password);
			if (!userIsValid || !passwordIsValid) {
				throw new Exception("{\"result\": \"Either username or password contain not allowed sequence of special characters. Please modify them and try again\"}");
			}
		}
		catch(Exception e) {
	
			
			Spark.halt(401, e.getMessage());
		}

	  };	
}
    
    