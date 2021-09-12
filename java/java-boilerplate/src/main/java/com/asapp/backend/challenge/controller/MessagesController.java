package com.asapp.backend.challenge.controller;

import com.asapp.backend.challenge.resources.ImageContent;
import com.asapp.backend.challenge.resources.MessageResource;
import com.asapp.backend.challenge.resources.TextContent;
import com.asapp.backend.challenge.resources.VideoContent;
import com.asapp.backend.challenge.utils.DatabaseConnection;
import com.asapp.backend.challenge.utils.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;


public class MessagesController {

	
	private static JsonObject postInMessagesTable(Long sender, Long recipient, String messageType, Long message_id) throws SQLException {
		
	    Calendar cal = Calendar.getInstance();
	    Timestamp timestamp = new Timestamp(cal.getTime().getTime());
	    
	    DatabaseConnection con = DatabaseConnection.getInstance();
		String q = "INSERT INTO messages (sender_id, recipient_id, message_type_id, message_id, timestamp) VALUES (" + sender + ", " + recipient+ ", 0, " + message_id + ", \" " + timestamp + "\")";
        PreparedStatement pstmt = con.getConnection().prepareStatement(q);  
        int res = (pstmt.executeUpdate());
		
        ResultSet generatedKeys = pstmt.getGeneratedKeys();
        generatedKeys.next();
        

		JsonObject resp = new JsonObject();
		resp.addProperty("id",generatedKeys.getLong(1) );
		resp.addProperty("timestamp", timestamp.toLocalDateTime().toString());
		
		
		return resp;
	}
	
	private static JsonObject sendText(JsonObject jsonReq) throws SQLException {
		Long sender = jsonReq.get("sender").getAsLong();
		Long recipient = jsonReq.get("recipient").getAsLong();
		String text = jsonReq.get("content").getAsJsonObject().get("text").getAsString() ; 
		
		DatabaseConnection con = DatabaseConnection.getInstance();
		String q = "INSERT INTO text_messages (text) VALUES (\""+ text + "\") ; SELECT SCOPE_IDENTITY();";
        PreparedStatement pstmt = con.getConnection().prepareStatement(q);  
        int res = (pstmt.executeUpdate());
        
        //read id of the inserted record
        ResultSet generatedKeys = pstmt.getGeneratedKeys();
        generatedKeys.next();
        
        //set the user id of the recently created user and send response back to user
        Long message_id = generatedKeys.getLong(1);
		
		return postInMessagesTable(sender,recipient, "text", message_id);
		
	}
	
	private static JsonObject sendImage(JsonObject jsonReq) throws SQLException {
		Long sender = jsonReq.get("sender").getAsLong();
		Long recipient = jsonReq.get("recipient").getAsLong();
		String url = jsonReq.get("content").getAsJsonObject().get("url").getAsString();
		Long width = jsonReq.get("content").getAsJsonObject().get("width").getAsLong();
		Long height = jsonReq.get("content").getAsJsonObject().get("height").getAsLong();
				
		
		DatabaseConnection con = DatabaseConnection.getInstance();
		String q = "INSERT INTO image_messages (url, height, width) VALUES (\""+ url + "\", "+height+", "+ width+ ")";

        PreparedStatement pstmt = con.getConnection().prepareStatement(q);  
        int res = (pstmt.executeUpdate());
        
        //read id of the inserted record
        ResultSet generatedKeys = pstmt.getGeneratedKeys();
        generatedKeys.next();
        
        //set the user id of the recently created user and send response back to user
        Long message_id = generatedKeys.getLong(1);
        

		return postInMessagesTable(sender,recipient, "image", message_id);
		
	}
	
