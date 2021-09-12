package com.asapp.backend.challenge.resources;

public class VideoContent extends MessageContent{

	
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	private String source;
	private String url;
}
