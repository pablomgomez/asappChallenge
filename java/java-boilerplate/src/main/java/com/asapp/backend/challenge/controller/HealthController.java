package com.asapp.backend.challenge.controller;

import java.io.File;


import com.asapp.backend.challenge.resources.HealthResource;
import com.asapp.backend.challenge.utils.JSONUtil;
import spark.Request;
import spark.Response;
import spark.Route;

public class HealthController {

	public static Route check = (Request req, Response rep) -> {
		
    	HealthResource health = new HealthResource();
    	File asappDB = req.attribute("db");
    	
    	boolean exists = asappDB.exists();
    	if (exists) {
    		health.setHealth("ok");
    	}
    	else health.setHealth("Database Error");
    	
        return JSONUtil.dataToJson(health);
    };
    
    
}
