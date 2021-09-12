const message = require('../models/message');

/**
 * Send a message from one user to another. 
 * We support three types of messages `text`, `image` and `video` 
 */
module.exports.send = async (req, res) => {
  // TODO: Send a New Message

  const db = req.db
  try{

    //check that both sender and recipient exists
    const querySender = `SELECT id from users where id="${req.body.sender}";`
    const querySenderResult = await db.get(querySender)

    const queryRecipient = `SELECT id from users where id="${req.body.recipient}";`
    const queryRecipientResult = await db.get(queryRecipient)

    if(!(querySenderResult || {}).id) throw new Error(`User not found for sender with id. ${req.body.sender}`)
    if(!(queryRecipientResult || {}).id) throw new Error(`User not found for recipient with id. ${req.body.recipient}`)


    //first query sessions for the given sender id and token
    querySession =`SELECT * FROM sessions WHERE user_id = ${req.body.sender} AND token = "${req.headers.authorization}"`
    querySessionResult = await db.all(querySession)
    if(querySessionResult.length == 0) throw new Error(`The provided Authorization token for the sender user with id ${req.body.sender} is incorrect`)
    
    //filter for expiryDateTime >= now
    let now =new Date(Date.now()).getTime()

    let isThereAnActiveSession = querySessionResult.map( item => {
      let expiryDateTime = new Date(item.expire_datetime).getTime();
      return now <= expiryDateTime
    }).filter( item => item).length > 0
    
    if (!isThereAnActiveSession) throw new Error(`There are not active sessions for the sender user with id: ${req.body.sender} with the specified Authorization token`)
    
    //we are all set to post the message to the recipient
    let response = ''
    switch (req.body.content.type){
      case "text":
        response =  await postTextMessage(db, req.body)
        break;
      case "image":
        response =  await postImageMessage(db, req.body)
        break;
      case "video":
        response =  await postVideoMessage(db, req.body)
        break;
      default:
        throw new Error(`Invalid message type`)
        break;
    }
     message.id = response.queryPostMessageResult.lastID
     message.timestamp =response.message_timestamp
    
    res.status(200).json(message);
  }
  catch(err){
    res.status(400).json({"result": err.message})
  }


}

/**
 * Fetch all existing messages to a given recipient, within a range of message IDs.
 */
module.exports.get = async (req, res) => {

  let startDate =new Date(req.query.start)
  const db = req.db
  try{

    //check that recipient exists
    const queryRecipient = `SELECT id from users where id="${req.query.recipient}";`
    const queryRecipientResult = await db.get(queryRecipient)
    if(!(queryRecipientResult || {}).id) throw new Error(`User not found for recipient with id. ${req.query.recipient}`)


    //first query sessions for the given recipient id and token
    querySession =`SELECT * FROM sessions WHERE user_id = ${req.query.recipient} AND token = "${req.headers.authorization}"`
    querySessionResult = await db.all(querySession)
    if(querySessionResult.length == 0) throw new Error(`The provided Authorization token for the recipient user with id ${req.query.recipient} is incorrect`)
   

    //filter for expiryDateTime >= now
    let now =new Date(Date.now()).getTime()

    let isThereAnActiveSession = querySessionResult.map( item => {
      let expiryDateTime = new Date(item.expire_datetime).getTime();
      return now <= expiryDateTime
    }).filter( item => item).length > 0

    if (!isThereAnActiveSession) throw new Error(`There are not active sessions for the recipient user with id: ${req.query.recipient}`)
        

    //we're all set to get the messages for the recipient

    let queryMessages= `SELECT messages.id, messages.sender_id, messages.recipient_id, messages.timestamp ,messages.message_id , message_types.id as mtid, message_types.messages_tablename as mtdes 
      FROM messages, message_types 
      where recipient_id = ${req.query.recipient}
      AND timestamp >= "${startDate}" 
      AND messages.message_type_id = message_types.id limit ${req.query.limit}`
    let queryMessagesResult= await db.all(queryMessages)
    
    if(!queryMessagesResult.length > 0) throw new Error (`There are no messages found for the recipient ${req.query.recipient} after the given timestamp ${req.query.start}`)
    
    //found some messages. identify type and query message from the corresponding table. 
    let text_messages = queryMessagesResult.filter( msg => msg.mtdes =="text_messages")
    let image_messages = queryMessagesResult.filter( msg => msg.mtdes =="image_messages")
    let video_messages = queryMessagesResult.filter( msg => msg.mtdes =="video_messages")


    let queryTextMessagesResult=[]
    let queryImageMessagesResult=[]
    let queryVideoMessagesResult=[]

    if (text_messages.length > 0){
      text_messages_ids= text_messages.map(msg => msg.message_id)
      let queryTextMessages = `SELECT * FROM text_messages where id in (${text_messages_ids.toString()})`
      queryTextMessagesResult= await db.all(queryTextMessages)
    }

    if (image_messages.length > 0){
      image_messages_ids= image_messages.map(msg => msg.message_id)
      let queryImageMessages = `SELECT * FROM image_messages where id in (${image_messages_ids.toString()})`
      queryImageMessagesResult= await db.all(queryImageMessages)
    }
    
    if (video_messages.length > 0){
      video_messages_ids= video_messages.map(msg => msg.message_id)
      let queryVideoMessages = `SELECT * FROM video_messages where id in (${video_messages_ids.toString()})`
      queryVideoMessagesResult= await db.all(queryVideoMessages)
    }


    //generate response and send
    let allCompletedMessages = queryMessagesResult.map( msg => {
      let retrievedMsg = {
        id: msg.id,
        timestamp: msg.timestamp,
        sender: msg.sender_id,
        recipient: msg.recipient_id,
        content: {}
      }

      if(msg.mtdes == "text_messages") {
        retrievedMsg.content = {
          type: "text",
          text: queryTextMessagesResult[queryTextMessagesResult.findIndex( textmsg => textmsg.id ==msg.message_id)].text
        }
      }
      else if(msg.mtdes == "image_messages"){
        retrievedMsg.content = {
          type: "image",
          url: queryImageMessagesResult[queryImageMessagesResult.findIndex( imgMsg => imgMsg.id ==msg.message_id)].url,
          height: queryImageMessagesResult[queryImageMessagesResult.findIndex( imgMsg => imgMsg.id ==msg.message_id)].height,
          width:queryImageMessagesResult[queryImageMessagesResult.findIndex( imgMsg => imgMsg.id ==msg.message_id)].width
        }
      }
      else if(msg.mtdes == "video_messages"){
        retrievedMsg.content = {
          type: "video",
          url: queryVideoMessagesResult[queryVideoMessagesResult.findIndex( imgMsg => imgMsg.id ==msg.message_id)].url,
          source: queryVideoMessagesResult[queryVideoMessagesResult.findIndex( imgMsg => imgMsg.id ==msg.message_id)].source
        }
      }

        return retrievedMsg
    })

    res.status(200).json({ messages : allCompletedMessages });
    }

    catch(err){
      res.status(400).json({"result": err.message})
    }
};



