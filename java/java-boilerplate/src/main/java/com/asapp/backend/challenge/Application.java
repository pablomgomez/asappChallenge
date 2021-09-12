package com.asapp.backend.challenge;

import com.asapp.backend.challenge.controller.AuthController;
import com.asapp.backend.challenge.controller.DBController;
import com.asapp.backend.challenge.controller.HealthController;
import com.asapp.backend.challenge.controller.MessagesController;
import com.asapp.backend.challenge.controller.UsersController;
import com.asapp.backend.challenge.filter.StringSanitizationFilter;
import com.asapp.backend.challenge.filter.TokenValidatorFilter;
import com.asapp.backend.challenge.utils.Path;

import spark.Spark;

public class Application {

	public static void main(String[] args) {

		// Spark Configuration
		Spark.port(8080);

		// Configure Endpoints

		// Users
		Spark.before(Path.USERS, DBController.addDatabase);
		Spark.before(Path.USERS,StringSanitizationFilter.sanitizeEntries);
		Spark.post(Path.USERS, UsersController.createUser);

		// Auth
		Spark.before(Path.LOGIN, DBController.addDatabase);
		Spark.before(Path.LOGIN,StringSanitizationFilter.sanitizeEntries);
		Spark.post(Path.LOGIN, AuthController.login);

		// Messages
		Spark.before(Path.MESSAGES, DBController.addDatabase);
		Spark.before(Path.MESSAGES, TokenValidatorFilter.validateUser);

		Spark.post(Path.MESSAGES, MessagesController.sendMessage);
		Spark.get(Path.MESSAGES, MessagesController.getMessages);

		// Health
		Spark.before(Path.HEALTH, DBController.addDatabase);
		Spark.post(Path.HEALTH, HealthController.check);

	}

}
