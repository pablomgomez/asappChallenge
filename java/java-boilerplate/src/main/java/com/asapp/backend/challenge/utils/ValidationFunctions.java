package com.asapp.backend.challenge.utils;

import java.util.regex.Pattern;

import com.google.gson.JsonObject;

public class ValidationFunctions {
	
	private static String[] INVALID_SEQUENCES= {"'--" }; 
	
	
	public static boolean checkStringSanity(String str) {
			
		boolean isok = true;
		for(int i = 0; i < INVALID_SEQUENCES.length; i++) {
			if(str.contains(INVALID_SEQUENCES[i])) {
				isok = false;
				break;
			}
		}
		return isok;
		
	}


	public static JsonObject checkUsernameRules(String username) {
		//username must have length between 5 and 20 characters
		JsonObject response = new JsonObject();

		
		if (!(username.length() >=5) ||  !(username.length() <= 20)) {
			response.addProperty("pass", false);
			response.addProperty("reason", "Username length must be between 5 and 20 characters");
		 }
		else {
			response.addProperty("pass", true);
		}
		
		return response;
		 
	}


	public static JsonObject checkPasswprdRules(String password) {
		JsonObject response = new JsonObject();
		
		boolean approvesLenght = true;
		boolean approveOneUppercase = true;
		boolean approveOneLowercase = true;
		boolean approveOneNumber = true;
		boolean approveOneSpecial = true;
		
		String uppercaseRegex = "[A-Z]{1,}";
		String lowercaseRegex ="[a-z]{1,}";
		String numberRegex ="[0-9]{1,}";
		String specialCharRegex = "(?=.*[!@#$%^&*])";
		
		
		String reason = "";
		
		if ((password.length() <5) ||  (password.length() > 20)) {
			approvesLenght = false;
			reason = "Password length must be between 5 and 20 characters";
		 }

		if(!Pattern.compile(uppercaseRegex).matcher(password).find()) {
			approveOneUppercase = false;
			reason += " Password must contain at least one uppercase letter.";
		}

		if(!Pattern.compile(lowercaseRegex).matcher(password).find()) {
			approveOneLowercase = false;
			reason += " Password must contain at least one lowercase letter.";
		}
		
		if(!Pattern.compile(numberRegex).matcher(password).find()) {
			approveOneNumber = false;
			reason += " Password must contain at least one numeric character.";
		}
		
		if(!Pattern.compile(specialCharRegex).matcher(password).find()) {
			approveOneSpecial = false;
			reason += " Password must contain at least one of the following special characters (!, @, #, $, %, ^, &, *)";
		}
		
		
		boolean pass = approvesLenght && approveOneUppercase && approveOneLowercase && approveOneNumber && approveOneSpecial;
		response.addProperty("pass", pass);
		response.addProperty("reason", reason);
		return response;
		
	}
	
	
	
	public static boolean isExpiredTime(String timestampFromDb) {
		return false;
	}
	
	

}