	private static JsonObject sendVideo(JsonObject jsonReq) throws Exception {
		
		Long sender = jsonReq.get("sender").getAsLong();
		Long recipient = jsonReq.get("recipient").getAsLong();
		String url = jsonReq.get("content").getAsJsonObject().get("url").getAsString();
		String source = jsonReq.get("content").getAsJsonObject().get("source").getAsString();
		
		if (!(source.toLowerCase().equals("youtube")) && !(source.toLowerCase().equals("vimeo"))) {
			JsonObject jsonErrTokenNotFound = new JsonObject();
			jsonErrTokenNotFound.addProperty("result", "video source not allowed.");
			JsonObject jsonErrTokenNotFoundResult = new Gson().fromJson(jsonErrTokenNotFound, JsonObject.class);
			throw new Exception(jsonErrTokenNotFoundResult.toString());
		}
		
		DatabaseConnection con = DatabaseConnection.getInstance();
		String q = "INSERT INTO video_messages (url, source) VALUES (\"" + url + "\", \""+ source+ "\")";

        PreparedStatement pstmt = con.getConnection().prepareStatement(q);  
        int res = (pstmt.executeUpdate());
        
        //read id of the inserted record
        ResultSet generatedKeys = pstmt.getGeneratedKeys();
        generatedKeys.next();
        
        //set the user id of the recently created user and send response back to user
        Long message_id = generatedKeys.getLong(1);
        

		
		return postInMessagesTable(sender,recipient, "video", message_id);

	}
	
	
	
    public static Route sendMessage = (Request req, Response rep) -> {
    	
    	/*
		Validated from filter: 
			- there is a session for the user_id (sender) with the given token
			- the session has not expired

		To validate in this Route: 
				- the given recipient user_id exists
				- content.type in ["text", "image", "video"]
    	 * */
    	
    	JsonObject messageSendResult = null ;
    	try {
    		
       	 	//obtaint parameters, halt on input error
    		JsonObject jsonReq = new Gson().fromJson(req.body(), JsonObject.class);
    		Long recipient = -1L;
    		recipient = jsonReq.get("recipient").getAsLong();

    		JsonObject error =new JsonObject();
    		if(recipient == null || recipient==-1L) {
    			error.addProperty("result", "recipient cannot be empty");
    			Spark.halt(401, error.toString());
    		}
    		
    		
    		//check for recipient in DB
        	DatabaseConnection con = DatabaseConnection.getInstance();
    		String q = "SELECT id from users where id=\"" + recipient + "\";";

    		Statement pstmt = con.getConnection().createStatement();  
            ResultSet rs = pstmt.executeQuery(q);  
            
            //user not found
    		if (!rs.next()) {
    			JsonObject jsonErrUsrNotFound = new JsonObject();
    			jsonErrUsrNotFound.addProperty("result", "Recipient not Found.");
    			JsonObject jsonErrUsrNotFoundResult = new Gson().fromJson(jsonErrUsrNotFound, JsonObject.class);
    			throw new Exception(jsonErrUsrNotFoundResult.toString());
    		}
    		
    		
			//check for content type
    		JsonObject messageContent = jsonReq.get("content").getAsJsonObject();
			
    		String messageType = messageContent.get("type").getAsString();
    		
    		
    		
    		switch(messageType) {
    		case "text":
    			messageSendResult= sendText(jsonReq);
    			break;
    		case "image":
    			messageSendResult = sendImage(jsonReq);
    			break;
    		case "video":
    			messageSendResult = sendVideo(jsonReq);
    			break;
    			default:
        			JsonObject jsonInvalidType = new JsonObject();
        			jsonInvalidType.addProperty("result", "Invalid message type");
        			JsonObject jsonInvalidTypeResult = new Gson().fromJson(jsonInvalidType, JsonObject.class);
        			throw new Exception(jsonInvalidTypeResult.toString());
    		}
    		
    	    		

			
    		
    	}
    	catch(Exception e) {
        	JsonObject jsonObjectResult = new Gson().fromJson(e.getMessage(), JsonObject.class);
      		Spark.halt(401, jsonObjectResult.toString());
            System.out.println(e.getMessage());   		
    	}


		MessageResource mr = new MessageResource();
		mr.setId(messageSendResult.get("id").getAsLong());
		mr.setTimestamp(messageSendResult.get("timestamp").getAsString());
		
        return JSONUtil.dataToJson(mr);
		
		
    };

