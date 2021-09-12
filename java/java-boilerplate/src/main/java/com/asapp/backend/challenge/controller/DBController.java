package com.asapp.backend.challenge.controller;

import com.asapp.backend.challenge.utils.db;

import java.io.File;

import spark.Filter;
import spark.Request;
import spark.Response;

public class DBController{

	
	private static File database = null;
	
	private static File getDatabase() {
		return new File(db.DB_FILENAME);
	}
	
	public static File getInstance() {
		if (database == null) database = getDatabase();
		return database;
	}
	

	public static Filter addDatabase = (Request req, Response rep) -> {
		File db = getInstance();
		req.attribute("db", db );	
    };
	
}