let postImageMessage =async  (db,body) => {

  try{

    let message_timestamp = new Date(Date.now())
  
    let queryPostImageMessage = `INSERT INTO image_messages (url, height, width) VALUES ("${body.content.url}", ${body.content.height}, ${body.content.width})`
    let queryPostImageMessageResult = await db.run(queryPostImageMessage)
  
    if(queryPostImageMessageResult == undefined ) throw new Error(`Error while posting Image message`)
  
    let queryPostMessage = `INSERT INTO messages (sender_id, recipient_id, message_type_id, message_id, timestamp) VALUES (${body.sender}, ${body.recipient}, 1, ${queryPostImageMessageResult.lastID}, "${message_timestamp}")`
    let queryPostMessageResult = await db.run(queryPostMessage)
    if(queryPostMessageResult == undefined ) throw new Error(`Error while posting`)
  
    return {queryPostMessageResult, message_timestamp}
    }
  
    catch(err){
      throw err
    }

}

let postVideoMessage = async  (db,body) => {

  try{
    let lowersource = body.content.source.toLowerCase()
    if ( lowersource != 'youtube' && lowersource != 'vimeo') throw new Error(`Video source not allowed`)

    let message_timestamp = new Date(Date.now())
  
    let queryPostVideoMessage = `INSERT INTO video_messages (url, source) VALUES ("${body.content.url}", "${body.content.source}")`
    let queryPostVideoMessageResult = await db.run(queryPostVideoMessage)
  
    if(queryPostVideoMessageResult == undefined ) throw new Error(`Error while posting Video message`)
  
    let queryPostMessage = `INSERT INTO messages (sender_id, recipient_id, message_type_id, message_id, timestamp) VALUES (${body.sender}, ${body.recipient}, 2, ${queryPostVideoMessageResult.lastID}, "${message_timestamp}")`
    let queryPostMessageResult = await db.run(queryPostMessage)
    if(queryPostMessageResult == undefined ) throw new Error(`Error while posting message `)
  
    return {queryPostMessageResult, message_timestamp}
    }
  
    catch(err){
      throw err
    }  
}


let postTextMessage = async (db, body) => {

  try{
  let message_timestamp = new Date(Date.now())

  let queryPostTextMessage = `INSERT INTO text_messages (text) VALUES ("${body.content.text}")`
  let queryPostTextMessageResult = await db.run(queryPostTextMessage)

  if(queryPostTextMessageResult == undefined ) throw new Error(`Error while posting message with content : ${body.content.text}`)

  let queryPostMessage = `INSERT INTO messages (sender_id, recipient_id, message_type_id, message_id, timestamp) VALUES (${body.sender}, ${body.recipient}, 0, ${queryPostTextMessageResult.lastID}, "${message_timestamp}")`
  let queryPostMessageResult = await db.run(queryPostMessage)
  if(queryPostMessageResult == undefined ) throw new Error(`Error while posting message with content : ${body.content.text}`)

  return {queryPostMessageResult, message_timestamp}
  }

  catch(err){
    throw err
  }
}