    public static Route getMessages = (Request req, Response rep) -> {
    	
    	/*
		Validated from filter: 
			- there is a session for the user_id (recipient) with the given token
			- the session has not expired
			
    	 */
    	
    	
    	// get the query params
		String paramsString = req.queryString();
		String[] paramsArray = paramsString.split("&");    		        
		Map<String, String> map = new HashMap<String, String>();
		for (String param : paramsArray) {
		    try {
		        String name = param.split("=")[0];
		        String value = param.split("=")[1];
		        map.put(name, value);
		    }catch(Exception e) {
		    	System.out.println("Error while parsing parameters");
		    }
		  }
		
		
		
		ArrayList<MessageResource> messages = new ArrayList<MessageResource>();
		
    	try {
    		
    		Long recipient =Long.parseLong(map.get("recipient"));
    		Long limit = Long.parseLong((map.get("limit")));
    		String start = map.get("start");
    		
    		
        	//try to find messages in the messages table. if found, then search for the it in the correpsonding table
        	String q = "SELECT messages.id, messages.sender_id, messages.recipient_id, messages.timestamp ,messages.message_id , message_types.id as mtid, message_types.messages_tablename as mtdes \n"
        			+ "FROM messages, message_types \n"
        			+ "where recipient_id = "+ recipient + "\n"
        			+ "AND timestamp >= \"" + start + "\" \n"
        			+ "AND messages.message_type_id = message_types.id limit " + limit + ";";
        	
        	
        	DatabaseConnection con = DatabaseConnection.getInstance();
    		Statement pstmt = con.getConnection().createStatement();  
            ResultSet rs = pstmt.executeQuery(q);  
            
            //user not found
    		if (!rs.isBeforeFirst()) {
    			JsonObject jsonErrTokenNotFound = new JsonObject();
    			jsonErrTokenNotFound.addProperty("result", "There were no messages found for the recipient after the given timestamp");
    			JsonObject jsonErrTokenNotFoundResult = new Gson().fromJson(jsonErrTokenNotFound, JsonObject.class);
    			throw new Exception(jsonErrTokenNotFoundResult.toString());
    		}
    		
    		
    		ArrayList<JsonObject> textMessages = new ArrayList<JsonObject>();
    		ArrayList<JsonObject> imageMessages = new ArrayList<JsonObject>();
    		ArrayList<JsonObject> videoMessages = new ArrayList<JsonObject>();
    		
    		while(rs.next()) {
    			
    			JsonObject msg = new JsonObject();
    			msg.addProperty("id", rs.getLong("id"));
    			msg.addProperty("timestamp", rs.getString("timestamp"));
    			msg.addProperty("sender", rs.getLong("sender_id"));
    			msg.addProperty("recipient", rs.getLong("recipient_id"));
    			msg.addProperty("message_id", rs.getLong("message_id"));
    			
    			
    			String msgType = rs.getString("mtdes");
    			switch(msgType) {
    			case "text_messages" :
    				textMessages.add(msg);
    				break;
    			case "image_messages": 
    				imageMessages.add(msg);
    				break;
    			case "video_messages":
    				videoMessages.add(msg);
    				break;
    			default:
    				break;
    			}
    			
    		}
    		

    		//Iterator<JsonObject> itImage = imageMessages.iterator();		
    		//Iterator<JsonObject> itVideo = videoMessages.iterator();
    		
    		if (textMessages.size() > 0 ) {
    			
    			String textIds = "";
    			for(int i = 0; i < textMessages.size() ; i++) {
    				JsonObject temp =textMessages.get(i);
    	    		JsonObject jsonReq = new Gson().fromJson(temp, JsonObject.class);
    	    		textIds+= jsonReq.get("message_id").getAsLong()+ ",";
    			}

    			String textIdsString = textIds.substring(0, textIds.length()-1);
    			String qtextMsg  = "SELECT * FROM text_messages where id in (" + textIdsString +")";
    			
    	        ResultSet rstextMsg = pstmt.executeQuery(qtextMsg);  
    	        
    	        //text messages not found
    			if (!rstextMsg.isBeforeFirst()) {
    				JsonObject jsonErrMsgNotFound = new JsonObject();
    				jsonErrMsgNotFound.addProperty("result", "Some messages could not be retrieved");
    				JsonObject jsonErrMsgNotFoundResult = new Gson().fromJson(jsonErrMsgNotFound, JsonObject.class);
    				throw new Exception(jsonErrMsgNotFoundResult.toString());
    			}
    			
    			
    			
    			//if found, append to object in textMessages

    			while(rstextMsg.next()) {
    				JsonObject obj = new JsonObject();
    				obj.addProperty("type", "text");
    				obj.addProperty("text", rs.getString("text"));
    				Long idTemp = rs.getLong("id");
    				
    				Iterator<JsonObject> itText = textMessages.iterator();	
    				boolean found = false;
    				int index = 0;
    				
    				while(itText.hasNext() && !found) {
    					JsonObject obj2 = itText.next();
    					Long  msg_id = obj2.get("message_id").getAsLong();
    					if(msg_id.equals(idTemp)) {
    						found = true;
    						obj2.add("content", obj);
    						textMessages.set(index, obj2);
    					}
    					else index++;
    					
    				}
    			}	
    		}
    		
    		
    		
    		
    		//SAME FOR IMAGE MESSAGES
    		
    		if (imageMessages.size() > 0 ) {
    			
    			String textIds = "";
    			for(int i = 0; i < imageMessages.size() ; i++) {
    				JsonObject temp =imageMessages.get(i);
    	    		JsonObject jsonReq = new Gson().fromJson(temp, JsonObject.class);
    	    		textIds+= jsonReq.get("message_id").getAsLong()+ ",";
    			}

    			String imageIdsString = textIds.substring(0, textIds.length()-1);
    			String qtextMsg  = "SELECT * FROM image_messages where id in (" + imageIdsString +")";
    			
    	        ResultSet rstextMsg = pstmt.executeQuery(qtextMsg);  
    	        
    	        //image messages not found
    			if (!rstextMsg.isBeforeFirst()) {
    				JsonObject jsonErrMsgNotFound = new JsonObject();
    				jsonErrMsgNotFound.addProperty("result", "Some messages could not be retrieved");
    				JsonObject jsonErrMsgNotFoundResult = new Gson().fromJson(jsonErrMsgNotFound, JsonObject.class);
    				throw new Exception(jsonErrMsgNotFoundResult.toString());
    			}
    			
    			
    			
    			//if found, append to object in image_messages

    			while(rstextMsg.next()) {
    				JsonObject obj = new JsonObject();
    				obj.addProperty("type", "image");
    				obj.addProperty("url", rs.getString("url"));
    				obj.addProperty("height", rs.getLong("height"));
    				obj.addProperty("width", rs.getLong("width"));
    				Long idTemp = rs.getLong("id");
    				
    				Iterator<JsonObject> itText = imageMessages.iterator();	
    				boolean found = false;
    				int index = 0;
    				
    				while(itText.hasNext() && !found) {
    					JsonObject obj2 = itText.next();
    					Long  msg_id = obj2.get("message_id").getAsLong();
    					if(msg_id.equals(idTemp)) {
    						found = true;
    						obj2.add("content", obj);
    						imageMessages.set(index, obj2);
    					}
    					else index++;
    					
    				}
    			}	
    		}		
    		
    		
    		
    		//SAME FOR VIDEO MESSAGES
    		
    		if (videoMessages.size() > 0 ) {
    			
    			String textIds = "";
    			for(int i = 0; i < videoMessages.size() ; i++) {
    				JsonObject temp =videoMessages.get(i);
    	    		JsonObject jsonReq = new Gson().fromJson(temp, JsonObject.class);
    	    		textIds+= jsonReq.get("message_id").getAsLong()+ ",";
    			}

    			String videoIdsString = textIds.substring(0, textIds.length()-1);
    			String qtextMsg  = "SELECT * FROM video_messages where id in (" + videoIdsString +")";
    			
    	        ResultSet rstextMsg = pstmt.executeQuery(qtextMsg);  
    	        
    	        //image messages not found
    			if (!rstextMsg.isBeforeFirst()) {
    				JsonObject jsonErrMsgNotFound = new JsonObject();
    				jsonErrMsgNotFound.addProperty("result", "Some messages could not be retrieved");
    				JsonObject jsonErrMsgNotFoundResult = new Gson().fromJson(jsonErrMsgNotFound, JsonObject.class);
    				throw new Exception(jsonErrMsgNotFoundResult.toString());
    			}
    			
    			
    			
    			//if found, append to object in video_messages

    			while(rstextMsg.next()) {
    				JsonObject obj = new JsonObject();
    				obj.addProperty("type", "video");
    				obj.addProperty("url", rs.getString("url"));
    				obj.addProperty("source", rs.getString("source"));

    				Long idTemp = rs.getLong("id");
    				
    				Iterator<JsonObject> itText = videoMessages.iterator();	
    				boolean found = false;
    				int index = 0;
    				
    				while(itText.hasNext() && !found) {
    					JsonObject obj2 = itText.next();
    					Long  msg_id = obj2.get("message_id").getAsLong();
    					if(msg_id.equals(idTemp)) {
    						found = true;
    						obj2.add("content", obj);
    						videoMessages.set(index, obj2);
    					}
    					else index++;
    					
    				}
    			}	
    		}			
    		
    		
    		
    		
    		//finished to form all messages of the response.
    		
    		
    		
  //  		ArrayList<MessageResource> messages = new ArrayList<MessageResource>();
    		Iterator<JsonObject> it = textMessages.iterator();
    		while(it.hasNext()) {
    			JsonObject tempObject = it.next();
    			MessageResource temp = new MessageResource();
    			temp.setId(tempObject.get("id").getAsLong());
    			temp.setTimestamp(tempObject.get("timestamp").getAsString());
    			temp.setSender(tempObject.get("sender").getAsLong());
    			temp.setRecipient(tempObject.get("recipient").getAsLong());
    			
    			TextContent tempMsgContent = new TextContent();
    			tempMsgContent.setType("text");
    			tempMsgContent.setText(tempObject.get("content").getAsJsonObject().get("text").getAsString());
    					
    			temp.setContent(tempMsgContent);
    			
    			messages.add(temp);
    			
    		
        		
        		
    		}
        	
    		
    		Iterator<JsonObject> itimage = imageMessages.iterator();
    		while(itimage.hasNext()) {
    			JsonObject tempObject = itimage.next();
    			MessageResource temp = new MessageResource();
    			temp.setId(tempObject.get("id").getAsLong());
    			temp.setTimestamp(tempObject.get("timestamp").getAsString());
    			temp.setSender(tempObject.get("sender").getAsLong());
    			temp.setRecipient(tempObject.get("recipient").getAsLong());
    			
    			ImageContent tempMsgContent = new ImageContent();
    			tempMsgContent.setType("image");
    			
    			
    			tempMsgContent.setUrl(tempObject.get("content").getAsJsonObject().get("url").getAsString());
    			tempMsgContent.setHeight(tempObject.get("content").getAsJsonObject().get("height").getAsLong());		
    			tempMsgContent.setWidth(tempObject.get("content").getAsJsonObject().get("width").getAsLong());
    			temp.setContent(tempMsgContent);
    			
    			messages.add(temp);
    		}
    		
    		Iterator<JsonObject> itivideo = videoMessages.iterator();
    		while(itivideo.hasNext()) {
    			JsonObject tempObject = itivideo.next();
    			MessageResource temp = new MessageResource();
    			temp.setId(tempObject.get("id").getAsLong());
    			temp.setTimestamp(tempObject.get("timestamp").getAsString());
    			temp.setSender(tempObject.get("sender").getAsLong());
    			temp.setRecipient(tempObject.get("recipient").getAsLong());
    			
    			VideoContent tempMsgContent = new VideoContent();
    			tempMsgContent.setType("video");			
    			tempMsgContent.setUrl(tempObject.get("content").getAsJsonObject().get("url").getAsString());
    			tempMsgContent.setSource(tempObject.get("content").getAsJsonObject().get("source").getAsString());
    			temp.setContent(tempMsgContent);
    			
    			messages.add(temp);
    		}    		

    	}
    	catch(Exception e) {
           	JsonObject jsonObjectResult = new Gson().fromJson(e.getMessage(), JsonObject.class);
      		Spark.halt(401, jsonObjectResult.toString());
            System.out.println(e.getMessage());  
    	}
    	

		
		

		
		
        return JSONUtil.dataToJson(messages);
    };

}
