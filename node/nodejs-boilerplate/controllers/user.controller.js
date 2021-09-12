const user = require('../models/user');
const sanitizer = require('../utils/sanitizer')


/**
 * Creates a user.
 */
module.exports.createUser = async (req, res) => {

  const db = req.db

  try{
    const username = sanitizer.checkStringSanity(req.body.username) ? req.body.username : null
    const pwd = sanitizer.checkStringSanity(req.body.password) ? req.body.password : null

    if (username == null || pwd == null) throw new Error("Either username or password contain not allowed sequence of special characters. Please modify them and try again")
    
    const userRulesResult = sanitizer.checkUsernameRules(username)
    const pwdRulesResult = sanitizer.checkPasswordRules(pwd)

    if(!userRulesResult.pass) throw new Error(userRulesResult.reason)
    if(!pwdRulesResult.pass) throw new Error(pwdRulesResult.reason)

    const query = `INSERT INTO users (username, password) VALUES("${username}", "${pwd}");`
    const queryResult = await db.run(query)
    user.id = queryResult.lastID
    res.status(200).json(user);

  }
  catch(err){
    if (err.message.includes("UNIQUE constraint failed")) res.status(200).json({"result": "The username is already taken"})
    else res.status(400).json({"result": err.message})
  }

}
