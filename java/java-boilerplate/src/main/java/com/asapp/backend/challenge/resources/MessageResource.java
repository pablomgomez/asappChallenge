package com.asapp.backend.challenge.resources;


public class MessageResource {

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public Long getSender() {
		return sender;
	}
	public void setSender(Long sender) {
		this.sender = sender;
	}
	public Long getRecipient() {
		return recipient;
	}
	public void setRecipient(Long recipient) {
		this.recipient = recipient;
	}
	public MessageContent getContent() {
		return content;
	}
	public void setContent(MessageContent content) {
		this.content = content;
	}
	private Long id;
	private String timestamp;
	private Long sender;
	private Long recipient;
	private MessageContent content;
	
}
