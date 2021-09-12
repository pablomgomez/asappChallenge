package com.asapp.backend.challenge.resources;

public class TextContent extends MessageContent{
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	private String text;

}
