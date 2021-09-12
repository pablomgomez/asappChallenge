package com.asapp.backend.challenge.resources;

public class ImageContent extends MessageContent {

	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Long getHeight() {
		return height;
	}
	public void setHeight(Long height) {
		this.height = height;
	}
	public Long getWidth() {
		return width;
	}
	
	public void setWidth(Long width) {
		this.width = width;
	}
	
	private String url;
	private Long height;
	private Long width;
}
