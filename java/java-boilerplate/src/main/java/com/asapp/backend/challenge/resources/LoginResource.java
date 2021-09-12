package com.asapp.backend.challenge.resources;

public class LoginResource {

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	
	private Long id;
	private String token;
	
}
