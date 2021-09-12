const csrf = require('csrf-token')
const login = require('../models/login');
const user = require('../models/user');
const sanitizer = require('../utils/sanitizer')


/**
 * Login allows the user to authenticate with credentials 
 * and get a token to use on secured endpoints. 
 */
module.exports.login = async(req, res) => {

  const db = req.db
  try{
    const username = sanitizer.checkStringSanity(req.body.username) ? req.body.username : null
    const pwd = sanitizer.checkStringSanity(req.body.password) ? req.body.password : null

    if(username != null && pwd != null){
      
      const query = `SELECT id, password from users where username="${username}";`
      const queryResult = await db.get(query)
      password = (queryResult ||{}).password
      
      if(password==undefined) throw new Error("Please check your credentials and try again")      
      if(password !== pwd) throw new Error("The combination for the given user/password is incorrect")
      

      //create an expiration date-time for the current session (5 minutes ahead of now)
      let now =new Date(Date.now())
      let expiryDateTime = new Date(now.setSeconds( now.getSeconds() + 300 ));
      
      //token creation based on username and expire datetime for the session
      const token = csrf.createSync(`${username}-${expiryDateTime.toString()}`)

      //create session in db and return to token to consumer
      const insertQuery = `INSERT INTO sessions(user_id, token, expire_datetime) VALUES(${queryResult.id},"${token}", "${expiryDateTime}" )`
      const loginResult = await db.run(insertQuery)

      user.token = token
      user.id = queryResult.id
      res.status(200).json(user);


    }
    else{
      throw new Error("Invalid username or Password")   
    }

  }
  catch(err){
    res.status(400).json({"result": err.message})
  }

};
